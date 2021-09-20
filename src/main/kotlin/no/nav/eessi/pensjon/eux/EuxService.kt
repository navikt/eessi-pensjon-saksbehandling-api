package no.nav.eessi.pensjon.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.eux.model.buc.Institusjon
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class EuxService(
    private val klient: EuxKlient,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {
    private lateinit var hentInstitusjoner: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {    hentInstitusjoner = metricsHelper.init("hentInstitusjoner", alert = MetricsHelper.Toggle.OFF)
    }

    /**
     * Henter alle institusjoner på BUC
     *
     * @param bucType: BucTypen man vil hente tilhørende institusjoner fra.
     * @param landkode: Hvilket land institusjonene tilhører.
     *
     * @return Liste over institusjoner [Institusjon]
     */
    fun hentInstitusjoner(bucType: BucType, landkode: String = ""): List<Institusjon> {
        return hentInstitusjoner.measure {
            klient.hentInstitusjoner(bucType, landkode)
        }
    }

}
