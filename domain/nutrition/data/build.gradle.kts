plugins {
    alias(libs.plugins.tummy.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.domain.nutrition.api)
        implementation(projects.utilities.kotlinExt)
        implementation(libs.koin.core)
    }
}
