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
    compileSdk = 35

    defaultConfig {
        applicationId = "cn.minimote.toolbox"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
            versionNameSuffix = "-debug"   // 可选：添加版本后缀
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
//    afterEvaluate {
//        tasks.named("assembleRelease") {
//            finalizedBy("copyAndRenameApkTask")
//        }
//    }
    android.applicationVariants.all {
        val config = project.android.defaultConfig
        val versionName = config.versionName
        val formatter = DateTimeFormatter.ofPattern("yyyy_MMdd_HHmmss")
        val createTime = LocalDateTime.now().format(formatter)
        val appName = "toolbox"
        val filename = "${appName}_${versionName}_${buildType.name}_${createTime}.apk"
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
                    rename { _ -> filename }
                }
            }
        }
    }
}


//gradlew copyAndRenameApkTask
val copyAndRenameApkTask by tasks.registering(Copy::class) {
    dependsOn("assembleRelease") // 确保 app-release.apk 已经生成

    val config = project.android.defaultConfig
    val versionName = config.versionName
    val formatter = DateTimeFormatter.ofPattern("yyyy_MMdd_HHmm")
    val createTime = LocalDateTime.now().format(formatter)
    val appName = "toolbox"
    val destDir = File(
        rootDir,
        "app/build/outputs/apk/release"
    )
    // 确保目标目录存在
    doFirst {
        destDir.mkdirs()
    }
    from(destDir) {
        include("app-release.apk")
    }
    into(destDir)
    rename { _ -> "${appName}_${versionName}_${createTime}.apk" }
}

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
}
kapt {
    correctErrorTypes = true
}
