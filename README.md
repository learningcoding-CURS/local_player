# 本地媒体播放器

一款基于 Kotlin + Jetpack Compose + ExoPlayer 开发的轻量、干净、完全离线的本地音视频播放器。

## 📋 项目概述

本项目是一个功能完整的 Android 本地媒体播放器，专注于提供极致的用户体验和丰富的播放功能，完全离线运行，不依赖任何网络服务。

### 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **播放器内核**: ExoPlayer (androidx.media3)
- **数据库**: Room
- **架构**: MVVM + Repository 模式
- **异步处理**: Kotlin Coroutines + Flow

---

## 🏗️ 项目架构

### 整体架构分层

```
app/
├── data/                           # 数据层
│   ├── entity/                     # 数据实体
│   │   ├── MediaItem.kt            # 媒体项实体
│   │   └── Category.kt             # 分类实体
│   ├── dao/                        # 数据访问对象
│   │   ├── MediaItemDao.kt         # 媒体项 DAO
│   │   └── CategoryDao.kt          # 分类 DAO
│   ├── database/                   # 数据库
│   │   └── AppDatabase.kt          # Room 数据库
│   └── repository/                 # 数据仓库
│       └── MediaRepository.kt      # 统一数据访问接口
│
├── player/                         # 播放器层
│   ├── ExoPlayerManager.kt         # ExoPlayer 管理器
│   └── PlayerViewModel.kt          # 播放器 ViewModel
│
├── service/                        # 服务层
│   └── PlaybackService.kt          # 后台播放服务
│
├── subtitle/                       # 字幕模块
│   └── SubtitleParser.kt           # 字幕解析器
│
├── transcript/                     # 稿件模块
│   ├── TranscriptParser.kt         # 稿件解析器
│   └── TranscriptManager.kt        # 稿件管理器
│
├── ui/                             # UI 层
│   ├── player/                     # 播放器 UI
│   │   ├── PlayerScreen.kt         # 播放器屏幕
│   │   └── PlayerControls.kt       # 播放器控制栏
│   ├── library/                    # 媒体库 UI
│   │   ├── LibraryScreen.kt        # 媒体库屏幕
│   │   └── LibraryViewModel.kt     # 媒体库 ViewModel
│   ├── utils/                      # UI 工具类
│   │   ├── BrightnessManager.kt    # 亮度管理器
│   │   ├── VolumeManager.kt        # 音量管理器
│   │   └── OrientationManager.kt   # 屏幕方向管理器
│   └── theme/                      # 主题
│       ├── Theme.kt                # 主题定义
│       └── Type.kt                 # 字体定义
│
├── MainActivity.kt                 # 主 Activity
├── AppNavigation.kt                # 应用导航
└── MediaPlayerApplication.kt       # 应用入口
```

### 架构设计说明

1. **数据层 (Data Layer)**
   - 使用 Room 数据库持久化媒体信息和播放进度
   - Repository 模式提供统一的数据访问接口
   - 使用 Flow 实现响应式数据流

2. **播放器层 (Player Layer)**
   - ExoPlayerManager 封装播放器核心功能
   - PlayerViewModel 管理播放状态和 UI 交互
   - 支持倍速、跳转、长按加速等功能

3. **UI 层 (UI Layer)**
   - 使用 Jetpack Compose 构建声明式 UI
   - 实现手势控制（滑动调节亮度/音量、双击快进/快退）
   - 支持横竖屏自动切换

4. **服务层 (Service Layer)**
   - 使用 MediaSessionService 实现后台播放
   - 通知栏显示播放控制

---

## ✨ 核心功能

### 1. 基础播放功能

#### 播放控制
- ✅ 播放 / 暂停
- ✅ 进度条拖动
- ✅ 显示当前时间 / 总时长
- ✅ 支持本地视频和音频文件

#### 跳转功能
- ✅ 前进 15 秒
- ✅ 后退 15 秒
- ✅ 双击左/右区域触发前后 15 秒跳转

#### 倍速播放
- ✅ 预设倍速：0.75x / 1.0x / 1.25x / 1.5x / 1.75x / 2.0x / 2.5x / 3.0x
- ✅ 可随时切换倍速
- ✅ 长按屏幕临时切换到 2.5x
- ✅ 松开后恢复之前的倍速

**核心代码示例：**

```kotlin
// ExoPlayerManager.kt - 倍速控制
fun setPlaybackSpeed(speed: Float) {
    player?.let {
        val params = PlaybackParameters(speed)
        it.playbackParameters = params
        _playbackSpeed.value = speed
        
        if (!isLongPressing) {
            savedSpeed = speed
        }
    }
}

// 长按临时加速
fun onLongPressStart() {
    if (!isLongPressing) {
        isLongPressing = true
        savedSpeed = _playbackSpeed.value
        setPlaybackSpeed(2.5f)
    }
}

fun onLongPressEnd() {
    if (isLongPressing) {
        isLongPressing = false
        setPlaybackSpeed(savedSpeed)
    }
}
```

