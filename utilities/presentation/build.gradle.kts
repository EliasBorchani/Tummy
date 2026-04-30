plugins {
    alias(libs.plugins.tummy.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.androidx.lifecycle.viewmodel)
        implementation(projects.utilities.kotlinExt)
    }
}
