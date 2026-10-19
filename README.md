# 学习引擎（Android 测试版）

这是一个原生 Android/Kotlin 项目，包含学习 XP/等级、专注计时、学习任务奖励，以及通过 Android 无障碍服务实现的基础应用拦截原型。

## 功能
- 25 / 45 / 60 分钟专注计时
- 完成知识点、练习题、错题订正和 3 分钟重启任务后领取 XP
- 每 100 XP 升一级
- 选择要在专注期间限制的已安装应用
- 开启无障碍服务后，专注期间尝试打开所选应用时返回学习引擎
- GitHub Actions 云端编译 APK

## 手机安装
在 GitHub 仓库中上传整个项目目录的内容（包括隐藏的 `.github` 目录）。打开 Actions，选择 `Build Android APK`，点击 `Run workflow`。成功后在该次运行页面的 Artifacts 中下载 `xuexi-engine-debug-apk`，解压得到 `app-debug.apk`。

## 构建配置
- Android Gradle Plugin 8.7.3
- Gradle 8.9（由 GitHub Actions 环境安装）
- Kotlin Android 2.0.21
- Java 17
- compileSdk 35 / minSdk 26

## 重要说明
这是早期测试工程，尚未在真实设备上验证。无障碍服务必须由用户在系统设置中主动开启；不同手机厂商的后台限制可能影响效果。请先只测试非关键应用，不要限制电话、紧急联系或学习资料应用。应用拦截是软拦截，不能保证抵抗强制停止、关闭无障碍权限或重启等情况。

此项目使用 `QUERY_ALL_PACKAGES` 枚举本机可启动应用，仅用于用户自行选择本地应用。若计划发布到 Google Play，需要另行核对其应用可见性和无障碍 API 政策。