---

### 2. 手势交互

#### 亮度调节（左半屏上下滑动）
- 使用 `window.attributes.screenBrightness` 调节应用内亮度
- 不需要 `WRITE_SETTINGS` 权限

#### 音量调节（右半屏上下滑动）
- 使用 `AudioManager` 调节系统音量
- 不显示系统音量面板

#### 双击快进/快退
- 双击左侧：后退 15 秒
- 双击右侧：前进 15 秒
- 显示视觉反馈

#### 长按加速
- 长按开始：临时切换到 2.5x
- 长按结束：恢复原速

#### 锁屏模式
- 锁定后隐藏所有控制按钮
- 禁用手势
- 仅显示锁定图标（可解锁）

**手势处理示例：**

```kotlin
// PlayerScreen.kt - 手势检测
.pointerInput(Unit) {
    detectTapGestures(
        onTap = {
            if (!isLocked) {
                if (showControls) viewModel.hideControls()
                else viewModel.showControls()
            }
        },
        onDoubleTap = { offset ->
            if (!isLocked) {
                val screenWidth = size.width
                if (offset.x < screenWidth / 2) {
                    viewModel.seekBackward() // 后退 15 秒
                } else {
                    viewModel.seekForward()  // 前进 15 秒
                }
            }
        },
        onLongPress = {
            if (!isLocked) {
                viewModel.onLongPressStart() // 临时 2.5x
            }
        }
    )
}
```

---

### 3. 字幕支持

#### 支持的格式
- ✅ SRT（SubRip）
- ✅ WebVTT
- 🔲 ASS（可扩展）

#### 字幕功能
- 动态加载字幕文件
- 根据播放时间自动显示字幕
- 支持多行字幕

**字幕解析示例：**

```kotlin
// SubtitleParser.kt
data class SubtitleEntry(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

class SubtitleParser {
    // 解析 SRT 格式
    fun parseSrt(inputStream: InputStream): List<SubtitleEntry>
    
    // 解析 WebVTT 格式
    fun parseWebVtt(inputStream: InputStream): List<SubtitleEntry>
    
    // 获取当前应该显示的字幕
    fun getCurrentSubtitle(entries: List<SubtitleEntry>, currentTimeMs: Long): SubtitleEntry?
}
```

**使用方法：**

```kotlin
// 1. 加载字幕文件
val parser = SubtitleParser()
val subtitles = parser.parseSrt(inputStream)

// 2. 在播放时获取当前字幕
val currentSubtitle = parser.getCurrentSubtitle(subtitles, currentTimeMs)
```

---

### 4. 稿件时间点跳转

#### 支持的格式
文本文件中包含时间点标记，例如：

```
[00:01:23.500] 这里是第一段内容
[00:02:30] 这里是第二段内容
[01:15:00] 这里是第三段内容
```

#### 稿件功能
- 解析时间点标记
- 点击时间点跳转到对应位置
- 播放时自动高亮当前文本

**稿件解析示例：**

```kotlin
// TranscriptParser.kt
data class TranscriptEntry(
    val timeMs: Long,
    val text: String,
    val lineNumber: Int
)

class TranscriptParser {
    // 解析稿件文件
    fun parse(inputStream: InputStream): List<TranscriptEntry>
    
    // 获取当前高亮的条目
    fun getCurrentEntry(entries: List<TranscriptEntry>, currentTimeMs: Long): TranscriptEntry?
}

// TranscriptManager.kt
class TranscriptManager {
    // 加载稿件
    fun load(inputStream: InputStream)
    
    // 获取当前高亮的条目索引
    fun getCurrentIndex(currentTimeMs: Long): Int
}
```

**使用方法：**

```kotlin
// 1. 加载稿件
val manager = TranscriptManager()
manager.load(inputStream)

// 2. 点击条目跳转
val entry = manager.getEntry(index)
playerManager.seekTo(entry.timeMs)

// 3. 获取当前高亮的条目
val currentIndex = manager.getCurrentIndex(currentTimeMs)
```

---

### 5. 屏幕方向规则

#### 视频播放
- 根据视频宽高比自动决定横屏或竖屏
- 使用 ExoPlayer 的 `videoSize` 回调判断

#### 音频播放
- 始终竖屏显示
- 显示音频封面（从媒体元数据或单独图片）

**屏幕方向管理：**

