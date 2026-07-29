pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ForgeFlow"
include(":app")
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:database")
include(":core:data")
include(":core:navigation")
include(":core:testing")
include(":feature:home")
include(":feature:exercises")
include(":feature:routines")
include(":feature:workout")
include(":feature:history")
include(":feature:settings")
