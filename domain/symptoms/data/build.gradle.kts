plugins {
    alias(libs.plugins.tummy.kmp.library)
    alias(libs.plugins.tummy.kmp.room)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.domain.symptoms.api)
        implementation(projects.utilities.kotlinExt)
        implementation(libs.kotlinx.datetime)
        implementation(libs.koin.core)
    }
}
