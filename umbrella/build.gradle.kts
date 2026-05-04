plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.skie)
}

kotlin {
    // AGP 9 : config Android via kotlin { android { } } et l'extension dédiée.
    val kmpAndroid = extensions.getByType(com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension::class.java)
    kmpAndroid.namespace = "com.tummy.umbrella"
    kmpAndroid.compileSdk = libs.versions.compileSdk.get().toInt()
    kmpAndroid.minSdk = libs.versions.minSdk.get().toInt()

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "TummyShared"
            isStatic = true

            // Exports : tout ce que Swift doit voir directement (types, VM, tokens...)
            export(projects.utilities.presentation)
            export(projects.utilities.kotlinExt)
            export(projects.core.uiTokens)
            export(projects.domain.meal.api)
            export(projects.domain.user.api)
            export(projects.domain.nutrition.api)
            export(projects.domain.ingredients.api)
            export(projects.domain.symptoms.api)
            export(projects.features.meal)
            export(projects.features.diary)
            export(projects.features.settings)
            export(projects.features.home)
            export(projects.features.log)
            export(libs.kotlinx.datetime)
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // api pour que les exports ci-dessus fonctionnent
            api(projects.utilities.presentation)
            api(projects.utilities.kotlinExt)
            api(projects.core.uiTokens)
            api(projects.domain.meal.api)
            api(projects.domain.user.api)
            api(projects.domain.nutrition.api)
            api(projects.domain.ingredients.api)
            api(projects.domain.symptoms.api)
            api(projects.features.meal)
            api(projects.features.diary)
            api(projects.features.settings)
            api(projects.features.home)
            api(projects.features.log)
            api(libs.kotlinx.datetime)

            // Implémentations injectées dans Koin
            implementation(projects.domain.meal.data)
            implementation(projects.domain.user.data)
            implementation(projects.domain.nutrition.data)
            implementation(projects.domain.ingredients.data)
            implementation(projects.domain.symptoms.data)
            implementation(projects.utilities.network)

            implementation(libs.koin.core)
        }
    }
}

skie {
    // Defaults SKIE 0.10.11 couvrent FlowInterop / SuspendInterop / SealedInterop / DefaultArguments.
    analytics {
        disableUpload.set(true)
    }
}
