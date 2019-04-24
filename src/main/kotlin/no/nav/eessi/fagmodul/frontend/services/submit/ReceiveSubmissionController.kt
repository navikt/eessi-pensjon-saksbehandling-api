package no.nav.eessi.fagmodul.frontend.services.submit

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.eessi.fagmodul.frontend.services.kafka.KafkaService
import no.nav.eessi.fagmodul.frontend.services.pdf.PdfService
import no.nav.eessi.fagmodul.frontend.services.storage.StorageService
import no.nav.eessi.fagmodul.frontend.utils.counter
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import no.nav.eessi.fagmodul.frontend.utils.getClaims
import no.nav.eessi.fagmodul.frontend.utils.successBody
import no.nav.security.oidc.api.Protected
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Protected
@RestController
@RequestMapping("/api/submission")
class ReceiveSubmissionController(val kafkaService: KafkaService,
                                  val storageService: StorageService,
                                  val javaTimeObjectMapper: ObjectMapper,
                                  val oidcRequestContextHolder: OIDCRequestContextHolder,
                                  val pdfService: PdfService) {

    private val logger = LoggerFactory.getLogger(ReceiveSubmissionController::class.java)
    private final val kafkaSendtTilFssTellerNavn = "eessipensjon_frontend-api.soknad_sendt_kafka"
    private val kafkaSendtTilFssVellykkede = counter(kafkaSendtTilFssTellerNavn, "vellykkede")
    private val kafkaSendtTilFssFeilede = counter(kafkaSendtTilFssTellerNavn, "feilede")

    private final val kafkaKvitteringTilFssTellerNavn = "eessipensjon_frontend-api.kvittering_sendt_kafka"
    private val kafkaKvitteringTilFssVellykkede = counter(kafkaKvitteringTilFssTellerNavn, "vellykkede")
    private val kafkaKvitteringTilFssFeilede = counter(kafkaKvitteringTilFssTellerNavn, "feilede")

    val PINFO_SUBMISSION = "PinfoSubmission"
    val PINFO_SUBMISSION_RECEIPT = "PinfoSubmissionReceipt"

    @PostMapping(value = ["/submit"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun receiveSubmission(@RequestBody requestBody: SubmissionRequest): Map<String, String> {

        logger.info("Sender inn endelig eessi-personinformasjon")
        val personIdentifier = getClaims(oidcRequestContextHolder).subject

        val content = javaTimeObjectMapper.writeValueAsString(requestBody)
        val filename: String

        try {
            filename = lagreFil(personIdentifier, PINFO_SUBMISSION, content)
            kafkaService.publishSubmissionReceivedEvent(filename)
            kafkaSendtTilFssVellykkede.increment()
            logger.info("put submission on kafka queue")
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            kafkaSendtTilFssFeilede.increment()
            throw ex
        }
        return mapOf("filename" to filename)
    }

    @PostMapping(value = ["/resubmit"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun resendSubmission(@RequestBody fileName: String) : ResponseEntity<String> {
        logger.info("Trying to resubmit")

        return try {
            kafkaService.publishSubmissionReceivedEvent(fileName)
            kafkaSendtTilFssVellykkede.increment()
            logger.info("Resubmitted on kafka queue")
            ResponseEntity.ok().body(successBody())
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            kafkaSendtTilFssFeilede.increment()
            logger.error("Resubmit feilet. $ex.message uuid: $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Resend av submission feilet. ",uuid))
        }
    }

    @GetMapping("/receipt")
    fun sendReceipt(): String {
        logger.info("Sender inn endelig kvittering")
        val personIdentifier = getClaims(oidcRequestContextHolder).subject
        val receipt : Map<String, Any>
        val receiptJson : String
        val filename: String
        val submission = getSubmission(personIdentifier) ?: throw IllegalStateException("No submission")

        try {
            receipt = pdfService.generateReceipt(submission, personIdentifier)
            receiptJson = ObjectMapper().writeValueAsString(receipt)
            filename = lagreFil(personIdentifier, PINFO_SUBMISSION_RECEIPT, receiptJson)
        } catch (ex : Exception) {
            logger.error(ex.message, ex)
            throw ex
        }

        try {
            kafkaService.publishSubmissionReceivedEvent(filename)
            kafkaKvitteringTilFssVellykkede.increment()
            logger.info("put receipt on kafka queue")
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            kafkaKvitteringTilFssFeilede.increment()
            throw ex
        }
        return receiptJson
    }

    val dateTimeStrToLocalDateTime: (String) -> LocalDateTime = {
        val dateString = ".*___${PINFO_SUBMISSION}___(.*)\\.json".toRegex().matchEntire(it)?.groups?.get(1)?.value
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    }

    fun getSubmission(subject: String): String? {
        val list = storageService.list("${subject}___${PINFO_SUBMISSION}___")
        if (list.isEmpty()) {
            return null
        }
        val sortedList = list.sortedByDescending(dateTimeStrToLocalDateTime)
        return storageService.get(sortedList[0])
    }

    private fun lagreFil(personIdentifier: String, fileDescription: String, content: String): String {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        val filename = "${personIdentifier}___${fileDescription}___$now.json"
        storageService.put(filename, content)
        return filename
    }
}