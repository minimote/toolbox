/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "cn.minimote.toolbox"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.minimote.toolbox"
        minSdk = 26
        targetSdk = 36

        var testSuffix = ""

        // 测试的后缀
        testSuffix = "-alpha2"
        // 版本号
        val versionCodeList = listOf(2, 0, 0)

        var versionCodeCombined = 0
        for(x in versionCodeList) {
            // 每位的取值范围是 0 ~ 99
            versionCodeCombined *= 100
            versionCodeCombined += x
        }

        versionCode = versionCodeCombined

//        versionName = "1.0"
        // 使用当前时间动态设置版本名
        val formatter = DateTimeFormatter.ofPattern("yyMMdd.HHmmss")
        val dateTime = LocalDateTime.now().format(formatter)
        versionName = versionCodeList.joinToString(".") + "-" + dateTime + testSuffix
//        versionName = "1.0.1-$dateTime"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }

        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug" // 可选：添加包名后缀
            // 使用当前时间动态设置版本名
//            val formatter = DateTimeFormatter.ofPattern("yyMMdd.HHmmss")
//            val dateTime = LocalDateTime.now().format(formatter)
//            versionNameSuffix = "-$dateTime-debug"   // 可选：添加版本后缀
            versionNameSuffix = "-debug"   // 可选：添加版本后缀
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
//    afterEvaluate {
//        tasks.named("assembleRelease") {
//            finalizedBy("copyAndRenameApkTask")
//        }
//    }
    android.applicationVariants.all {
        val config = project.android.defaultConfig
        val versionName = config.versionName
//        val formatter = DateTimeFormatter.ofPattern("yyyy_MMdd_HHmmss")
//        val createTime = LocalDateTime.now().format(formatter)
        val appName = "toolbox"
//        val filename = "${appName}_${buildType.name}_${versionName}_${createTime}.apk"
//        val filename = "${appName}_${buildType.name}_${versionName}.apk"
        val filename = "${appName}_${versionName}.apk"
        outputs.all {
            // 判断是否是输出 apk 类型
            if(this is com.android.build.gradle.internal.api.ApkVariantOutputImpl
            ) {
                this.outputFileName = filename
            }
        }
        // 保存 release 版本的 apk
        if(buildType.name == "release") {
            // 也可以用 assembleProvider.configure {
            assembleProvider.get().doLast {
                // 可以这样过滤 if (name == "assembleRelease")
                project.copy {
                    val fromDir =
                        packageApplicationProvider.get().outputDirectory.asFile.get().absolutePath
                    val outDir = File(project.rootDir, "apks")
                    outDir.mkdirs()
                    from(fromDir) {
                        include("**/*.apk")
                    }
                    into(outDir)
                    println("> My Task :copy from $fromDir into $outDir")
//                    rename { _ -> filename }
                }
            }
        }
    }
}


//gradlew copyAndRenameApkTask
//val copyAndRenameApkTask by tasks.registering(Copy::class) {
//    dependsOn("assembleRelease") // 确保 app-release.apk 已经生成
//
//    val config = project.android.defaultConfig
//    val versionName = config.versionName
//    val formatter = DateTimeFormatter.ofPattern("yyyy_MMdd_HHmm")
//    val createTime = LocalDateTime.now().format(formatter)
//    val appName = "toolbox"
//    val destDir = File(
//        rootDir,
//        "app/build/outputs/apk/release"
//    )
//    // 确保目标目录存在
//    doFirst {
//        destDir.mkdirs()
//    }
//    from(destDir) {
//        include("app-release.apk")
//    }
//    into(destDir)
////    rename { _ -> "${appName}_${versionName}_${createTime}.apk" }
//    rename { _ -> "${appName}_${versionName}.apk" }
//}

dependencies {
//    // 引入 libs 目录中的所有 AAR 文件
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
//    // 单独引入 heytap-widget-v7.0.18.aar 文件
//    implementation(files("libs/heytap-widget-v7.0.18.aar"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.customview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.gson)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hilt.android)
    implementation(libs.androidx.room.ktx)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.viewpager2)
    implementation(libs.google.flexbox)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.work.runtime.ktx)

    // 越界回弹
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")
}
kapt {
    correctErrorTypes = true
}
