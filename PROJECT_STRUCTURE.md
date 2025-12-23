# 项目文件结构

## 完整文件列表

```
播放器L2/
├── README.md                              # 项目主文档
├── ARCHITECTURE.md                        # 架构设计文档
├── TESTING.md                             # 测试指南
├── PROJECT_STRUCTURE.md                   # 本文件
├── .gitignore                             # Git 忽略配置
│
├── build.gradle.kts                       # 根级 Gradle 配置
├── settings.gradle.kts                    # Gradle 设置
├── gradle.properties                      # Gradle 属性
│
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties      # Gradle Wrapper 配置
│
└── app/
    ├── build.gradle.kts                   # 应用级 Gradle 配置
    ├── proguard-rules.pro                 # ProGuard 混淆规则
    │
    └── src/
        └── main/
            ├── AndroidManifest.xml        # 应用清单
            │
            ├── java/com/localmedia/player/
            │   ├── MediaPlayerApplication.kt          # Application 类
            │   │
            │   ├── data/                              # 数据层
            │   │   ├── MediaDatabase.kt              # Room 数据库
            │   │   ├── entity/                        # 实体类
            │   │   │   ├── MediaItem.kt              # 媒体条目实体
            │   │   │   ├── Category.kt               # 分类实体
            │   │   │   └── PlaybackHistory.kt        # 播放历史实体
            │   │   ├── dao/                           # DAO 接口
            │   │   │   ├── MediaItemDao.kt           # 媒体条目 DAO
            │   │   │   ├── CategoryDao.kt            # 分类 DAO
            │   │   │   └── PlaybackHistoryDao.kt     # 播放历史 DAO
            │   │   └── repository/                    # 数据仓库
            │   │       └── MediaRepository.kt        # 媒体数据仓库
            │   │
            │   ├── player/                            # 播放器核心
            │   │   └── ExoPlayerManager.kt           # ExoPlayer 管理器
            │   │
            │   ├── service/                           # 服务层
            │   │   └── PlaybackService.kt            # 后台播放服务
            │   │
            │   ├── ui/                                # UI 层
            │   │   ├── MainActivity.kt                # 主 Activity
            │   │   ├── MainNavigation.kt              # 导航配置
            │   │   ├── screen/                        # 界面
            │   │   │   ├── PlayerScreen.kt           # 播放器界面
            │   │   │   └── MediaListScreen.kt        # 媒体列表界面
            │   │   └── theme/                         # 主题
            │   │       ├── Theme.kt                   # 主题配置
            │   │       ├── Color.kt                   # 颜色定义
            │   │       └── Type.kt                    # 字体定义
            │   │
            │   └── utils/                             # 工具类
            │       ├── SubtitleParser.kt             # 字幕解析器
            │       ├── TranscriptParser.kt           # 文稿解析器
            │       ├── GestureHandler.kt             # 手势处理器
            │       └── OrientationManager.kt         # 屏幕方向管理器
            │
            └── res/                                   # 资源文件
                ├── drawable/
                │   └── ic_launcher_foreground.xml     # 应用图标前景
                ├── mipmap-anydpi-v26/
                │   ├── ic_launcher.xml                # 自适应图标
                │   └── ic_launcher_round.xml          # 圆形图标
                ├── values/
                │   ├── strings.xml                    # 字符串资源
                │   ├── colors.xml                     # 颜色资源
                │   └── themes.xml                     # 主题资源
                └── (其他 mipmap 文件夹需要添加图标图片)
```

## 核心文件说明

### 配置文件 (4 个)

| 文件 | 用途 |
|------|------|
| `build.gradle.kts` (根) | 项目级 Gradle 配置，定义插件版本 |
| `app/build.gradle.kts` | 应用级配置，定义依赖、编译选项、ABI 拆分 |
| `settings.gradle.kts` | Gradle 设置，定义仓库和模块 |
| `gradle.properties` | Gradle 属性配置 |

### 数据层 (9 个)

| 文件 | 用途 |
|------|------|
| `MediaDatabase.kt` | Room 数据库实例 |
| `entity/MediaItem.kt` | 媒体文件实体（包含播放进度） |
| `entity/Category.kt` | 分类实体 |
| `entity/PlaybackHistory.kt` | 播放历史记录 |
| `dao/MediaItemDao.kt` | 媒体条目数据访问 |
| `dao/CategoryDao.kt` | 分类数据访问 |
| `dao/PlaybackHistoryDao.kt` | 播放历史数据访问 |
| `repository/MediaRepository.kt` | 数据仓库统一接口 |

### 播放器层 (2 个)

| 文件 | 用途 |
|------|------|
| `player/ExoPlayerManager.kt` | ExoPlayer 封装，播放控制、倍速、跳转 |
| `service/PlaybackService.kt` | 后台播放服务、通知栏控制 |

### UI 层 (7 个)

