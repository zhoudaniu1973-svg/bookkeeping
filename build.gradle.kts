// 个人记账应用 - 项目级 Gradle 构建文件
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.compose.compiler) apply false
    // Google 服务插件（用于 Firebase）
    alias(libs.plugins.googleServices) apply false
}
