# 快速开始指南

## 环境要求

### 必需软件
- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **JDK**: 17 或更高版本
- **Android SDK**: API 26 (Android 8.0) 或更高
- **Kotlin**: 1.9.20（已在项目中配置）

### 推荐配置
- **内存**: 8GB RAM 以上
- **硬盘**: 10GB 可用空间
- **操作系统**: Windows 10+, macOS 10.14+, 或 Linux

---

## 第一步：导入项目

### 1. 打开 Android Studio
启动 Android Studio

### 2. 导入项目
```
File → Open → 选择项目根目录 (播放器L2/)
```

### 3. 等待 Gradle 同步
- 首次打开会自动下载依赖
- 需要等待 5-10 分钟（取决于网络速度）
- 确保能访问 Maven Central 和 Google Maven

### 4. 检查 SDK
```
File → Project Structure → SDK Location
```
确保 Android SDK 已正确配置

---

## 第二步：添加图标资源（重要）

### 为什么需要这一步？
项目目前只有图标的 XML 定义，需要添加实际的图标图片文件。

### 快速方案：使用 Android Studio 生成

1. **右键点击 `res` 文件夹**
2. **选择**: `New → Image Asset`
3. **配置**:
   - Asset Type: Launcher Icons (Adaptive and Legacy)
   - Name: ic_launcher
   - Foreground Layer: 选择一个图片或使用 Clipart
   - Background Layer: 选择颜色或图片
4. **点击 Next → Finish**

### 手动方案：添加图片文件

如果有自己的图标，将图片放到以下位置：

```
app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png (48x48)
│   └── ic_launcher_round.png (48x48)
├── mipmap-hdpi/
│   ├── ic_launcher.png (72x72)
│   └── ic_launcher_round.png (72x72)
├── mipmap-xhdpi/
│   ├── ic_launcher.png (96x96)
│   └── ic_launcher_round.png (96x96)
├── mipmap-xxhdpi/
│   ├── ic_launcher.png (144x144)
│   └── ic_launcher_round.png (144x144)
└── mipmap-xxxhdpi/
    ├── ic_launcher.png (192x192)
    └── ic_launcher_round.png (192x192)
```

---

## 第三步：运行应用

### 1. 准备设备

#### 选项 A: 使用真机（推荐）
1. 启用开发者选项
   - 设置 → 关于手机 → 连续点击"版本号" 7 次
2. 启用 USB 调试
   - 设置 → 开发者选项 → USB 调试
3. 用 USB 线连接电脑
4. 允许 USB 调试授权

#### 选项 B: 使用模拟器
1. 打开 AVD Manager
   ```
   Tools → Device Manager
   ```
2. 创建新设备
   - 推荐配置: Pixel 6, API 33, Android 13
3. 启动模拟器

### 2. 运行应用

1. 在工具栏选择设备
2. 点击绿色的运行按钮 ▶
3. 或按快捷键: `Shift + F10` (Windows/Linux) 或 `Ctrl + R` (macOS)

### 3. 首次运行

应用启动后会显示：
- 空的媒体列表界面
- 一个 "+" 按钮用于添加媒体

---

## 第四步：添加测试媒体

### 准备测试文件

将以下文件放到设备的 `Downloads` 文件夹：
- 一个视频文件 (如 `test_video.mp4`)
- 一个音频文件 (如 `test_audio.mp3`)

### 导入媒体

1. 点击应用中的 "+" 按钮
2. 在文件选择器中找到测试文件
3. 选择文件
4. 文件会出现在列表中

### 播放测试

1. 点击列表中的媒体条目
2. 开始播放
3. 尝试各种手势和控制

---

## 常见问题解决

### 问题 1: Gradle 同步失败

**症状**: "Failed to sync Gradle project"

**解决方案**:
1. 检查网络连接
2. 配置代理（如需要）:
   ```
   File → Settings → Appearance & Behavior → System Settings → HTTP Proxy
   ```
3. 清理缓存:
   ```
   File → Invalidate Caches / Restart
   ```

### 问题 2: SDK 版本不匹配

**症状**: "Unsupported SDK version"

