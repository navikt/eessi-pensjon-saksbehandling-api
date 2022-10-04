package no.nav.eessi.pensjon.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import no.nav.eessi.pensjon.Application
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

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
            classesToAnalyze = ClassFileImporter()
                .withImportOptions(listOf(
                    ImportOption.DoNotIncludeTests(),
                    ImportOption.DoNotIncludeJars())
                ).importPackages(root)

            assertTrue(classesToAnalyze.size in 10..300, "Sanity check on no. of classes to analyze (is ${classesToAnalyze.size})")
        }
    }

    @Test
    fun `Packages should not have cyclic depenedencies`() {
        slices().matching("$root.(*)..").should().beFreeOfCycles().check(classesToAnalyze)
        slices().matching("$root..(*)").should().beFreeOfCycles().check(classesToAnalyze)
    }

    @Test
    fun `Check architecture`() {
        layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage(root)
                .layer("App").definedBy("$root")
                .layer("API").definedBy("$root.api..", "$root.personoppslag..")
                .layer("Listeners").definedBy("$root.listeners..")
                .layer("Health").definedBy("$root.health..")
                .layer("Services").definedBy("$root.services..", "$root.personoppslag..")
                .layer("Config").definedBy("$root.config..")
                .layer("Interceptor").definedBy("$root.interceptor..")
                .layer("Utils").definedBy("$root.utils..")
                .whereLayer("App").mayNotBeAccessedByAnyLayer()
                .whereLayer("API").mayNotBeAccessedByAnyLayer()
                .whereLayer("Health").mayNotBeAccessedByAnyLayer()
                .whereLayer("Listeners").mayNotBeAccessedByAnyLayer()
                .whereLayer("Services").mayOnlyBeAccessedByLayers("API", "Interceptor")
                .whereLayer("Config").mayOnlyBeAccessedByLayers("API")

                //Verify rules
                .check(classesToAnalyze)
    }
}
