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

// Domain — un module api + un module data par bounded context
include(":domain:meal:api")
include(":domain:meal:data")
include(":domain:user:api")
include(":domain:user:data")
include(":domain:nutrition:api")
include(":domain:nutrition:data")

// Features (VM + state, commonMain only)
include(":features:meal")
include(":features:diary")
include(":features:settings")
