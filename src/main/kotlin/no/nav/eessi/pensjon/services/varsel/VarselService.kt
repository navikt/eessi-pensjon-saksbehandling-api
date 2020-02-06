//package no.nav.eessi.pensjon.services.varsel
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import io.micrometer.core.instrument.simple.SimpleMeterRegistry
//import no.nav.eessi.pensjon.metrics.MetricsHelper
//import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
//import no.nav.eessi.pensjon.services.storage.StorageService
//import no.nav.eessi.pensjon.services.whitelist.WhitelistService
//import no.nav.melding.virksomhet.varsel.v1.varsel.*
//import no.nav.security.oidc.api.Protected
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.jms.core.JmsTemplate
//import org.springframework.stereotype.Component
//import java.io.StringWriter
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import javax.xml.bind.JAXBContext
//import javax.xml.bind.JAXBElement
//
//private val logger = LoggerFactory.getLogger(VarselService::class.java)
//
//@Protected
//@Component
//class VarselService(val wmqJmsTemplate: JmsTemplate,
//                    val storageService: StorageService,
//                    val navRegistreService: NavRegistreOppslagService,
//                    val whitelistService: WhitelistService,
//                    val javaTimeObjectMapper: ObjectMapper,
//                    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(
//                        SimpleMeterRegistry()))  {
//
//    @Value("\${VARSELPRODUKSJON.VARSLINGER.queuename}")
//    lateinit var varselQueue: String
//
//    @Value("\${eessi.pensjon.frontend.ui.url}")
//    lateinit var frontendUrl: String
//
//    @Value("\${eessi.pensjon.frontend.api.varsel.tittel}")
//    lateinit var varselTittel: String
//
//    fun sendVarsel(personIdentifikator: String, saksId: String, varseltype: String) {
//
//        val varsel = ObjectFactory().createVarsel(
//                Varsel().apply {
//                    this.mottaker = PersonIdent().apply {
//                        this.personIdent = personIdentifikator
//                    }
//                    this.varslingstype = Varslingstyper().apply {
//                        this.value = varseltype
//                    }
//                    this.parameterListe.add(
//                            Parameter().apply {
//                                this.key = "url"
//                                this.value = frontendUrl
//                            }
//                    )
//                })
//
//        val now = LocalDateTime.now()
//
//         metricsHelper.measure("varsel_sendt_til_ko") {
//             try {
//                val marshalledVarsel = marshal(varsel)
//                logger.info("Legger varselmelding på kø")
//                wmqJmsTemplate.convertAndSend(varselQueue, marshalledVarsel)
//             } catch (e: Exception) {
//                throw VarselServiceException("Kunne ikke sende varselet til varseltjenesten: ${e.message}")
//            }
//        }
//        storeVarsel(now, personIdentifikator, saksId, varseltype)
//        whitelistService.addToWhitelist(personIdentifikator)
//
//    }
//
//    private fun storeVarsel(now: LocalDateTime, aktoerId: String, saksId: String, varseltype: String) {
//        logger.info(("Lagrer varsel i S3"))
//        val personNavn = navRegistreService.hentPersoninformasjonNavn(aktoerId)
//        val sendtVarsel = SendtVarsel(varselTittel, personNavn, now, varseltype)
//        val varselFilename = now.format(DateTimeFormatter.ISO_DATE_TIME)
//        storageService.put("${aktoerId}___varsler___${saksId}___$varselFilename.json",
//                javaTimeObjectMapper.writeValueAsString(sendtVarsel))
//    }
//
//    private fun marshal(varsel: JAXBElement<Varsel>): String {
//        val sw = StringWriter()
//        val jaxbContext = JAXBContext.newInstance(Varsel::class.java)
//        val marshaller = jaxbContext.createMarshaller()
//        marshaller.marshal(varsel, sw)
//        return sw.toString()
//    }
//}
