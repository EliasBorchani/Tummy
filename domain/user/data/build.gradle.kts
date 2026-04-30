plugins {
    alias(libs.plugins.tummy.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.domain.user.api)
        implementation(projects.utilities.kotlinExt)
        implementation(libs.koin.core)
    }
}
