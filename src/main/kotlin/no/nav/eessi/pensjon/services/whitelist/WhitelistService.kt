package no.nav.eessi.pensjon.services.whitelist

import com.amazonaws.services.s3.model.AmazonS3Exception
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

private val logger = LoggerFactory.getLogger(WhitelistService::class.java)

@Service
class WhitelistService(
    val storageService: StorageService,
    @Value("\${eessi.pensjon.whitelist.users}") var newUsersToWhitelist: List<String>,
    @Value("\${eessi.pensjon.frontend.api.s3.whitelist.key.ending}") var whitelistEnding: String,
    @Value("\${eessi.pensjon.frontend.api.s3.personidentifier.separator}") var personIdentifierSeparator: String,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    @PostConstruct
    fun startup() {
        whitelistNewUsersFromFasit()
    }

    private fun whitelistNewUsersFromFasit() {
        logger.info("Whitelisting new users $newUsersToWhitelist")

        newUsersToWhitelist.iterator().forEach {
            if (!isPersonWhitelisted(it)) {
                logger.info("Whitelister bruker $it")
                addToWhitelist(it)
            }
        }
    }

    fun addToWhitelist(personIdentifikator: String) {
        return metricsHelper.measure("addToWhitelist") {
            return@measure try {
                storageService.put(
                    "$personIdentifikator$personIdentifierSeparator$whitelistEnding",
                    "{ \"timestamp\": \"${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)}\"}"
                )
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under whitelisting: " + ex.message)
            }
        }
    }

    fun isPersonWhitelisted(key: String): Boolean {
        logger.info("Sjekker om borger er whitelistet")
        return metricsHelper.measure("isPersonWhitelisted") {
            return@measure try {
                val resp = storageService.get("$key$personIdentifierSeparator$whitelistEnding")
                if (resp != null) {
                    logger.info("Etterspurt borger er whitelistet")
                    true
                } else {
                    logger.info("Etterspurt borger er ikke whitelistet")
                    false
                }
            } catch (ae: AmazonS3Exception) {
                if (ae.statusCode == HttpStatus.NOT_FOUND.value()) {
                    logger.info("Etterspurt borger er ikke whitelistet")
                } else {
                    logger.error("Noe gikk galt under sjekk av whitelisting, borger anses som ikke whitelistet, $ae")
                }
                false
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under sjekk av whitelisting, borger anses som ikke whitelistet, $ex")
                false
            }
        }
    }

    fun isKeyWhitelist(key: String): Boolean {
        if (key.isEmpty()) {
            return false
        }

        if (!key.contains(personIdentifierSeparator)) {
            return false
        }
        return key.split(personIdentifierSeparator)[1] == whitelistEnding
    }
}
