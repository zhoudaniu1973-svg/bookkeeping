# Bookkeeping Web

这是这个记账项目的网页端，面向桌面浏览器和手机浏览器使用。登录后会直接连接 Firebase Authentication 和 Firestore，与 Android App 共享同一套用户和数据。

线上地址：

- [https://angular-expanse-235611.web.app](https://angular-expanse-235611.web.app)

## 已实现功能

- 邮箱注册、登录、退出、重置密码
- 账单新增、编辑、删除
- 收入 / 支出分类管理
- 月度账单列表
- 月度 / 年度统计
- 周期账单管理
- 到期账单自动生成
- JSON 导入导出
- CSV 导出
- 手机端底部导航和底部工具抽屉

## 主要文件

- [index.html](index.html)
- [app.js](app.js)
- [styles.css](styles.css)
- [firebase-service.js](firebase-service.js)
- [firebase-config.js](firebase-config.js)

## 本地运行

```bash
cd web
python -m http.server 8000
```

访问：

- [http://localhost:8000](http://localhost:8000)

## Firebase 要求

确保 Firebase 控制台中已完成以下设置：

1. 创建 Web App
2. 把 Web 配置写入 [firebase-config.js](firebase-config.js)
3. 开启 `Authentication -> Sign-in method -> Email/Password`
4. 在 `Authorized domains` 中加入：
   - `localhost`
   - `angular-expanse-235611.web.app`
   - `angular-expanse-235611.firebaseapp.com`

## 发布

仓库根目录执行：

```bash
npx firebase-tools deploy --only hosting
```

相关配置文件：

- [../firebase.json](../firebase.json)
- [../.firebaserc](../.firebaserc)

## 说明

- `firebase-config.js` 中的 Web 配置属于前端公开配置，不是 Admin SDK 私钥
- 本地模式和 Firebase 模式都保留在代码里，但当前仓库默认已经启用 Firebase Web 配置
- 静态资源在 Hosting 上使用了禁止缓存头，便于手机端尽快拿到最新界面
