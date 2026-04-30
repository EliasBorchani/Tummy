plugins {
    alias(libs.plugins.tummy.kmp.feature)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.utilities.presentation)
        implementation(projects.domain.meal.api)
        implementation(projects.domain.user.api)
        implementation(projects.domain.nutrition.api)
    }
}