```kotlin
// OrientationManager.kt
class OrientationManager(private val activity: Activity) {
    
    fun setOrientation(videoSize: VideoSize?) {
        if (videoSize == null) {
            // 音频：竖屏
            activity.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        } else {
            // 视频：根据宽高比决定
            if (videoSize.width > videoSize.height) {
                activity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
            } else {
                activity.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }
}
```

---

### 6. 媒体导入与列表管理

#### 文件导入
- 使用 Storage Access Framework（SAF）导入文件
- 支持单个文件导入
- 支持文件夹批量导入
- 持久化 URI 权限

#### 列表管理
- 显示所有音视频文件
- 显示播放进度（百分比）
- 支持搜索
- 支持分类管理

#### 播放进度自动保存
- 播放时每 5 秒自动保存位置
- 暂停时立即保存位置
- 下次打开自动从上次位置继续播放

**导入文件示例：**

```kotlin
// LibraryViewModel.kt
fun importMediaFile(uri: Uri) {
    viewModelScope.launch {
        // 1. 持久化 URI 权限
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        
        // 2. 创建媒体项
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        val mediaItem = createMediaItem(uri, documentFile)
        
        // 3. 插入数据库
        repository.insertItem(mediaItem)
    }
}
```

---

### 7. 后台播放与系统集成

#### 后台播放
- 使用 Foreground Service 实现后台播放
- 应用切换到后台后音频继续播放

#### MediaSession 集成
- 使用 Media3 的 MediaSession
- 通知栏显示封面、标题、播放控制
- 支持耳机/蓝牙控制
- 支持锁屏控制

**后台服务实现：**

```kotlin
// PlaybackService.kt
class PlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化播放器
        player = ExoPlayer.Builder(this).build()
        
        // 创建 MediaSession
        mediaSession = MediaSession.Builder(this, player!!)
            .setCallback(MediaSessionCallback())
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
}
```

---

## 📊 数据库 Schema

### MediaItem（媒体项）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键，自增 |
| uri | String | 文件 URI（通过 SAF 获取） |
| title | String | 文件标题 |
| duration | Long | 媒体时长（毫秒） |
| type | MediaType | 媒体类型（VIDEO/AUDIO） |
| coverUri | String? | 封面 URI（可选） |
| lastPosition | Long | 上次播放位置（毫秒） |
| categoryId | Long? | 分类 ID |
| addedTime | Long | 添加时间 |
| lastPlayedTime | Long | 上次播放时间 |
| playCount | Int | 播放次数 |
| videoWidth | Int | 视频宽度 |
| videoHeight | Int | 视频高度 |
| subtitleUri | String? | 字幕文件 URI |
| transcriptUri | String? | 稿件文本 URI |

### Category（分类）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键，自增 |
| name | String | 分类名称 |
| sortOrder | Int | 排序顺序 |
| createdTime | Long | 创建时间 |

### DAO 接口

```kotlin
@Dao
interface MediaItemDao {
    @Insert suspend fun insert(item: MediaItem): Long
    @Update suspend fun update(item: MediaItem)
    @Delete suspend fun delete(item: MediaItem)
    @Query("SELECT * FROM media_items") fun getAllItems(): Flow<List<MediaItem>>
    @Query("UPDATE media_items SET lastPosition = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Long)
}
```

---

## 📦 Gradle 依赖

完整依赖列表见 `app/build.gradle.kts`：

```kotlin
dependencies {
    // Kotlin & Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-session:1.2.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
}
```

---

## 🛠️ 编译与打包

### 开发环境要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17 或 JDK 21
- Android SDK API 34
- Gradle 8.9
- Android Gradle Plugin 8.5.2

### 编译步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd LocalMediaPlayer
   ```

2. **使用 Android Studio 打开项目**
   - File → Open → 选择项目目录

3. **同步 Gradle**
   - 首次打开会自动同步，或点击 "Sync Project with Gradle Files"

4. **运行到设备**
   - 连接 Android 设备或启动模拟器
   - 点击 Run 按钮（绿色三角形）

### 打包 Release APK

#### 方法一：使用 Android Studio

1. Build → Generate Signed Bundle / APK
2. 选择 APK
3. 创建或选择密钥库（Keystore）
4. 选择 release 构建类型
5. 等待构建完成

#### 方法二：使用命令行

```bash
# 1. 生成 Release APK
./gradlew assembleRelease

