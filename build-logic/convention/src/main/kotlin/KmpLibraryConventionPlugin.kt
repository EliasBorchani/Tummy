import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import ext.libs
import ext.moduleNamespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention de base pour tout module KMP library (AGP 9+).
 *
 * AGP 9 introduit `com.android.kotlin.multiplatform.library` qui fusionne
 * `com.android.library` + `kotlin.multiplatform`. La config Android passe
 * désormais via `kotlin { android { ... } }` au lieu d'un bloc `android {}`
 * séparé.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            // Config Android via l'extension exposée par android-kmp-library.
            val kmpAndroid = extensions.getByType<KotlinMultiplatformAndroidLibraryExtension>()
            kmpAndroid.namespace = target.moduleNamespace()
            kmpAndroid.compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
            kmpAndroid.minSdk = libs.findVersion("minSdk").get().toString().toInt()

            listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
                it.binaries.framework {
                    baseName = target.name
                    isStatic = true
                }
            }
            applyDefaultHierarchyTemplate()

            // Room KMP utilise expect/actual classes — feature encore Beta côté
            // Kotlin mais stable d'usage. Opt-in pour silencer les warnings.
            targets.configureEach {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            freeCompilerArgs.add("-Xexpect-actual-classes")
                        }
                    }
                }
            }

            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            }
        }
    }
}
