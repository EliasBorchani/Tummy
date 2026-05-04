import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import ext.libs
import ext.moduleNamespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.apply

/**
 * Base convention for any KMP library module (AGP 9+).
 *
 * AGP 9 introduces `com.android.kotlin.multiplatform.library`, which merges
 * `com.android.library` and `kotlin.multiplatform`. Android config now goes
 * through `kotlin { android { ... } }` instead of a separate `android {}`
 * block.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                val kmpAndroid = extensions.getByType<KotlinMultiplatformAndroidLibraryExtension>()
                kmpAndroid.namespace = target.moduleNamespace()
                kmpAndroid.compileSdk =
                    libs
                        .findVersion("compileSdk")
                        .get()
                        .toString()
                        .toInt()
                kmpAndroid.minSdk =
                    libs
                        .findVersion("minSdk")
                        .get()
                        .toString()
                        .toInt()

                listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
                    it.binaries.framework {
                        baseName = target.name
                        isStatic = true
                    }
                }
                applyDefaultHierarchyTemplate()

                // Room KMP relies on expect/actual classes (silences the beta warning).
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
}
