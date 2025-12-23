# 本地播放器 - LocalMediaPlayer

一款轻量、好用、界面干净、完全离线的本地视频/音频播放器应用。

## 项目概述

### 技术栈
- **平台**：Android
- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **播放内核**：ExoPlayer (androidx.media3)
- **数据库**：Room
- **并发**：Kotlin Coroutines + Flow
- **架构**：MVVM + Repository 模式

### 核心特性
- ✅ 完全离线运行（无网络依赖、无广告、无统计）
- ✅ 支持本地视频和音频播放
- ✅ 丰富的手势控制（亮度、音量、快进快退）
- ✅ 多种播放速度（0.75x - 3.0x）
- ✅ 长按临时 2.5x 快速播放
- ✅ 字幕支持（SRT、WebVTT）
- ✅ 文稿时间点跳转
- ✅ 后台播放和通知栏控制
- ✅ 自动横竖屏切换
- ✅ 播放进度自动保存

---

## 1. 整体技术架构

### 模块划分

```
com.localmedia.player/
├── data/                          # 数据层
│   ├── entity/                    # 数据库实体
│   │   ├── MediaItem.kt          # 媒体条目实体
│   │   ├── Category.kt           # 分类实体
│   │   └── PlaybackHistory.kt    # 播放历史实体
│   ├── dao/                       # 数据访问对象
│   │   ├── MediaItemDao.kt
│   │   ├── CategoryDao.kt
│   │   └── PlaybackHistoryDao.kt
│   ├── repository/                # 数据仓库
│   │   └── MediaRepository.kt
│   └── MediaDatabase.kt           # Room 数据库
│
├── player/                        # 播放器核心
│   └── ExoPlayerManager.kt       # ExoPlayer 管理器
│
├── service/                       # 服务层
│   └── PlaybackService.kt        # 后台播放服务
│
├── ui/                            # UI 层
│   ├── screen/                    # 界面
│   │   ├── PlayerScreen.kt       # 播放器界面
│   │   └── MediaListScreen.kt    # 媒体列表界面
│   ├── theme/                     # 主题
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── MainActivity.kt            # 主 Activity
│   └── MainNavigation.kt          # 导航
│
├── utils/                         # 工具类
│   ├── SubtitleParser.kt         # 字幕解析器
│   ├── TranscriptParser.kt       # 文稿解析器
│   ├── GestureHandler.kt         # 手势处理器
│   └── OrientationManager.kt     # 屏幕方向管理器
│
└── MediaPlayerApplication.kt      # Application 类
```

### 数据流向

```
UI Layer (Compose) 
    ↕ 
ViewModel (State Management)
    ↕
Repository (Data Aggregation)
    ↕
DAO (Database Access) / ExoPlayerManager (Media Playback)
    ↕
Room Database / ExoPlayer
```

---

## 2. 数据库 Schema

### MediaItem 表
存储媒体文件的元数据和播放状态。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键（自增） |
| uri | String | 文件 URI（SAF 持久化） |
| title | String | 显示标题 |
| duration | Long | 时长（毫秒） |
| type | MediaType | 媒体类型（VIDEO/AUDIO） |
| coverUri | String? | 封面图 URI |
| lastPosition | Long | 上次播放位置（毫秒） |
| categoryId | Long? | 所属分类 ID |
| addedTime | Long | 添加时间 |
| lastPlayedTime | Long | 最后播放时间 |
| playCount | Int | 播放次数 |
| videoWidth | Int | 视频宽度 |
| videoHeight | Int | 视频高度 |
| subtitleUri | String? | 字幕文件 URI |
| transcriptUri | String? | 文稿文件 URI |

### Category 表
媒体分类管理。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键（自增） |
| name | String | 分类名称 |
| sortOrder | Int | 排序顺序 |
| createdTime | Long | 创建时间 |

### PlaybackHistory 表
播放历史记录。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键（自增） |
| mediaId | Long | 关联的媒体 ID（外键） |
| position | Long | 播放位置（毫秒） |
| timestamp | Long | 记录时间戳 |
| playbackSpeed | Float | 播放速度 |

---

## 3. ExoPlayerManager 核心功能

### 主要功能

