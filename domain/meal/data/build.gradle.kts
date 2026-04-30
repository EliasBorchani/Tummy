plugins {
    alias(libs.plugins.tummy.kmp.library)
    alias(libs.plugins.tummy.kmp.room)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain.meal.api)
            implementation(projects.utilities.kotlinExt)
            implementation(projects.utilities.network)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
    }
}
