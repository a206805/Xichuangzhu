// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false

    // 【新增】Google Services 插件，告知 Gradle 插件版本和来源
    id("com.google.gms.google-services") version "4.4.0" apply false
}