#### 播放控制
- `initialize()` - 初始化播放器
- `prepareAndPlay(uri, startPosition)` - 准备并播放媒体
- `play()` / `pause()` / `togglePlayPause()` - 播放/暂停控制
- `stop()` - 停止播放
- `release()` - 释放资源

#### 进度控制
- `seekTo(positionMs)` - 跳转到指定位置
- `seekForward()` - 前进 15 秒
- `seekBackward()` - 后退 15 秒
- `getCurrentPositionMs()` - 获取当前位置
- `getDurationMs()` - 获取总时长

#### 倍速控制
- `setPlaybackSpeed(speed)` - 设置播放速度
- `cyclePlaybackSpeed()` - 切换到下一个倍速
- `startLongPress()` - 长按开始（切换到 2.5x）
- `endLongPress()` - 长按结束（恢复原速）

#### 状态监听
- `isPlaying: StateFlow<Boolean>` - 播放状态
- `currentPosition: StateFlow<Long>` - 当前位置
- `duration: StateFlow<Long>` - 总时长
- `playbackSpeed: StateFlow<Float>` - 当前倍速
- `videoSize: StateFlow<VideoSize?>` - 视频尺寸

### 倍速预设
```kotlin
val availableSpeeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)
```

### 长按临时加速实现
```kotlin
// 长按开始
fun startLongPress() {
    val currentSpeed = _playbackSpeed.value
    if (currentSpeed != 2.5f) {
        savedSpeedBeforeLongPress = currentSpeed
        setPlaybackSpeed(2.5f)
    }
}

// 长按结束
fun endLongPress() {
    savedSpeedBeforeLongPress?.let { savedSpeed ->
        setPlaybackSpeed(savedSpeed)
        savedSpeedBeforeLongPress = null
    }
}
```

---

## 4. PlayerScreen - 手势处理

### 手势类型

#### 1. 双击手势
- **左半屏双击**：后退 15 秒
- **右半屏双击**：前进 15 秒

```kotlin
detectTapGestures(
    onDoubleTap = { offset ->
        val screenWidth = size.width.toFloat()
        if (gestureHandler.isLeftHalf(offset, screenWidth)) {
            playerManager.seekBackward()
        } else {
            playerManager.seekForward()
        }
    }
)
```

#### 2. 长按手势
- **长按屏幕**：临时切换到 2.5x 速度
- **松开**：恢复原速

```kotlin
detectTapGestures(
    onLongPress = {
        isLongPressing = true
        playerManager.startLongPress()
    },
    onPress = {
        val press = tryAwaitRelease()
        if (!press && isLongPressing) {
            isLongPressing = false
            playerManager.endLongPress()
        }
    }
)
```

#### 3. 垂直滑动手势
- **左半屏上下滑动**：调节亮度（使用 window.attributes.screenBrightness）
- **右半屏上下滑动**：调节音量（使用 AudioManager）

```kotlin
detectDragGestures(
    onDrag = { change, dragAmount ->
        when (gestureType) {
            GestureHandler.GestureType.BRIGHTNESS -> {
                val currentBrightness = brightnessManager.getScreenBrightness()
                val newBrightness = gestureHandler.calculateBrightnessChange(
                    currentBrightness,
                    dragAmount.y,
                    screenHeight
                )
                brightnessManager.setScreenBrightness(newBrightness)
            }
            GestureHandler.GestureType.VOLUME -> {
                val currentVolume = gestureHandler.getCurrentVolume()
                val newVolume = gestureHandler.calculateVolumeChange(
                    currentVolume,
                    dragAmount.y,
                    screenHeight
                )
                gestureHandler.setVolume(newVolume)
            }
        }
    }
)
```

#### 4. 水平滑动手势（按住后）
- **左右滑动**：快进/快退（按拖动距离计算）

#### 5. 锁屏模式
- 点击锁定按钮后：
  - 隐藏所有控制按钮
  - 禁用所有手势
  - 仅显示解锁图标

### 亮度控制（无需系统权限）

```kotlin
class BrightnessManager(private val window: Window) {
    fun setScreenBrightness(brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness.coerceIn(0.01f, 1.0f)
        window.attributes = layoutParams
    }
}
```

**优点**：
- 不需要 `WRITE_SETTINGS` 权限
- 仅影响当前窗口
- 退出应用后恢复系统亮度

### 音量控制

```kotlin
class GestureHandler(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    fun setVolume(volume: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume.coerceIn(0, maxVolume),
            0
        )
    }
}
```

