plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
    compileOnly(libs.moko.resources.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "tummy.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpFeature") {
            id = "tummy.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("kmpRoom") {
            id = "tummy.kmp.room"
            implementationClass = "KmpRoomConventionPlugin"
        }
        register("kmpMoko") {
            id = "tummy.kmp.moko"
            implementationClass = "KmpMokoConventionPlugin"
        }
        register("androidApp") {
            id = "tummy.android.app"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
