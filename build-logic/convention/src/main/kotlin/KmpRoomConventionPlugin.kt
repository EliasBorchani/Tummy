import androidx.room.gradle.RoomExtension
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.apply

/**
 * Room KMP convention:
 * - KSP2 on android + the 3 iOS targets
 * - sqlite-bundled injected into commonMain (BundledSQLiteDriver)
 * - local schemaDirectory so migrations are tracked
 * Apply alongside tummy.kmp.library.
 */
class KmpRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("androidx.room")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.findLibrary("androidx-room-runtime").get())
                    implementation(libs.findLibrary("androidx-sqlite-bundled").get())
                }
            }

            dependencies {
                val roomCompiler = libs.findLibrary("androidx-room-compiler").get()
                add("kspAndroid", roomCompiler)
                add("kspIosX64", roomCompiler)
                add("kspIosArm64", roomCompiler)
                add("kspIosSimulatorArm64", roomCompiler)
            }
        }
    }
}