---

## 5. 字幕与文稿解析

### 字幕解析器 (SubtitleParser)

#### 支持格式
1. **SRT 格式**
```
1
00:00:01,000 --> 00:00:03,000
字幕文本
```

2. **WebVTT 格式**
```
WEBVTT

00:00:01.000 --> 00:00:03.000
字幕文本
```

#### 使用方法

```kotlin
val parser = SubtitleParser(context)
val entries = parser.parseSubtitle(uri)

// 获取当前字幕
val currentSubtitle = parser.getCurrentSubtitle(entries, currentPositionMs)
```

#### SubtitleEntry 数据结构
```kotlin
data class SubtitleEntry(
    val index: Int,           // 索引
    val startTimeMs: Long,    // 开始时间（毫秒）
    val endTimeMs: Long,      // 结束时间（毫秒）
    val text: String          // 字幕文本
)
```

### 文稿解析器 (TranscriptParser)

#### 文稿格式
支持多种时间戳格式：
```
[HH:MM:SS.mmm] 文本内容
[HH:MM:SS] 文本内容
[MM:SS.mmm] 文本内容
[MM:SS] 文本内容
```

#### 使用示例
```
[00:00:15] 欢迎使用本地播放器
[00:01:23.500] 这是一个时间点标记
[00:02:45] 点击时间点可以快速跳转
```

#### 使用方法

```kotlin
val parser = TranscriptParser(context)
val entries = parser.parseTranscript(uri)

// 获取当前应高亮的条目
val currentEntry = parser.getCurrentEntry(entries, currentPositionMs)

// 点击时间点跳转
playerManager.seekTo(entry.timeMs)
```

#### TranscriptEntry 数据结构
```kotlin
data class TranscriptEntry(
    val timeMs: Long,         // 时间戳（毫秒）
    val text: String,         // 文本内容
    val originalText: String  // 原始文本（包含时间标记）
)
```

#### 实现原理
1. 使用正则表达式匹配时间戳格式
2. 解析时间为毫秒数
3. 按时间排序生成条目列表
4. 根据当前播放位置查找对应条目
5. 点击条目时调用 `seekTo()` 跳转

---

## 6. 后台播放与 MediaSession

### PlaybackService 实现

使用 `MediaSessionService` 实现后台播放和系统集成。

#### 核心功能
1. **前台服务**
   - 使用 `foregroundServiceType="mediaPlayback"`
   - 显示持续通知

2. **MediaSession 集成**
   - 支持锁屏控制
   - 支持蓝牙/耳机控制
   - 支持通知栏媒体控制

3. **通知栏控制**
   - 显示封面图
   - 显示标题和艺术家
   - 播放/暂停按钮
   - 上一首/下一首按钮
   - 前进/后退 15 秒按钮

#### AndroidManifest 配置
```xml
<service
    android:name=".service.PlaybackService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

#### 通知渠道
```kotlin
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "播放控制",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "音视频播放通知"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
```

### 权限要求
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 7. 横竖屏自动切换

### OrientationManager 实现

根据视频宽高比自动切换屏幕方向。

#### 切换规则

1. **视频播放**
   - 宽高比 > 1.0：自动切换到横屏
   - 宽高比 ≤ 1.0：保持竖屏

2. **音频播放**
   - 始终保持竖屏

#### 使用方法

```kotlin
val orientationManager = OrientationManager(activity)