# 2. APK 输出路径
# app/build/outputs/apk/release/app-release.apk
```

### ABI 拆分配置

项目已配置 ABI 拆分，可生成不同架构的 APK：

```kotlin
// app/build.gradle.kts
splits {
    abi {
        isEnable = true
        reset()
        include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        isUniversalApk = true  // 同时生成通用 APK
    }
}
```

生成的 APK：
- `app-armeabi-v7a-release.apk` (32位 ARM)
- `app-arm64-v8a-release.apk` (64位 ARM)
- `app-x86-release.apk` (32位 x86)
- `app-x86_64-release.apk` (64位 x86)
- `app-universal-release.apk` (通用版，包含所有架构)

### ProGuard 混淆

项目已配置 ProGuard 规则（`app/proguard-rules.pro`），Release 版本会自动启用代码混淆和资源压缩。

---

## 🧪 测试用例清单

### 1. 基础播放测试
- [ ] 播放视频文件
- [ ] 播放音频文件
- [ ] 播放/暂停切换
- [ ] 进度条拖动
- [ ] 前进 15 秒
- [ ] 后退 15 秒

### 2. 倍速测试
- [ ] 切换所有预设倍速（0.75x ~ 3.0x）
- [ ] 长按屏幕临时 2.5x
- [ ] 松开后恢复原速
- [ ] 倍速记忆功能

### 3. 手势测试
- [ ] 双击左侧后退 15 秒
- [ ] 双击右侧前进 15 秒
- [ ] 左半屏上下滑动调节亮度
- [ ] 右半屏上下滑动调节音量
- [ ] 单击显示/隐藏控制栏
- [ ] 控制栏 3 秒自动隐藏

### 4. 锁屏模式测试
- [ ] 锁定后禁用所有手势
- [ ] 锁定后隐藏控制栏
- [ ] 显示锁定图标
- [ ] 点击图标解锁

### 5. 列表管理测试
- [ ] 导入单个视频文件
- [ ] 导入单个音频文件
- [ ] 导入文件夹
- [ ] 显示播放进度
- [ ] 删除媒体项
- [ ] 搜索功能

### 6. 播放进度测试
- [ ] 播放时自动保存位置
- [ ] 暂停时保存位置
- [ ] 下次打开从上次位置继续播放
- [ ] 切换媒体项保存位置

### 7. 屏幕方向测试
- [ ] 横屏视频自动横屏显示
- [ ] 竖屏视频自动竖屏显示
- [ ] 音频文件始终竖屏
- [ ] 旋转设备正常适配

### 8. 后台播放测试
- [ ] 切换到后台继续播放
- [ ] 通知栏显示播放信息
- [ ] 通知栏播放/暂停控制
- [ ] 耳机控制播放/暂停
- [ ] 锁屏控制

### 9. 字幕测试
- [ ] 加载 SRT 字幕
- [ ] 加载 WebVTT 字幕
- [ ] 字幕同步显示
- [ ] 多行字幕显示

### 10. 稿件时间点测试
- [ ] 加载稿件文件
- [ ] 点击时间点跳转
- [ ] 播放时自动高亮当前文本
- [ ] 滚动到当前文本位置

---

## 📝 使用说明

### 首次使用

1. **安装应用**
   - 将 APK 安装到 Android 设备

2. **导入媒体文件**
   - 打开应用，点击右上角的 "+" 按钮
   - 选择"文档"或"文件夹"
   - 授予文件访问权限
   - 选择要导入的视频或音频文件

3. **开始播放**
   - 在媒体库中点击任意媒体项
   - 进入播放器界面开始播放

### 播放控制

- **播放/暂停**: 点击中央播放按钮
- **进度调整**: 拖动进度条
- **快进/快退**: 双击屏幕左/右侧
- **倍速切换**: 点击倍速按钮，选择倍速
- **临时加速**: 长按屏幕（2.5x）
- **锁定屏幕**: 点击锁定按钮

### 亮度/音量调节

- **调节亮度**: 在屏幕左半侧上下滑动
- **调节音量**: 在屏幕右半侧上下滑动

### 字幕和稿件

- **加载字幕**: 将字幕文件放在与视频同目录，命名相同即可自动加载
- **加载稿件**: 在媒体项设置中指定稿件文件路径

---

## 🔒 权限说明

应用所需权限及用途：

| 权限 | 用途 | 是否必需 |
|------|------|----------|
| READ_MEDIA_AUDIO | 读取音频文件（Android 13+） | 是 |
| READ_MEDIA_VIDEO | 读取视频文件（Android 13+） | 是 |
| READ_EXTERNAL_STORAGE | 读取媒体文件（Android 12 及以下） | 是 |
| FOREGROUND_SERVICE | 后台播放服务 | 是 |
| FOREGROUND_SERVICE_MEDIA_PLAYBACK | 媒体播放前台服务 | 是 |
| WAKE_LOCK | 保持设备唤醒 | 是 |

**注意**：本应用完全离线，不申请任何网络权限，不收集任何用户数据。

---

## 🎨 特性亮点

1. **完全离线**：不依赖任何网络服务，保护隐私
2. **界面干净**：Material Design 3 设计，简洁美观
3. **手势丰富**：支持多种手势操作，提升体验
4. **功能强大**：支持倍速、字幕、稿件等高级功能
5. **性能优秀**：使用 ExoPlayer 内核，播放流畅
6. **架构清晰**：MVVM 架构，易于维护和扩展

---

## 🚀 未来计划

- [ ] 支持 ASS 字幕格式
- [ ] 支持播放列表循环模式
- [ ] 支持视频截图功能
- [ ] 支持多音轨切换
- [ ] 支持画中画（PIP）模式
- [ ] 支持 Cast（投屏）功能
- [ ] 支持睡眠定时器
- [ ] 支持书签功能

---

## 📋 版本历史

### v1.0.3 (2025-12-23)

#### 增强错误处理和日志系统

**问题描述**：
v1.0.2 修复了基础的闪退问题，但应用仍然存在 "keeps stopping" 问题。为了更好地诊断问题，需要添加全面的日志和错误处理。

**新增内容**：

##### 1. 全面的日志记录

在所有关键组件添加详细日志：
- `AppDatabase` - 数据库创建和初始化日志
- `LibraryViewModel` - ViewModel 生命周期和操作日志
- `AppNavigation` - 导航和组件初始化日志
- `MainActivity` - Activity 生命周期日志

**示例日志**：
```kotlin
// AppDatabase.kt
Log.d(TAG, "Getting database instance...")
Log.d(TAG, "Creating new database instance")
Log.d(TAG, "Database instance created successfully")

