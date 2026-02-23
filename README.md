# VideoMaster — Android 本地视频播放器

> 纯 Java · 无联网 · Android 10+（API 29+）

---

## 项目概览

VideoMaster 是一款功能完整的本地视频播放器，使用纯 Java 开发，基于 ExoPlayer（Media3）内核，提供流畅的播放体验、多格式字幕支持、丰富的手势操作和优美的深色 UI。

---

## 功能列表

| 功能 | 说明 |
|------|------|
| 视频库浏览 | 自动扫描设备 MediaStore，网格展示视频列表 |
| 导入视频 | 通过系统文件选择器导入任意视频，也支持从其他 App 分享打开 |
| 多倍速播放 | 0.5× / 1× / 1.25× / 1.5× / 1.75× / 2× / 2.5× / 3× |
| 进度条 | 可拖拽 SeekBar，实时显示当前时间 / 总时长 |
| 前进/后退 5 秒 | 点击控制区左右按钮快速跳转 ±5 秒 |
| 长按 2.5× 快进 | 长按屏幕激活 2.5× 速，松手立即恢复，顶部提示条确认状态 |
| 双击暂停/播放 | 双击视频区域切换播放状态 |
| 字幕导入 | 支持 SRT、VTT、ASS/SSA 格式，自动检测编码 |
| 字幕导出 | 可将已加载字幕导出为 SRT 或 VTT 文件 |
| 横竖屏切换 | 点击旋转按钮或系统自动旋转均可，配置变更不中断播放 |
| 全屏沉浸模式 | 自动隐藏状态栏 / 导航栏，手势召回 |
| 控制栏自动隐藏 | 3 秒无操作后控制区渐隐，单击恢复 |
| 完全离线 | 网络安全配置禁止所有明文/加密流量，无任何联网请求 |

---

## 项目结构

```
app/src/main/
├── java/com/videomaster/app/
│   ├── MainActivity.java              # 视频库首页
│   ├── PlayerActivity.java            # 播放器主页面
│   ├── interfaces/
│   │   ├── ISubtitleParser.java       # 字幕解析器接口（扩展点）
│   │   ├── ISubtitleExporter.java     # 字幕导出器接口（扩展点）
│   │   ├── IPlayerEventListener.java  # 播放事件监听接口（扩展点）
│   │   ├── ISpeedProvider.java        # 速度表供应接口（扩展点）
│   │   └── IVideoLibraryProvider.java # 视频库数据源接口（扩展点）
│   ├── model/
│   │   └── VideoItem.java             # 视频数据模型
│   ├── player/
│   │   ├── PlayerManager.java         # ExoPlayer 封装，分发所有事件
│   │   ├── GestureHandler.java        # 手势检测（单击/双击/长按）
│   │   └── DefaultSpeedProvider.java  # 默认速度表
│   ├── subtitle/
│   │   ├── SubtitleEntry.java         # 字幕条目模型
│   │   ├── SubtitleManager.java       # 字幕注册中心，解析/导出/查找
│   │   ├── SrtParser.java             # SRT 解析器
│   │   ├── VttParser.java             # WebVTT 解析器
│   │   ├── AssParser.java             # ASS/SSA 解析器
│   │   └── SubtitleExporter.java      # SRT / VTT 导出器
│   ├── ui/
│   │   ├── SubtitleView.java          # 自定义字幕渲染 View
│   │   └── VideoAdapter.java          # RecyclerView 适配器
│   └── util/
│       ├── TimeUtils.java             # 时间格式化与解析
│       ├── FileUtils.java             # 文件/URI 工具
│       └── PermissionUtils.java       # 动态权限工具
└── res/
    ├── layout/
    │   ├── activity_main.xml          # 视频库布局
    │   ├── activity_player.xml        # 播放器布局
    │   └── item_video.xml             # 视频列表卡片
    ├── drawable/                      # 矢量图标 + 背景 Drawable
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── xml/
        ├── file_paths.xml             # FileProvider 路径配置
        └── network_security_config.xml# 禁用所有网络
```

---

## 依赖库

