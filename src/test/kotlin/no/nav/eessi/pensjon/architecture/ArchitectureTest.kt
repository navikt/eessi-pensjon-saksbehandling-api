package no.nav.eessi.pensjon.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import no.nav.eessi.pensjon.Application
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class ArchitectureTest {

    companion object {

        @JvmStatic
        private val root = Application::class.qualifiedName!!
            .replace("." + Application::class.simpleName, "")

        @JvmStatic
        lateinit var classesToAnalyze: JavaClasses

        @BeforeAll
        @JvmStatic
        fun `extract classes`() {
            classesToAnalyze = ClassFileImporter().withImportOption(ImportOption.DoNotIncludeTests()).importPackages(root)
            assertTrue(classesToAnalyze.size > 100, "Sanity check on no. of classes to analyze")
            assertTrue(classesToAnalyze.size < 800, "Sanity check on no. of classes to analyze")
        }
    }

    @Test
    fun `Packages should not have cyclic depenedencies`() {
        slices().matching("$root.(*)..").should().beFreeOfCycles().check(classesToAnalyze)
        slices().matching("$root..(*)").should().beFreeOfCycles().check(classesToAnalyze)
    }

    @Test
    fun `Check architecture`() {
        Architectures.layeredArchitecture()
                .layer("App").definedBy("$root")
                .layer("API").definedBy("$root.api..", "$root.personoppslag..")
                .layer("Eux").definedBy("$root.eux..")
                .layer("Websockets").definedBy("$root.websocket..")
                .layer("Listeners").definedBy("$root.listeners..")
                .layer("Health").definedBy("$root.health..")
                .layer("Metrics").definedBy("$root.metrics..")
                .layer("Services").definedBy("$root.services..", "$root.personoppslag..")
                .layer("Config").definedBy("$root.config..")
                .layer("Security").definedBy("$root.security..")
                .layer("Interceptor").definedBy("$root.interceptor..")
                .layer("Logging").definedBy("$root.logging..")
                .layer("Utils").definedBy("$root.utils..")
                .whereLayer("App").mayNotBeAccessedByAnyLayer()
                .whereLayer("API").mayNotBeAccessedByAnyLayer()
                .whereLayer("Health").mayNotBeAccessedByAnyLayer()
                .whereLayer("Listeners").mayNotBeAccessedByAnyLayer()
                .whereLayer("Websockets").mayOnlyBeAccessedByLayers("Listeners")
                .whereLayer("Services").mayOnlyBeAccessedByLayers("API", "Interceptor")
                .whereLayer("Config").mayOnlyBeAccessedByLayers("API")
                .whereLayer("Security").mayOnlyBeAccessedByLayers("Services", "Config", "Eux")
                .whereLayer("Interceptor").mayOnlyBeAccessedByLayers("Config", "Logging")
                .whereLayer("Logging").mayOnlyBeAccessedByLayers("API", "Config", "Interceptor", "Security", "Eux")
                //Verify rules
                .check(classesToAnalyze)
    }
}
