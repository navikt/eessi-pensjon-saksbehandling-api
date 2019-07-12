package no.nav.eessi.fagmodul.frontend.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import no.nav.eessi.fagmodul.frontend.Application
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
    @Ignore("Test is disabled pending architecture cleanup") //TODO fix the cyclic dependencies then re-enable
    fun `Sub-Packages should not have cyclic dependencies`() {
        slices().matching("..$root.(**)").should().notDependOnEachOther().check(classesToAnalyze)
    }

}