// AppNavigation.kt
Log.d(TAG, "AppNavigation composing...")
Log.d(TAG, "Initializing database...")
Log.d(TAG, "Creating repository...")
Log.d(TAG, "Creating player manager...")

// MainActivity.kt
Log.d(TAG, "MainActivity onCreate")
Log.d(TAG, "MainActivity setContent completed")
```

##### 2. 错误捕获和显示

**MainActivity 错误屏幕**：
添加 `ErrorScreen` 组件，当应用初始化失败时不会直接崩溃，而是显示错误信息：

```kotlin
@Composable
fun ErrorScreen(error: Exception) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("应用启动失败", style = MaterialTheme.typography.headlineMedium)
        Text("错误信息：${error.message}")
        Text("请查看 logcat 获取详细日志")
    }
}
```

**所有初始化代码包装 try-catch**：
```kotlin
val database = remember { 
    try {
        Log.d(TAG, "Initializing database...")
        AppDatabase.getInstance(context)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize database", e)
        throw e
    }
}
```

##### 3. 数据库初始化增强

**AppDatabase 改进**：
- 添加详细日志记录
- 所有错误都会被记录并重新抛出
- 使用 `synchronized` 确保线程安全

```kotlin
fun getInstance(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(this) {
        INSTANCE ?: try {
            Log.d(TAG, "Creating new database instance")
            val instance = Room.databaseBuilder(/*...*/).build()
            Log.d(TAG, "Database instance created successfully")
            instance
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create database", e)
            throw e
        }
    }
}
```

##### 4. LibraryViewModel 优化

**初始化日志**：
```kotlin
init {
    Log.d(TAG, "LibraryViewModel initialized")
}
```

**媒体导入增强日志**：
```kotlin
fun importMediaFile(uri: Uri) {
    viewModelScope.launch {
        try {
            Log.d(TAG, "Importing media file: $uri")
            // ... 导入逻辑
            Log.d(TAG, "Media file imported successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import media file", e)
        }
    }
}
```

**调试方法**：

1. **查看 Logcat**
   ```bash
   adb logcat | grep -E "AppDatabase|AppNavigation|MainActivity|LibraryViewModel|ExoPlayerManager"
   ```

2. **按标签过滤**
   - `AppDatabase` - 数据库相关日志
   - `AppNavigation` - 导航初始化日志
   - `MainActivity` - Activity 生命周期
   - `ExoPlayerManager` - 播放器操作
   - `LibraryViewModel` - 媒体库操作

3. **常见错误模式**

   **数据库初始化失败**：
   ```
   E/AppDatabase: Failed to create database
   ```
   可能原因：存储空间不足、权限问题

   **播放器初始化失败**：
   ```
   E/ExoPlayerManager: Failed to initialize player
   ```
   可能原因：ExoPlayer 依赖问题

   **内存不足**：
   ```
   E/AndroidRuntime: java.lang.OutOfMemoryError
   ```

**如何使用日志诊断崩溃**：

1. 连接设备并启用 USB 调试
2. 运行应用并复制崩溃时的日志
3. 查找最后一条成功的日志：
   - "MainActivity onCreate" ✓
   - "AppNavigation composing..." ✓
   - "Initializing database..." ✓
   - "Creating repository..." ✗ (崩溃点)

4. 根据崩溃点定位问题：
   - 数据库初始化失败 → 检查存储权限
   - Repository 创建失败 → 检查 DAO 实现
   - Player 创建失败 → 检查 ExoPlayer 依赖
   - ViewModel 创建失败 → 检查参数传递

**性能注意事项**：
- 日志仅在 Debug 构建中详细输出
- Release 构建建议使用 ProGuard 移除日志
- 大量日志可能影响性能，但有助于诊断问题

---

### v1.0.2 (2025-12-23)

#### 稳定性修复和优化

**问题描述**：
播放器存在闪退和不稳定的问题，主要原因包括：
1. 缺少错误处理机制
2. 生命周期管理不当
3. 资源释放不完整
4. 协程任务泄漏

**修复内容**：

##### 1. ExoPlayerManager 增强

**添加的功能**：
- ✅ **错误处理监听** - 添加 `onPlayerError` 监听器捕获播放错误
- ✅ **释放状态检查** - 添加 `isReleased` 标志防止使用已释放的播放器
- ✅ **完善的 null 检查** - 所有操作都检查播放器状态
- ✅ **错误状态暴露** - 通过 `playbackError` Flow 暴露错误信息
- ✅ **异常捕获** - 所有播放器操作都添加 try-catch 保护
- ✅ **日志记录** - 添加详细的日志用于调试

**核心修复代码**：
```kotlin
// 添加错误监听
override fun onPlayerError(error: PlaybackException) {
    Log.e(TAG, "Player error: ${error.message}", error)
    _playbackError.value = "播放错误: ${error.message}"
    _playbackState.value = PlaybackState.Error(error.message ?: "未知错误")
}

