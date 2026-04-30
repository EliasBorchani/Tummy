plugins {
    alias(libs.plugins.tummy.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.utilities.kotlinExt)
    }
}
