pluginManagement {
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GymBro"

include(":app")

// Core modules
include(":core:common")
include(":core:designsystem")
include(":core:navigation")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:location")
include(":core:notifications")
include(":core:domain")

// Feature modules
include(":feature:auth")
include(":feature:onboarding")
include(":feature:map")
include(":feature:place")
include(":feature:workout")
include(":feature:people")
include(":feature:chat")
include(":feature:profile")