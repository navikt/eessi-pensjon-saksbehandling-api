package no.nav.eessi.pensjon.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import no.nav.eessi.pensjon.Application
import org.junit.Test
import org.junit.BeforeClass
import kotlin.test.assertTrue
import kotlin.test.Ignore

//TODO expand this test class

class ArchitectureTest {

    companion object {

        @JvmStatic
        private val root = Application::class.qualifiedName!!
            .replace("." + Application::class.simpleName, "")

        @JvmStatic
        lateinit var classesToAnalyze: JavaClasses

        @BeforeClass
        @JvmStatic
        fun `extract classes`() {
            classesToAnalyze = ClassFileImporter().importPackages(root)

            assertTrue(classesToAnalyze.size > 100, "Sanity check on no. of classes to analyze")
            assertTrue(classesToAnalyze.size < 800, "Sanity check on no. of classes to analyze")
        }
    }

    @Test
    fun `Packages should not have cyclic depenedencies`() {
        slices().matching("$root.(*)..").should().beFreeOfCycles().check(classesToAnalyze)
    }

    @Test
    fun `Check architecture`() {
        val ROOT = "saksbehandling-api"
        val Config = "saksbehandling-api.config"
        val Interceptor = "saksbehandling-api.interceptor"
        val Aktoerregister = "saksbehandling-api.services.aktoerregister"
        val EUX = "saksbehandling-api.services.eux"
        val Fagmodul = "saksbehandling-api.services.fagmodul"
        val Kafka = "saksbehandling-api.services.kafka"
        val Login = "saksbehandling-api.services.login"
        val PDF = "saksbehandling-api.services.pdf"
        val Storage = "saksbehandling-api.services.storage"
        val Amazons3 = "saksbehandling-api.services.storage.amazons3"
        val STS = "saksbehandling-api.security.sts"
        val Submit = "saksbehandling-api.services.submit"
        val UserInfo = "saksbehandling-api.services.userinfo"
        val Varsel = "saksbehandling-api.services.varsel"
        val WhiteList = "saksbehandling-api.services.whitelist"
        val Utils = "saksbehandling-api.utils"
        val Oldtests = "saksbehandling-api.services.oldtests" //why?

        val packages: Map<String, String> = mapOf(
            ROOT to root,
            Config to "$root.config",
            Interceptor to "$root.interceptor",
            Aktoerregister to "$root.services.aktoerregister",
            EUX to "$root.services.eux",
            Fagmodul to "$root.services.fagmodul",
            Kafka to "$root.services.kafka",
            Login to "$root.services.login",
            PDF to "$root.services.pdf",
            Storage to "$root.services.storage",
            Amazons3 to "$root.services.storage.amazons3",
            STS to "$root.security.sts",
            Submit to "$root.services.submit",
            UserInfo to "$root.services.userinfo",
            Varsel to "$root.services.varsel",
            WhiteList to "$root.services.whitelist",
            Utils to "$root.utils",
            Oldtests to "$root.services.oldtests" //Again why?
        )

        Architectures.layeredArchitecture()
            //Define components
            .layer(ROOT).definedBy(packages[ROOT])
            .layer(Config).definedBy(packages[Config])
            .layer(Interceptor).definedBy(packages[Interceptor])
            .layer(Aktoerregister).definedBy(packages[Aktoerregister])
            .layer(EUX).definedBy(packages[EUX])
            .layer(Fagmodul).definedBy(packages[Fagmodul])
            .layer(Kafka).definedBy(packages[Kafka])
            .layer(Login).definedBy(packages[Login])
            .layer(PDF).definedBy(packages[PDF])
            .layer(Storage).definedBy(packages[Storage])
            .layer(Amazons3).definedBy(packages[Amazons3])
            .layer(STS).definedBy(packages[STS])
            .layer(Submit).definedBy(packages[Submit])
            .layer(UserInfo).definedBy(packages[UserInfo])
            .layer(Varsel).definedBy(packages[Varsel])
            .layer(WhiteList).definedBy(packages[WhiteList])
            .layer(Utils).definedBy(packages[Utils])
            .layer(Oldtests).definedBy(packages[Oldtests])
            //define rules
            .whereLayer(ROOT).mayNotBeAccessedByAnyLayer()
            .whereLayer(Config).mayNotBeAccessedByAnyLayer()
            .whereLayer(Interceptor).mayOnlyBeAccessedByLayers(Config, STS)
            .whereLayer(Aktoerregister).mayOnlyBeAccessedByLayers(Fagmodul, Varsel) // Consider refactor to reduce dependencies
            .whereLayer(EUX).mayOnlyBeAccessedByLayers(Oldtests)
            .whereLayer(Fagmodul).mayOnlyBeAccessedByLayers(PDF, EUX, Varsel) // TODO Refactor to reduce dependencies
            .whereLayer(Kafka).mayOnlyBeAccessedByLayers(Submit)
            .whereLayer(Login).mayNotBeAccessedByAnyLayer()
            .whereLayer(PDF).mayOnlyBeAccessedByLayers(Submit)
            .whereLayer(Storage).mayOnlyBeAccessedByLayers(Amazons3, Submit, UserInfo, Varsel, WhiteList, Oldtests) // TODO Refactor to reduce dependencies
            .whereLayer(Amazons3).mayOnlyBeAccessedByLayers(Storage, Submit, Varsel) // TODO refactor to reduce dependencies
            .whereLayer(STS).mayOnlyBeAccessedByLayers(Config)
            .whereLayer(Submit).mayNotBeAccessedByAnyLayer()
            .whereLayer(UserInfo).mayNotBeAccessedByAnyLayer()
            .whereLayer(Varsel).mayNotBeAccessedByAnyLayer()
            .whereLayer(WhiteList).mayOnlyBeAccessedByLayers(UserInfo, Varsel, Storage) // Consider refactor to reduce dependencies
            .whereLayer(Oldtests).mayNotBeAccessedByAnyLayer()
            //Verify rules
            .check(classesToAnalyze)
    }

}
