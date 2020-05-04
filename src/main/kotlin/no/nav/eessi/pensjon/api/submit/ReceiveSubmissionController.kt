package no.nav.eessi.pensjon.api.submit

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.kafka.KafkaService
import no.nav.eessi.pensjon.services.pdf.TemplateService
import no.nav.eessi.pensjon.services.storage.StorageService
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.eessi.pensjon.utils.successBody
import no.nav.security.oidc.api.Protected
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.PostConstruct

@Protected
@RestController
@RequestMapping("/api/submission")
class ReceiveSubmissionController(

    val kafkaService: KafkaService,
    val storageService: StorageService,
    val oidcRequestContextHolder: OIDCRequestContextHolder,
    val templateService: TemplateService,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(ReceiveSubmissionController::class.java)
    private val mapper = ObjectMapper()

    private lateinit var soknad_sendt_kafka: MetricsHelper.Metric
    private lateinit var soknad_resendt_kafka: MetricsHelper.Metric
    private lateinit var kvittering_sendt_kafka: MetricsHelper.Metric
    private lateinit var hent_innsending: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        soknad_sendt_kafka = metricsHelper.init("soknad_sendt_kafka")
        soknad_resendt_kafka = metricsHelper.init("soknad_resendt_kafka")
        kvittering_sendt_kafka = metricsHelper.init("kvittering_sendt_kafka")
        hent_innsending = metricsHelper.init("hent_innsending")
    }


    val PINFO_SUBMISSION = "PinfoSubmission"
    val PINFO_SUBMISSION_RECEIPT = "PinfoSubmissionReceipt"

    @PostMapping(value = ["/submit/{page}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun receiveSubmission(@PathVariable(required = true) page: String,
                          @RequestBody requestBody: SubmissionRequest) : ResponseEntity<String> {

        val personIdentifier = getClaims(oidcRequestContextHolder).subject
        val uuid = UUID.randomUUID().toString()
        logger.info("Sender inn endelig eessi-personinformasjon. $uuid")

        val content = mapper.writeValueAsString(requestBody)

        return soknad_sendt_kafka.measure {
            val filename: String
            try {
                filename = lagreFil(personIdentifier, PINFO_SUBMISSION, content)
                putOnKafka(filename, uuid)
                ResponseEntity.status(HttpStatus.OK).body(successBody())
            } catch (ex: Exception) {
                logger.error(ex.message + " uuid: $uuid", ex)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("En feil oppstod under innsending av skjema", uuid))
            }
        }
    }

    @PostMapping(value = ["/resubmit"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun resendSubmission(@RequestBody fileName: String) : ResponseEntity<String> {
        logger.info("Trying to resubmit")

        return soknad_resendt_kafka.measure {
            try {
                kafkaService.publishSubmissionReceivedEvent(fileName)
                logger.info("Resubmitted on kafka queue")
                ResponseEntity.ok().body(successBody())
            } catch (ex: Exception) {
                val uuid = UUID.randomUUID().toString()
                logger.error("Resubmit feilet. $ex.message uuid: $uuid")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Resend av submission feilet. ", uuid))
            }
        }
    }

    @GetMapping("/receipt/{page}")
    fun sendReceipt(@PathVariable(required = true) page: String): String {
        logger.info("Sender inn endelig kvittering")

        return kvittering_sendt_kafka.measure {

            val personIdentifier = getClaims(oidcRequestContextHolder).subject
            val receipt: Map<String, Any>
            val receiptJson: String
            val filename: String
            val submission = getSubmission(personIdentifier) ?: throw IllegalStateException("No submission")
            val uuid = UUID.randomUUID().toString()

            try {
                receipt = templateService.generateReceipt(submission, personIdentifier, page)
                receiptJson = ObjectMapper().writeValueAsString(receipt)
                filename = lagreFil(personIdentifier, PINFO_SUBMISSION_RECEIPT, receiptJson)
            } catch (ex: Exception) {
                logger.error("En feil oppstod under produsering av pdf for kvittering $ex")
                throw ex
            }

            try {
                putOnKafka(filename, uuid)
                logger.info("put receipt on kafka queue")
            } catch (ex: Exception) {
                logger.error("En feil oppstod under produsering av kafkamelding for kvittering $ex")
                throw ex
            }
            receiptJson
        }
    }

    @GetMapping(value = ["/get/{subject}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSubmissionAsJson(@PathVariable subject: String): ResponseEntity<String> {

        return hent_innsending.measure {
            try {
                val resp = mapAnyToJson(mapOf("content" to getSubmission(subject)))
                ResponseEntity.ok().body(resp)
            } catch (ex: Exception) {
                val uuid = UUID.randomUUID().toString()
                logger.error("Get feilet. $ex.message $uuid")
                ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.message)
            }
        }
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

    //helper function to put message on kafka, will retry 3 times and wait before fail
    fun putOnKafka(message: String, uuid: String): String {
        logger.info("Trying to resubmit")

        var count = 0
        val maxTries = 3
        val waitTime = 8000L
        var failException : Exception ?= null

        while (count < maxTries) {
            try {
                kafkaService.publishSubmissionReceivedEvent(message)
                logger.info("put submission on kafka queue, uuid: $uuid")
                return uuid
            } catch (ex: Exception) {
                count++
                logger.warn("Failed to put submission on kafka, try nr.: $count, Error message: ${ex.message} ")
                failException = ex
                Thread.sleep(waitTime)
            }
        }
        logger.error("Failed to put message on kafka, uuid: $uuid. meesage: $message", failException)
        throw failException!!

    }

    val dateTimeStrToLocalDateTime: (String) -> LocalDateTime = {
        val dateString = ".*___${PINFO_SUBMISSION}___(.*)\\.json".toRegex().matchEntire(it)?.groups?.get(1)?.value
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    }
}
