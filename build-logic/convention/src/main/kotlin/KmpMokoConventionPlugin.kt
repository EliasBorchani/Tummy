import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.apply
import kotlin.text.set

/**
 * MOKO Resources convention:
 * - resourcesPackage = "com.tummy.tokens.resources"
 * - resourcesClassName = "MR"
 * - base `resources` (not `resources-compose`, since the UI is 100% native)
 */
class KmpMokoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.icerock.mobile.multiplatform-resources")

            extensions.configure<MultiplatformResourcesPluginExtension> {
                resourcesPackage.set("com.tummy.tokens.resources")
                resourcesClassName.set("MR")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    api(libs.findLibrary("moko-resources").get())
                }
            }
        }
    }
}
