pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Network fallback for regions where the official repositories are unavailable.
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Network fallback for regions where the official repositories are unavailable.
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
    }
}

rootProject.name = "PocketLedger"
include(":app")
