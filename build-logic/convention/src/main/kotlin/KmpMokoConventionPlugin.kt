import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention MOKO Resources :
 * - resourcesPackage nommé depuis le module
 * - resourcesClassName = "MR"
 * - resources-core en commonMain (pas resources-compose : UI native 100%)
 */
class KmpMokoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
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
