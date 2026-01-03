# 📒 个人记账 App (Bookkeeping)

这是一个基于 Android 原生开发的个人记账应用，使用最新的 **Kotlin** 和 **Jetpack Compose** 技术栈构建，界面现代简洁，并集成 **Firebase** 实现云端数据同步。

## ✨ 功能特点

- **云端同步**：通过 Firebase Authentication 和 Firestore 实现数据实时云同步，多设备无缝切换。
- **账单记录**：支持快速记录收入和支出，自定义金额、类别和备注。
- **分类管理**：内置多种常用分类（如餐饮、交通、购物等），支持自定义添加和管理。
- **统计图表**：提供直观的统计报表，按月查看收支情况，帮助你更好地掌握财务状况。
- **预算设置**：支持设置每月预算，实时监控消费进度（开发中）。
- **完全免费**：利用 Firebase 免费套餐，个人使用无额外成本。

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose (Material Design 3)
- **架构**: MVVM (Model-View-ViewModel)
- **云服务 (Firebase)**:
    - **Authentication**: 用户注册与登录
    - **Firestore**: NoSQL 云数据库
- **构建工具**: Gradle (Kotlin DSL)

## 🚀 快速开始

### 前置要求
- Android Studio Ladybug 或更高版本
- JDK 17+
- 配置好的 Firebase 项目 (`google-services.json`)

### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/zhoudaniu1973-svg/bookkeeping.git
   ```

2. **配置 Firebase**
   - 在 Firebase 控制台创建项目。
   - 启用 **Authentication** (邮箱/密码登录)。
   - 启用 **Firestore Database**。
   - 下载 `google-services.json` 文件并放入 `app/` 目录下。

3. **构建运行**
   使用 Android Studio 打开项目，同步 Gradle，连接模拟器或真机即可运行。

## 📄 许可证

本项目仅供学习和个人使用。
