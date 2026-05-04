import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention for a :features:<name> module.
 * 100% native UI: no Compose in the module, only VM + state + intent + event.
 * - Applies the base KMP convention
 * - Adds lifecycle-viewmodel + koin-core to commonMain
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        return with(target) {
            pluginManager.apply("tummy.kmp.library")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
                    implementation(libs.findLibrary("koin-core").get())
                }
            }
        }
    }
}
