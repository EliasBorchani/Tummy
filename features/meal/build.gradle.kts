plugins {
    alias(libs.plugins.tummy.kmp.feature)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.utilities.presentation)
        implementation(projects.utilities.kotlinExt)
        implementation(projects.domain.meal.api)
        implementation(libs.kotlinx.datetime)
    }
}
