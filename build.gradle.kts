// 个人记账应用 - 项目级 Gradle 构建文件
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    // Google 服务插件（用于 Firebase）
    id("com.google.gms.google-services") version "4.4.0" apply false
}
