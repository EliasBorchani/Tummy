import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention pour un module :features:<name>.
 * UI native 100% : pas de Compose dans le module, uniquement VM + state + intent + event.
 * - Applique le convention KMP de base
 * - Injecte lifecycle-viewmodel + koin-core en commonMain
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
