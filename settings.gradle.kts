pluginManagement {
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
        // Pleos Connect SDK. 다른 라이브러리를 이 저장소에서 받지 않도록 그룹을 제한한다.
        maven {
            url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/")
            content { includeGroup("ai.pleos.playground") }
        }
    }
}

rootProject.name = "AlterCharge"
include(":app")