**解决方案**:
1. 打开 SDK Manager:
   ```
   Tools → SDK Manager
   ```
2. 安装所需组件:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools

### 问题 3: 找不到 R 类

**症状**: "Unresolved reference: R"

**解决方案**:
1. 确保所有资源文件格式正确
2. 清理项目:
   ```
   Build → Clean Project
   Build → Rebuild Project
   ```

### 问题 4: 应用安装失败

**症状**: "Installation failed"

**解决方案**:
1. 卸载设备上的旧版本
2. 确保设备有足够存储空间
3. 检查 USB 调试权限

### 问题 5: ExoPlayer 初始化失败

**症状**: 应用崩溃，日志显示 ExoPlayer 错误

**解决方案**:
1. 检查依赖是否正确下载
2. 清理并重新构建:
   ```
   Build → Clean Project
   Build → Rebuild Project
   ```

---

## 开发技巧

### 1. 查看日志

```
View → Tool Windows → Logcat
```

筛选应用日志:
```
package:com.localmedia.player
```

### 2. 调试断点

在代码行号左侧点击设置断点，然后以调试模式运行:
```
Run → Debug 'app' (Shift + F9)
```

### 3. 布局检查器

实时查看 Compose UI 层次:
```
Tools → Layout Inspector
```

### 4. 性能分析

```
View → Tool Windows → Profiler
```

### 5. 快速重新编译

使用 Apply Changes 快速应用代码更改（不重启应用）:
```
Run → Apply Changes and Restart Activity (Ctrl + F10)
```

---

## 构建 Release APK

### 1. 生成签名密钥（首次）

```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### 2. 配置签名（可选）

在 `app/build.gradle.kts` 中添加:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../my-release-key.jks")
            storePassword = "your-store-password"
            keyAlias = "my-key-alias"
            keyPassword = "your-key-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... 其他配置
        }
    }
}
```

### 3. 构建 APK

```bash
# 在项目根目录执行
./gradlew assembleRelease

# Windows:
gradlew.bat assembleRelease
```

### 4. 查找生成的 APK

```
app/build/outputs/apk/release/
├── app-armeabi-v7a-release.apk
├── app-arm64-v8a-release.apk
├── app-x86-release.apk
├── app-x86_64-release.apk
└── app-universal-release.apk
```

推荐安装: `app-universal-release.apk` (适用于所有设备)

---

## 下一步

### 学习代码

1. **从简单开始**:
   - 阅读 `MediaItem.kt` 了解数据结构
   - 查看 `MediaListScreen.kt` 了解 Compose UI

2. **深入核心**:
   - 研究 `ExoPlayerManager.kt` 播放器实现
   - 分析 `PlayerScreen.kt` 手势处理

3. **扩展功能**:
   - 添加播放列表功能
   - 实现均衡器
   - 添加更多手势

### 阅读文档

- [README.md](README.md) - 完整功能说明
- [ARCHITECTURE.md](ARCHITECTURE.md) - 架构设计
- [TESTING.md](TESTING.md) - 测试指南

### 参考资料

- [ExoPlayer 官方文档](https://developer.android.com/guide/topics/media/exoplayer)
- [Jetpack Compose 教程](https://developer.android.com/jetpack/compose/tutorial)
- [Kotlin 协程指南](https://kotlinlang.org/docs/coroutines-guide.html)

---

## 获取帮助

### 遇到问题？

1. 检查 Logcat 日志
2. 搜索错误信息
3. 查看相关文档
4. 提交 Issue

---

## 检查清单

在开始开发前，确保：

- [x] Android Studio 已安装并更新
- [x] 项目已成功导入
- [x] Gradle 同步完成
- [x] 图标资源已添加
- [x] 应用能在设备上运行
- [x] 能成功播放测试媒体
- [x] 已阅读 README.md
- [x] 了解项目结构

**全部完成？恭喜，可以开始开发了！** 🎉

---

## 快速命令参考

```bash
# 清理项目
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行测试
./gradlew test

# 查看所有任务
./gradlew tasks

# 检查依赖
./gradlew dependencies
```

---

**Happy Coding!** 💻✨