| 库 | 版本 | 用途 |
|----|------|------|
| androidx.media3:media3-exoplayer | 1.3.0 | 视频解码与播放内核 |
| androidx.media3:media3-ui | 1.3.0 | PlayerView 组件 |
| com.google.android.material | 1.11.0 | Material Design 3 组件 |
| androidx.recyclerview | 1.3.2 | 视频库网格列表 |
| androidx.constraintlayout | 2.1.4 | 布局引擎 |
| androidx.documentfile | 1.0.1 | SAF 文档文件访问 |

---

## 构建与运行

### 环境要求

- Android Studio Hedgehog 2023.1.1 或更高
- JDK 8+
- Android SDK：compileSdk 34，minSdk 29

### 步骤

```bash
# 克隆/下载项目后，在 Android Studio 中打开根目录
# 同步 Gradle，连接 Android 10+ 设备或模拟器
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 手势操作说明

| 手势 | 动作 |
|------|------|
| 单击 | 显示/隐藏控制栏 |
| 双击 | 切换播放/暂停 |
| 长按（不松手） | 激活 2.5× 速度快进 |
| 松开长按 | 恢复之前的播放速度 |
| 拖拽进度条 | 精确跳转播放位置 |

---

## 字幕使用说明

### 导入字幕

1. 播放视频时点击右上角字幕图标（CC）
2. 选择「导入字幕」，从文件选择器选取 `.srt` / `.vtt` / `.ass` / `.ssa` 文件
3. 字幕将自动与播放进度同步显示

### 导出字幕

1. 在字幕菜单中选择「导出为 SRT」或「导出为 VTT」
2. 系统文件选择器选择保存路径
3. 文件以 UTF-8 编码写出

### 支持格式

| 格式 | 扩展名 | 导入 | 导出 |
|------|--------|------|------|
| SubRip | `.srt` | ✅ | ✅ |
| WebVTT | `.vtt` | ✅ | ✅ |
| ASS/SSA | `.ass` `.ssa` | ✅ | — |

---

## 扩展开发指南

### 添加新字幕格式

实现 `ISubtitleParser` 接口，在 `Application` 或首次使用前注册：

```java
SubtitleManager.getInstance().registerParser(new MyCustomParser());
```

`ISubtitleParser` 需实现：
- `getFormatName()` — 格式名称
- `getSupportedExtensions()` — 文件扩展名数组（含点）
- `parse(InputStream, String charset)` — 返回 `List<SubtitleEntry>`

### 添加新字幕导出格式

实现 `ISubtitleExporter` 接口：

```java
SubtitleManager.getInstance().registerExporter(new MyCustomExporter());
```

### 自定义速度表

实现 `ISpeedProvider`，在创建 `PlayerManager` 后替换：

```java
playerManager.setSpeedProvider(new MySpeedProvider());
```

### 监听播放事件

实现 `IPlayerEventListener` 并注册：

```java
playerManager.addListener(myListener);
// 记得在销毁时移除
playerManager.removeListener(myListener);
```

回调方法：`onPlaybackStarted` / `onPlaybackPaused` / `onPlaybackCompleted` / `onPositionChanged` / `onSpeedChanged` / `onError` / `onBufferingChanged`

### 自定义视频数据源

实现 `IVideoLibraryProvider` 可替换视频库来源（如网络 NAS、云盘等），该接口已预留但当前未启用联网功能。

---

## 权限说明

| 权限 | Android 版本 | 原因 |
|------|-------------|------|
| `READ_EXTERNAL_STORAGE` | ≤ 12 | 读取外置存储视频 |
| `READ_MEDIA_VIDEO` | ≥ 13 | 分区存储媒体读取 |
| `WRITE_EXTERNAL_STORAGE` | ≤ 9 | （备用，本项目 minSdk=29 不需要） |
| `WAKE_LOCK` | 全版本 | 播放时保持屏幕常亮 |

---

## 界面截图说明

| 页面 | 特色 |
|------|------|
| 视频库 | 深色网格卡片，显示时长与文件大小，FAB 快速导入 |
| 播放器 | 全屏沉浸，半透明控制叠加层，红色主题进度条 |
| 字幕 | 白色文字 + 半透明圆角背景，居中底部显示 |

---

*VideoMaster v1.0.0 — 纯本地，无联网，完全离线。*
