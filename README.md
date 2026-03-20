# Bookkeeping

一个同时包含 Android App 和 Web 版的个人记账项目。两端共用同一个 Firebase 项目，登录后可以同步账单、分类、统计和周期账单数据。

当前线上地址：

- [https://angular-expanse-235611.web.app](https://angular-expanse-235611.web.app)

## 当前能力

- Android 端基于 Kotlin + Jetpack Compose
- Web 端支持手机浏览器记账和浏览
- Firebase Authentication 邮箱密码登录
- Firestore 云端同步
- 账单新增、编辑、删除
- 收入 / 支出分类管理
- 月度 / 年度统计
- 周期账单与到期账单生成
- JSON 导入导出
- CSV 导出

## 项目结构

```text
app/                Android 客户端
web/                Web 客户端静态文件
firebase.json       Firebase Hosting 配置
.firebaserc         Firebase 项目绑定
```

## Android 本地运行

前提：

- Android Studio
- JDK 17+
- `app/google-services.json`

步骤：

```bash
git clone https://github.com/zhoudaniu1973-svg/bookkeeping.git
cd bookkeeping
```

用 Android Studio 打开项目后同步 Gradle，选择模拟器或真机运行即可。

## Web 本地运行

在仓库根目录执行：

```bash
cd web
python -m http.server 8000
```

然后打开：

- [http://localhost:8000](http://localhost:8000)

如果要测试和 App 同步，请先完成 Firebase Web 配置。

## Firebase 配置

这个项目当前绑定的 Firebase 项目是：

- `projectId`: `angular-expanse-235611`

Web 端配置文件：

- [web/firebase-config.js](web/firebase-config.js)
- [web/firebase-config.example.js](web/firebase-config.example.js)

需要确认：

1. Firebase Authentication 已开启“电子邮件/密码”
2. `Authorized domains` 包含 `localhost`
3. 如果使用 Hosting 域名，授权域名里也包含：
   - `angular-expanse-235611.web.app`
   - `angular-expanse-235611.firebaseapp.com`

## 部署 Web 到 Firebase Hosting

首次使用先登录 Firebase CLI：

```bash
npx firebase-tools login
```

部署命令：

```bash
npx firebase-tools deploy --only hosting
```

当前 Hosting 配置见：

- [firebase.json](firebase.json)
- [.firebaserc](.firebaserc)

## Web 端说明

Web 端的详细使用说明见：

- [web/README.md](web/README.md)