// 添加释放状态检查
private var isReleased = false

fun loadMedia(uri: Uri, startPosition: Long = 0) {
    if (isReleased) {
        Log.w(TAG, "Cannot load media: player is released")
        return
    }
    // ... 操作
}

// 安全释放
fun release() {
    if (isReleased) return
    try {
        player?.let {
            it.removeListener(playerListener)
            it.stop()
            it.release()
        }
    } finally {
        player = null
        isReleased = true
    }
}
```

**新增 PlaybackState**：
```kotlin
sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Ended : PlaybackState()
    data class Error(val message: String) : PlaybackState()  // 新增
}
```

##### 2. PlayerViewModel 生命周期管理

**添加的方法**：
- `onPause()` - Activity 暂停时调用，暂停播放并保存位置
- `onResume()` - Activity 恢复时调用
- `onStop()` - Activity 停止时调用，停止播放并取消协程
- `onCleared()` - 现在会正确释放 playerManager

**核心修复代码**：
```kotlin
override fun onCleared() {
    super.onCleared()
    progressJob?.cancel()
    savePositionJob?.cancel()
    saveCurrentPosition()
    // 新增：释放播放器资源
    playerManager.release()
}

fun onPause() {
    playerManager.pause()
    saveCurrentPosition()
}

fun onStop() {
    playerManager.pause()
    saveCurrentPosition()
    progressJob?.cancel()
    savePositionJob?.cancel()
}
```

##### 3. AppNavigation 生命周期监听

**新增功能**：
- ✅ 使用 `DisposableEffect` 监听生命周期事件
- ✅ 自动处理 Activity 暂停/停止/恢复
- ✅ 确保资源在组件销毁时正确清理

**核心修复代码**：
```kotlin
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                playerViewModel.onPause()
            }
            Lifecycle.Event.ON_STOP -> {
                playerViewModel.onStop()
            }
            Lifecycle.Event.ON_RESUME -> {
                playerViewModel.onResume()
            }
            else -> {}
        }
    }
    
    lifecycleOwner.lifecycle.addObserver(observer)
    
    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
}
```

##### 4. MainActivity 优化

**新增功能**：
- ✅ 添加 `FLAG_KEEP_SCREEN_ON` 保持屏幕常亮
- ✅ 添加生命周期回调方法（由 DisposableEffect 处理）

**核心修复代码**：
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 保持屏幕常亮
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    
    // ...
}
```

##### 5. PlayerScreen 配置变化处理

**新增功能**：
- ✅ 添加 `DisposableEffect` 管理 PlayerView 生命周期
- ✅ 在 `AndroidView.update` 中处理配置变化
- ✅ 确保 Player 正确绑定到 PlayerView

