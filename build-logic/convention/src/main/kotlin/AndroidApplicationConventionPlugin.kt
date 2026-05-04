import com.android.build.api.dsl.ApplicationExtension
import ext.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import kotlin.apply

/**
 * Convention for :composeApp — pure Android app (not KMP).
 * In AGP 9+, `com.android.application` natively integrates Kotlin compilation;
 * we additionally apply `org.jetbrains.kotlin.plugin.compose` for the Compose DSL.
 * No `kotlin.multiplatform` plugin here — would conflict with the extension setup.
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
