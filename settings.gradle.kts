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
        maven("https://maven.myket.ir")

        // میرورها
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://gradle.jamko.ir") }
        maven { url = uri("https://en-mirror.ir") }
        maven { url = uri("https://google403.ir") }

        val internalRepoUrl = providers.gradleProperty("internalRepoUrl").orNull
        val internalRepoUser = providers.gradleProperty("internalRepoUser").orNull
        val internalRepoPassword = providers.gradleProperty("internalRepoPassword").orNull
        if (!internalRepoUrl.isNullOrBlank() && !internalRepoUser.isNullOrBlank() && !internalRepoPassword.isNullOrBlank()) {
            maven {
                url = uri(internalRepoUrl)
                isAllowInsecureProtocol = internalRepoUrl.startsWith("http://")
                credentials {
                    username = internalRepoUser
                    password = internalRepoPassword
                }
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.myket.ir")

        // میرورها
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://gradle.jamko.ir") }
        maven { url = uri("https://en-mirror.ir") }
        maven { url = uri("https://google403.ir") }

        // اختیاری: مخزن ملی ایران
        maven { url = uri("https://repo.iranrepo.ir/repository/maven-public/") }

        // اختیاری: مخزن Snapshot (برای لایبرری‌هایی که نسخه Snapshot دارند)
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }

        val internalRepoUrl = providers.gradleProperty("internalRepoUrl").orNull
        val internalRepoUser = providers.gradleProperty("internalRepoUser").orNull
        val internalRepoPassword = providers.gradleProperty("internalRepoPassword").orNull
        if (!internalRepoUrl.isNullOrBlank() && !internalRepoUser.isNullOrBlank() && !internalRepoPassword.isNullOrBlank()) {
            maven {
                url = uri(internalRepoUrl)
                isAllowInsecureProtocol = internalRepoUrl.startsWith("http://")
                credentials {
                    username = internalRepoUser
                    password = internalRepoPassword
                }
            }
        }

    }
}

rootProject.name = "MrCAndroidLibs"
include(":app")
include(":network-logger-core")
include(":network-logger-ui")
