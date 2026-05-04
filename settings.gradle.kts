pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Tummy"

// Apps
include(":composeApp")

// Umbrella iOS
include(":umbrella")

// Utilities
include(":utilities:kotlin-ext")
include(":utilities:network")
include(":utilities:presentation")

// Core
include(":core:ui-tokens")

// Domain — api module + data module
include(":domain:ingredients:api")
include(":domain:ingredients:data")
include(":domain:symptoms:api")
include(":domain:symptoms:data")

// Features (VM + state, commonMain only)
include(":features:home")
include(":features:log")
