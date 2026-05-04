import com.android.build.api.dsl.ApplicationExtension
import ext.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import kotlin.apply

/**
 * Convention pour :composeApp — app Android pure (pas KMP).
 * En AGP 9+, `com.android.application` intègre nativement la compilation Kotlin ;
 * applique `org.jetbrains.kotlin.android` en complément pour bénéficier de la DSL.
 * Pas de `kotlin.multiplatform` sinon conflit d'extension.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                namespace = "com.tummy.android"
                compileSdk =
                    libs
                        .findVersion("compileSdk")
                        .get()
                        .toString()
                        .toInt()
                defaultConfig {
                    applicationId = "com.tummy.android"
                    minSdk =
                        libs
                            .findVersion("minSdk")
                            .get()
                            .toString()
                            .toInt()
                    targetSdk =
                        libs
                            .findVersion("targetSdk")
                            .get()
                            .toString()
                            .toInt()
                    versionCode = 1
                    versionName = "0.1.0"
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                buildFeatures {
                    compose = true
                }
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }
            }
        }
    }
}