| 文件 | 用途 |
|------|------|
| `ui/MainActivity.kt` | 主 Activity |
| `ui/MainNavigation.kt` | Compose 导航配置 |
| `ui/screen/PlayerScreen.kt` | 播放器界面（核心，包含所有手势处理） |
| `ui/screen/MediaListScreen.kt` | 媒体列表界面 |
| `ui/theme/Theme.kt` | Material 3 主题配置 |
| `ui/theme/Color.kt` | 颜色定义 |
| `ui/theme/Type.kt` | 字体定义 |

### 工具层 (4 个)

| 文件 | 用途 |
|------|------|
| `utils/SubtitleParser.kt` | 解析 SRT、WebVTT 字幕 |
| `utils/TranscriptParser.kt` | 解析文稿时间点 |
| `utils/GestureHandler.kt` | 手势识别和处理（亮度、音量） |
| `utils/OrientationManager.kt` | 横竖屏自动切换 |

### 资源文件 (6 个)

| 文件 | 用途 |
|------|------|
| `AndroidManifest.xml` | 应用清单（权限、Activity、Service） |
| `res/values/strings.xml` | 字符串资源 |
| `res/values/colors.xml` | 颜色资源 |
| `res/values/themes.xml` | 主题资源 |
| `res/drawable/ic_launcher_foreground.xml` | 应用图标 |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | 自适应图标 |

### 文档文件 (3 个)

| 文件 | 用途 |
|------|------|
| `README.md` | 项目主文档（功能说明、使用指南） |
| `ARCHITECTURE.md` | 架构设计文档（详细设计说明） |
| `TESTING.md` | 测试指南（测试用例清单） |

## 统计数据

- **Kotlin 源文件**：26 个
- **资源文件**：6 个
- **配置文件**：5 个
- **文档文件**：4 个
- **总计**：41 个文件

## 代码行数估算

| 类别 | 行数 |
|------|------|
| ExoPlayerManager | ~300 行 |
| PlayerScreen (UI + 手势) | ~400 行 |
| SubtitleParser | ~200 行 |
| TranscriptParser | ~150 行 |
| GestureHandler | ~120 行 |
| 数据库层 (Entity + DAO) | ~400 行 |
| 其他 UI 和工具 | ~500 行 |
| **总计** | **~2000 行** |

## 依赖项统计

- **Jetpack Compose**: 7 个库
- **ExoPlayer (Media3)**: 4 个库
- **Room**: 3 个库
- **Kotlin**: 3 个库
- **其他**: 2 个库
- **测试**: 5 个库

**总计**: 24 个依赖项

## 关键实现亮点

### 1. ExoPlayerManager.kt (核心播放器)
- ✅ 完整的倍速控制（0.75x - 3.0x）
- ✅ 长按临时 2.5x 加速实现
- ✅ StateFlow 响应式状态管理
- ✅ 前进/后退 15 秒

### 2. PlayerScreen.kt (播放器界面)
- ✅ 完整的手势系统（双击、长按、滑动）
- ✅ 亮度调节（无需系统权限）
- ✅ 音量调节（AudioManager）
- ✅ 锁屏模式
- ✅ 自定义 UI 控件

### 3. SubtitleParser.kt (字幕解析)
- ✅ SRT 格式解析
- ✅ WebVTT 格式解析
- ✅ 时间同步显示

### 4. TranscriptParser.kt (文稿解析)
- ✅ 多种时间格式支持
- ✅ 点击跳转功能
- ✅ 自动高亮当前位置

### 5. GestureHandler.kt (手势处理)
- ✅ 垂直滑动识别
- ✅ 水平滑动识别
- ✅ 亮度/音量计算
- ✅ 左右区域判断

## 待完善部分

### 必须添加的文件
1. **图标图片**：各尺寸的 launcher 图标
   - mipmap-mdpi/ic_launcher.png
   - mipmap-hdpi/ic_launcher.png
   - mipmap-xhdpi/ic_launcher.png
   - mipmap-xxhdpi/ic_launcher.png
   - mipmap-xxxhdpi/ic_launcher.png

### 建议添加的功能（可选）
1. ViewModel 层（目前直接在 Composable 中管理状态）
2. 依赖注入（Hilt/Koin）
3. 单元测试实现
4. CI/CD 配置

## 快速导航

### 要修改播放器核心逻辑？
→ `app/src/main/java/com/localmedia/player/player/ExoPlayerManager.kt`

### 要修改 UI 界面？
→ `app/src/main/java/com/localmedia/player/ui/screen/PlayerScreen.kt`

### 要添加手势功能？
→ `app/src/main/java/com/localmedia/player/utils/GestureHandler.kt`

### 要修改数据库？
→ `app/src/main/java/com/localmedia/player/data/entity/` + DAO

### 要调整依赖？
→ `app/build.gradle.kts`

### 要修改权限？
→ `app/src/main/AndroidManifest.xml`

---

**项目结构清晰，模块化良好，易于维护和扩展！** 📁

