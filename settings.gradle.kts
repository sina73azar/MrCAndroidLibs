pluginManagement {
    repositories {
        maven("https://maven.myket.ir")
        maven {
            url = uri("http://swd.daneshrefah.ir/artifactory/Android-virtual_maven-repo/")
            isAllowInsecureProtocol = true
            credentials {
                username = "android-developer"
                password = "Dsa@1234"
            }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        // میرورها
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://gradle.jamko.ir") }
        maven { url = uri("https://en-mirror.ir") }
        maven { url = uri("https://google403.ir") }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.myket.ir")
        google()
        mavenCentral()

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

        maven {
            url = uri("http://swd.daneshrefah.ir/artifactory/Android-virtual_maven-repo/")
            isAllowInsecureProtocol = true
            credentials {
                username = "android-developer"
                password = "Dsa@1234"
            }
        }

    }
}

rootProject.name = "MrCAndroidLibs"
include(":app")
include(":network-logger-core")
include(":network-logger-ui")
