plugins {
    alias(libs.plugins.tummy.android.app)
}

dependencies {
    implementation(projects.umbrella)

    implementation(projects.utilities.presentation)
    implementation(projects.utilities.kotlinExt)
    implementation(projects.utilities.network)
    implementation(projects.core.uiTokens)

    implementation(projects.domain.ingredients.api)
    implementation(projects.domain.ingredients.data)
    implementation(projects.domain.symptoms.api)
    implementation(projects.domain.symptoms.data)

    implementation(projects.features.home)
    implementation(projects.features.log)

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
    implementation(libs.kotlinx.datetime)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.napier)
}