// 监听视频尺寸变化
videoSize.collect { size ->
    if (mediaType == MediaType.VIDEO) {
        orientationManager.setOrientationForVideo(size)
    } else {
        orientationManager.setOrientationForAudio()
    }
}
```

#### 实现原理

```kotlin
fun setOrientationForVideo(videoSize: VideoSize?) {
    if (videoSize == null || videoSize.width == 0 || videoSize.height == 0) {
        setPortrait()
        return
    }
    
    val aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
    
    if (aspectRatio > 1.0f) {
        setLandscape()  // 横屏视频
    } else {
        setPortrait()   // 竖屏视频
    }
}
```

#### 其他功能
- `lockCurrentOrientation()` - 锁定当前方向
- `unlockOrientation()` - 解锁方向
- `setAutoRotate()` - 自动旋转

---

## 8. Gradle 依赖清单

### app/build.gradle.kts

```kotlin
dependencies {
    // Kotlin 核心
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    implementation("androidx.media3:media3-common:1.2.1")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Accompanist (手势、权限等)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // DataStore（可选，用于保存设置）
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // 测试
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### 插件版本
```kotlin
plugins {
    id("com.android.application") version "8.2.0"
    id("org.jetbrains.kotlin.android") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}
```

---

## 9. APK 打包与 ABI 拆分

### ABI 拆分配置

```kotlin
android {
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true  // 生成通用 APK
        }
    }
}
```

### 生成的 APK 文件
- `app-armeabi-v7a-release.apk` (32 位 ARM)
- `app-arm64-v8a-release.apk` (64 位 ARM，主流设备)
- `app-x86-release.apk` (模拟器)
- `app-x86_64-release.apk` (模拟器)
- `app-universal-release.apk` (包含所有 ABI，体积较大)

### Release 配置

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### ProGuard 规则

```proguard
# ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
```

### 打包命令

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease

# 清理后重新打包
./gradlew clean assembleRelease
```

### 签名配置（生产环境）

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("your-keystore.jks")
        storePassword = "your-store-password"
        keyAlias = "your-key-alias"
        keyPassword = "your-key-password"
    }
}
```

---

## 10. 测试用例清单

### 功能测试

#### 播放功能
- [ ] 播放视频文件
- [ ] 播放音频文件
- [ ] 播放/暂停切换
- [ ] 进度条拖动
- [ ] 前进 15 秒
- [ ] 后退 15 秒
- [ ] 播放完成后自动停止
- [ ] 播放进度自动保存

#### 倍速功能
- [ ] 切换到 0.75x
- [ ] 切换到 1.25x
- [ ] 切换到 1.5x
- [ ] 切换到 1.75x
- [ ] 切换到 2.0x
- [ ] 切换到 2.5x
- [ ] 切换到 3.0x
- [ ] 倍速菜单显示当前速度
- [ ] 循环切换倍速

#### 长按临时加速
- [ ] 长按屏幕切换到 2.5x
- [ ] 松开恢复原速度
- [ ] 原速度为 1.0x 时的恢复
- [ ] 原速度为 1.5x 时的恢复
- [ ] 显示 "2.5x" 提示

### 手势测试

#### 双击手势
- [ ] 左半屏双击后退 15 秒
- [ ] 右半屏双击前进 15 秒
- [ ] 双击边界位置

#### 滑动手势
- [ ] 左半屏上滑增加亮度
- [ ] 左半屏下滑降低亮度
- [ ] 右半屏上滑增加音量
- [ ] 右半屏下滑降低音量
- [ ] 水平滑动快进/快退
- [ ] 显示亮度指示器
- [ ] 显示音量指示器
- [ ] 指示器自动隐藏

#### 锁屏模式
- [ ] 点击锁定按钮
- [ ] 锁定后禁用手势
- [ ] 锁定后隐藏控件
- [ ] 点击解锁按钮
- [ ] 解锁后恢复功能

### 列表与导入测试

#### 媒体导入
- [ ] 选择单个视频文件
- [ ] 选择单个音频文件
- [ ] 选择多个文件
- [ ] 选择文件夹
- [ ] 持久化 URI 权限
- [ ] 提取媒体元数据
- [ ] 显示媒体时长
- [ ] 显示封面图

#### 列表管理
- [ ] 显示所有媒体
- [ ] 按类型筛选（视频/音频）
- [ ] 按分类筛选
- [ ] 显示播放进度
- [ ] 点击播放
- [ ] 删除媒体
- [ ] 重命名媒体
- [ ] 创建分类
- [ ] 移动到分类

### 屏幕方向测试

#### 视频播放
- [ ] 横屏视频（16:9）自动横屏
- [ ] 竖屏视频（9:16）保持竖屏
- [ ] 正方形视频（1:1）保持竖屏
- [ ] 切换视频时自动调整方向
- [ ] 用户手动旋转设备

#### 音频播放
- [ ] 始终保持竖屏
- [ ] 显示音频封面
- [ ] 显示音频元数据
- [ ] 旋转设备不改变方向

### 后台播放测试

#### 服务功能
- [ ] 切换到后台继续播放
- [ ] 锁屏后继续播放
- [ ] 通知栏显示播放信息
- [ ] 通知栏封面显示
- [ ] 通知栏播放/暂停控制
- [ ] 通知栏上一首/下一首
- [ ] 通知栏前进/后退 15 秒
- [ ] 点击通知返回应用
- [ ] 关闭通知停止播放

#### 系统集成
- [ ] 耳机播放/暂停
- [ ] 蓝牙控制
- [ ] 音频焦点管理
- [ ] 来电时自动暂停
- [ ] 通话结束后恢复播放

### 字幕与文稿测试

#### 字幕功能
- [ ] 加载 SRT 字幕
- [ ] 加载 WebVTT 字幕
- [ ] 字幕同步显示
- [ ] 字幕位置调整
- [ ] 字幕大小调整
- [ ] 切换字幕
- [ ] 关闭字幕

#### 文稿功能
- [ ] 解析时间点格式
- [ ] 显示文稿列表
- [ ] 点击时间点跳转
- [ ] 播放时自动高亮
- [ ] 滚动到当前位置
- [ ] 支持多种时间格式

### 性能与稳定性测试

#### 性能测试
- [ ] 播放 4K 视频
- [ ] 播放长时间视频（2 小时+）
- [ ] 快速切换倍速
- [ ] 频繁拖动进度条
- [ ] 连续前进/后退
- [ ] 内存占用监控
- [ ] CPU 占用监控
- [ ] 电池消耗监控

#### 异常处理
- [ ] 播放损坏的文件
- [ ] 播放不支持的格式
- [ ] 网络断开（本应用无网络依赖）
- [ ] 存储权限拒绝
- [ ] 文件被删除后的处理
- [ ] 低内存情况
- [ ] 应用崩溃恢复

### 兼容性测试

#### Android 版本
- [ ] Android 8.0 (API 26)
- [ ] Android 9.0 (API 28)
- [ ] Android 10 (API 29)
- [ ] Android 11 (API 30)
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)

