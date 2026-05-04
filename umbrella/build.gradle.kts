plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.skie)
}

kotlin {
    val kmpAndroid = extensions.getByType(com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension::class.java)
    kmpAndroid.namespace = "com.tummy.umbrella"
    kmpAndroid.compileSdk = libs.versions.compileSdk.get().toInt()
    kmpAndroid.minSdk = libs.versions.minSdk.get().toInt()

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "TummyShared"
            isStatic = true

            // Exported modules
            export(projects.utilities.presentation)
            export(projects.utilities.kotlinExt)
            export(projects.core.uiTokens)
            export(projects.domain.ingredients.api)
            export(projects.domain.symptoms.api)
            export(projects.features.home)
            export(projects.features.log)
            export(libs.kotlinx.datetime)
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(projects.utilities.presentation)
            api(projects.utilities.kotlinExt)
            api(projects.core.uiTokens)
            api(projects.domain.ingredients.api)
            api(projects.domain.symptoms.api)
            api(projects.features.home)
            api(projects.features.log)
            api(libs.kotlinx.datetime)

            // Implémentations injectées dans Koin
            implementation(projects.domain.ingredients.data)
            implementation(projects.domain.symptoms.data)
            implementation(projects.utilities.network)

            implementation(libs.koin.core)
        }
    }
}

skie {
    analytics {
        disableUpload.set(true)
    }
}