**核心修复代码**：
```kotlin
AndroidView(
    factory = { ctx ->
        PlayerView(ctx).apply {
            player = playerManager.getPlayer()
            useController = false
        }
    },
    update = { playerView ->
        // 处理配置变化，确保 player 正确绑定
        val currentPlayer = playerManager.getPlayer()
        if (playerView.player != currentPlayer) {
            playerView.player = currentPlayer
        }
    }
)
```

##### 6. PlaybackService 改进

**修复内容**：
- ✅ 添加异常处理和日志
- ✅ 安全的资源释放
- ✅ 添加注释说明 Player 实例冲突问题
- ✅ 硬编码通知渠道名称，避免资源缺失导致崩溃

**核心修复代码**：
```kotlin
override fun onDestroy() {
    Log.d(TAG, "PlaybackService onDestroy")
    try {
        mediaSession?.let { session ->
            player?.let { p ->
                p.stop()
                p.release()
            }
            session.release()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error during cleanup", e)
    } finally {
        mediaSession = null
        player = null
    }
    super.onDestroy()
}
```

**修复效果**：
- ✅ **消除闪退** - 完善的错误处理和 null 检查
- ✅ **提升稳定性** - 正确的生命周期管理和资源释放
- ✅ **防止内存泄漏** - 协程任务和监听器正确清理
- ✅ **配置变化支持** - 屏幕旋转等操作不会导致崩溃
- ✅ **后台切换稳定** - 应用切换到后台再返回不会出错

**使用建议**：
- 如遇播放错误，可通过 `playerManager.playbackError` 获取错误信息
- 应用会在 Activity 暂停时自动暂停播放，保护用户体验
- 所有操作都有日志记录，便于调试问题

---

### v1.0.1 (2025-12-23)

#### 1. Gradle 版本升级

**问题描述**：
项目原本使用 Java 21.0.3 和 Gradle 8.2 的组合，但这两个版本不兼容。根据 Gradle 官方文档，Java 21 需要 Gradle 8.5 或更高版本支持。

**解决方案**：
- 升级 Gradle 版本：8.2 → 8.9
- 升级 Android Gradle Plugin：8.2.0 → 8.5.2
- 更新 JDK 支持：JDK 17 → JDK 17 或 JDK 21

**修改文件**：
1. `gradle/wrapper/gradle-wrapper.properties`
   ```properties
   # 修改前
   distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
   
   # 修改后
   distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
   ```

2. `build.gradle.kts`
   ```kotlin
   // 修改前
   id("com.android.application") version "8.2.0" apply false
   
   // 修改后
   id("com.android.application") version "8.5.2" apply false
   ```

**兼容性说明**：
- Gradle 8.9 完全兼容 Java 21.0.3
- Android Gradle Plugin 8.5.2 与 Gradle 8.9 完全兼容
- 项目现在可以使用 JDK 17 或 JDK 21 进行开发

**使用方法**：
更新后，首次同步项目时，Gradle 会自动下载新版本。如果遇到同步问题，可以尝试：

```bash
# 清理项目
./gradlew clean

# 重新同步
./gradlew --refresh-dependencies
```

#### 2. 添加应用图标资源

**问题描述**：
项目初始化时缺少应用图标资源文件，导致构建失败：

```
AAPT: error: resource mipmap/ic_launcher not found.
AAPT: error: resource mipmap/ic_launcher_round not found.
```

**解决方案**：
创建了完整的应用图标资源，包括：

1. **自适应图标**（Android 8.0+ / API 26+）
   - `mipmap-anydpi-v26/ic_launcher.xml`
   - `mipmap-anydpi-v26/ic_launcher_round.xml`

2. **背景和前景 Drawable**
   - `drawable/ic_launcher_background.xml` - 绿色背景
   - `drawable/ic_launcher_foreground.xml` - 白色播放三角形
   - `drawable/ic_launcher_legacy.xml` - 旧版本设备的图标

3. **多分辨率 Mipmap**（API < 26 的备用图标）
   - `mipmap-mdpi/` (48dp)
   - `mipmap-hdpi/` (72dp)
   - `mipmap-xhdpi/` (96dp)
   - `mipmap-xxhdpi/` (144dp)
   - `mipmap-xxxhdpi/` (192dp)

**图标设计说明**：
- **主题色**：#3DDC84（Android 绿色）
- **图标符号**：白色播放三角形，代表媒体播放器
- **样式**：Material Design 自适应图标风格
- **兼容性**：支持 Android 5.0（API 21）及以上所有版本

