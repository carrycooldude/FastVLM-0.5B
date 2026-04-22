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

rootProject.name = "FastVLM0.5B"
include(":app")

// // AI packs for AOT models only
// include(":ai_packs:my_model")

// NPU runtime library modules
include(":litert_npu_runtime_libraries:runtime_strings")
include(":litert_npu_runtime_libraries:qualcomm_runtime_v79")
 