#### 设备类型
- [ ] 手机（小屏）
- [ ] 手机（大屏）
- [ ] 平板
- [ ] 折叠屏
- [ ] 异形屏

#### 格式支持
- [ ] MP4 视频
- [ ] MKV 视频
- [ ] AVI 视频
- [ ] MP3 音频
- [ ] FLAC 音频
- [ ] AAC 音频
- [ ] M4A 音频

---

## 使用指南

### 构建项目

1. **克隆项目**
```bash
git clone <repository-url>
cd LocalMediaPlayer
```

2. **打开 Android Studio**
   - 使用 Android Studio Hedgehog (2023.1.1) 或更高版本
   - 打开项目根目录

3. **同步 Gradle**
   - 等待 Gradle 同步完成
   - 下载所有依赖

4. **运行项目**
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮

### 添加媒体文件

1. 点击右上角 "+" 按钮
2. 选择视频或音频文件
3. 授予存储权限
4. 文件将出现在列表中

### 播放控制

- **播放/暂停**：点击中央播放按钮
- **前进/后退**：点击箭头按钮或双击屏幕
- **调节进度**：拖动进度条
- **调节倍速**：点击倍速按钮选择
- **调节亮度**：左半屏上下滑动
- **调节音量**：右半屏上下滑动
- **临时加速**：长按屏幕
- **锁定屏幕**：点击锁定按钮

---

## 项目特色

### ✅ 完全离线
- 无需网络连接
- 无广告、无统计
- 保护用户隐私

### ✅ 流畅体验
- ExoPlayer 高性能播放
- 硬件加速支持
- 低内存占用

### ✅ 人性化设计
- 直观的手势控制
- 自动保存播放进度
- 智能横竖屏切换

### ✅ 功能丰富
- 多种播放速度
- 字幕支持
- 文稿时间点跳转
- 后台播放

---

## 许可证

本项目为开源项目，仅供学习和个人使用。

---

## 更新日志

### v1.0.0 (2024-12-23)
- ✅ 初始版本发布
- ✅ 实现基础播放功能
- ✅ 实现手势控制
- ✅ 实现倍速播放
- ✅ 实现字幕支持
- ✅ 实现后台播放
- ✅ 实现自动横竖屏切换

---

## 技术支持

如有问题或建议，请提交 Issue。

---

**享受纯净的本地播放体验！** 🎬🎵