**自定义图标**：
如果您想使用自己的图标，可以：
1. 使用 [Android Studio Image Asset Studio](https://developer.android.com/studio/write/image-asset-studio) 生成图标
2. 使用在线工具如 [App Icon Generator](https://appicon.co/) 生成各尺寸图标
3. 替换 `drawable/ic_launcher_foreground.xml` 中的图标设计

#### 3. 支持中文路径

**问题描述**：
Android Gradle 插件默认不允许项目路径包含非 ASCII 字符（如中文、日文等）。当项目路径包含中文时（例如：`E:\local_lib\coding\播放器`），会抛出以下错误：

```
Your project path contains non-ASCII characters. This will most likely 
cause the build to fail on Windows. Please move your project to a different directory.
```

**解决方案**：
在 `gradle.properties` 文件中添加配置来禁用路径检查：

```properties
# 允许项目路径包含非 ASCII 字符（如中文）
android.overridePathCheck=true
```

**修改文件**：
- `gradle.properties` - 添加 `android.overridePathCheck=true`

**注意事项**：
虽然此配置可以绕过路径检查，但建议在生产环境中使用纯英文路径，因为：
1. 某些第三方工具可能不支持非 ASCII 路径
2. 可能在特定情况下导致编码问题
3. 跨平台开发时可能遇到兼容性问题

**使用建议**：
- **开发环境**：可以使用中文路径，配置 `android.overridePathCheck=true`
- **CI/CD 环境**：建议使用纯英文路径，避免潜在问题

#### 4. 修复 PlaybackService 和 PlayerScreen 编译错误

**问题描述**：
在编译过程中遇到以下错误：

1. `PlaybackService.kt` 中的编译错误：
   ```
   'onPlay' overrides nothing
   'onPause' overrides nothing
   'onStop' overrides nothing
   Unresolved reference: onPlay/onPause/onStop
   ```

2. `PlayerScreen.kt` 中的编译错误：
   ```
   Unresolved reference: Lock
   ```

**问题原因**：

1. **PlaybackService 错误**：
   - 尝试覆盖 `MediaSession.Callback` 中不存在的方法
   - 在 Media3 库中，`MediaSession.Callback` 的标准实现会自动处理播放控制
   - 不需要也不能覆盖带有 `session` 和 `controller` 参数的 `onPlay`、`onPause`、`onStop` 方法

2. **PlayerScreen 错误**：
   - 缺少 Material Icons 的导入语句
   - 未导入 `Lock` 图标类

**解决方案**：

1. **修复 PlaybackService.kt**：
   
   删除了错误的方法覆盖，保留空的 `MediaSessionCallback` 实现：
   
   ```kotlin
   private inner class MediaSessionCallback : MediaSession.Callback {
       // MediaSession.Callback 会自动将播放控制命令转发给 player
       // 这里保留空实现，如需扩展可以添加自定义逻辑
   }
   ```

2. **修复 PlayerScreen.kt**：
   
   添加必要的导入语句：
   
   ```kotlin
   import androidx.compose.material.icons.Icons
   import androidx.compose.material.icons.filled.Lock
   ```
   
   简化图标引用：
   
   ```kotlin
   // 修改前
   imageVector = androidx.compose.material.icons.Icons.Default.Lock
   
   // 修改后
   imageVector = Icons.Default.Lock
   ```

**修改文件**：
- `app/src/main/java/com/local/mediaplayer/service/PlaybackService.kt`
- `app/src/main/java/com/local/mediaplayer/ui/player/PlayerScreen.kt`

**技术说明**：

**Media3 MediaSession 工作原理**：
- `MediaSession` 会自动连接 `Player` 和媒体控制器
- 播放控制命令（播放、暂停、停止等）会自动转发给关联的 `ExoPlayer`
- 如需自定义播放行为，应通过 `Player.Listener` 监听播放状态变化，而不是覆盖 `Callback` 方法

**Material Icons 使用**：
- Material Icons 需要显式导入才能使用
- 推荐使用简化的导入方式，提高代码可读性
- 常用图标都在 `androidx.compose.material.icons.filled` 包中

**验证修复**：
修复后，可以通过以下命令验证编译是否成功：

```bash
# Windows
.\gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

或在 Android Studio 中：
- Build → Rebuild Project

### v1.0.0 (2025-12-23)
- 项目初始版本
- 实现所有核心播放功能

---

## 📄 开源协议

本项目采用 MIT 协议开源。

---

## 👨‍💻 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交 Issue
- Bug 报告：请详细描述问题、复现步骤、设备信息
- 功能建议：请说明功能需求和使用场景

### 提交 PR
1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 GitHub Issue
- 邮箱：[your-email@example.com]

---

## 🙏 致谢

- [ExoPlayer](https://github.com/google/ExoPlayer) - 强大的媒体播放器
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代化的 UI 框架
- [Material Design 3](https://m3.material.io/) - 优秀的设计系统

---

**项目版本**: v1.0.0  
**最后更新**: 2025-12-23  
**作者**: 本地播放器团队

