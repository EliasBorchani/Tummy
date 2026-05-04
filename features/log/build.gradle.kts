plugins {
    alias(libs.plugins.tummy.kmp.feature)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.utilities.presentation)
        implementation(projects.utilities.kotlinExt)
        implementation(projects.domain.ingredients.api)
        implementation(projects.domain.symptoms.api)
        implementation(libs.kotlinx.datetime)
    }
}
