/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

pluginManagement {
    repositories {
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/google") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/jcenter") }
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
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/google") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/jcenter") }


        google()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
    }
}


rootProject.name = "工具箱"
include(":app")

