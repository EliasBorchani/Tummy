plugins {
    alias(libs.plugins.tummy.android.app)
}

// composeApp est une app Android pure — layout AGP standard (src/main/…).
// Toute la config (namespace, compileSdk, minSdk, buildFeatures.compose) vit
// dans le convention plugin tummy.android.app.

dependencies {
    implementation(projects.umbrella)

    implementation(projects.utilities.presentation)
    implementation(projects.utilities.kotlinExt)
    implementation(projects.utilities.network)
    implementation(projects.core.uiTokens)

    implementation(projects.domain.meal.api)
    implementation(projects.domain.meal.data)
    implementation(projects.domain.user.api)
    implementation(projects.domain.user.data)
    implementation(projects.domain.nutrition.api)
    implementation(projects.domain.nutrition.data)

    implementation(projects.features.meal)
    implementation(projects.features.diary)
    implementation(projects.features.settings)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.napier)
}
