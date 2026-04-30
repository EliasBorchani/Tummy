plugins {
    alias(libs.plugins.tummy.kmp.library)
    alias(libs.plugins.tummy.kmp.moko)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.utilities.kotlinExt)
    }
}
