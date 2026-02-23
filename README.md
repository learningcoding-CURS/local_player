# VideoMaster — Android 本地音视频播放器

> 纯 Java · 无联网 · Android 10+（API 29+）

---

## 项目概览

VideoMaster 是一款功能完整的本地音视频播放器，使用纯 Java 开发，基于 ExoPlayer（Media3）内核，提供流畅的播放体验、多格式字幕支持、丰富的手势操作（含亮度/音量调节、锁屏、切换媒体）、自定义播放列表、内置媒体管理和优美的深色 UI。

---

## 功能列表

| 功能 | 说明 |
|------|------|
| 媒体库浏览 | 自动扫描设备 MediaStore，展示视频 + 音频列表 |
| 导入媒体 | 通过系统文件选择器导入任意视频/音频，也支持从其他 App 分享打开 |
| 多倍速播放 | 0.5× / 1× / 1.25× / 1.5× / 1.75× / 2× / 2.5× / 3× |
| 进度条 | 可拖拽 SeekBar，实时显示当前时间 / 总时长 |
| 前进/后退 5 秒 | 点击控制区左右按钮快速跳转 ±5 秒 |
| 长按 2.5× 快进 | 长按屏幕激活 2.5× 速，松手立即恢复 |
| 双击暂停/播放 | 双击视频区域切换播放状态 |
| **左侧上划调亮度** | 在屏幕左半边上下滑动调节当前窗口亮度，右侧调音量 |
| **锁屏** | 点击顶栏左侧锁图标锁定所有手势和按钮，防误触；再次点击解锁 |
| **长按横滑切换** | 长按屏幕激活快进后继续横向滑动可切换上一首/下一首 |
| **自定义播放列表** | 创建、重命名、删除列表；支持分类；添加/移除音视频条目 |
| **播放进度保存** | 自动保存每个文件的播放位置，在列表中以进度条可视化显示 |
| **内置媒体** | 开发者可在 assets/builtin_media/ 内置音视频文件，随 APK 分发 |
| 字幕导入 | 支持 SRT、VTT、ASS/SSA 格式，自动检测编码 |
| 字幕导出 | 可将已加载字幕导出为 SRT 或 VTT 文件 |
| 横竖屏切换 | 点击旋转按钮或系统自动旋转均可 |
| 全屏沉浸模式 | 自动隐藏状态栏 / 导航栏，手势召回 |
| 控制栏自动隐藏 | 3 秒无操作后控制区渐隐，单击恢复 |
| 完全离线 | 网络安全配置禁止所有明文/加密流量，无任何联网请求 |

---

## 项目结构

```
app/src/main/
├── java/com/videomaster/app/
│   ├── MainActivity.java              # 媒体库首页（底部导航三标签）
│   ├── PlayerActivity.java            # 播放器主页面
│   ├── interfaces/
│   │   ├── ISubtitleParser.java
│   │   ├── ISubtitleExporter.java
│   │   ├── IPlayerEventListener.java
│   │   ├── ISpeedProvider.java
│   │   └── IVideoLibraryProvider.java
│   ├── model/
│   │   ├── VideoItem.java             # 视频/音频数据模型
│   │   └── MediaList.java             # 自定义播放列表数据模型（NEW）
│   ├── player/
│   │   ├── PlayerManager.java         # ExoPlayer 封装
│   │   ├── GestureHandler.java        # 手势（点击/双击/长按/竖滑/横划）
│   │   └── DefaultSpeedProvider.java
│   ├── playlist/
│   │   ├── MediaListManager.java      # 播放列表 JSON 持久化（NEW）
│   │   ├── PlaylistActivity.java      # 播放列表管理页（NEW）
│   │   ├── PlaylistDetailActivity.java# 播放列表内条目页（NEW）
│   │   ├── PlaylistAdapter.java       # 列表适配器（NEW）
│   │   └── MediaItemListAdapter.java  # 条目适配器，含进度条（NEW）
│   ├── subtitle/
│   │   ├── SubtitleEntry.java
│   │   ├── SubtitleManager.java
│   │   ├── SrtParser.java
│   │   ├── VttParser.java
│   │   ├── AssParser.java
│   │   └── SubtitleExporter.java
│   ├── ui/
│   │   ├── SubtitleView.java
│   │   └── VideoAdapter.java
│   └── util/
│       ├── TimeUtils.java
│       ├── FileUtils.java
│       ├── PermissionUtils.java       # 增加 READ_MEDIA_AUDIO
│       └── BuiltinMediaProvider.java  # 读取 assets 内置媒体（NEW）
└── res/
    ├── layout/
    │   ├── activity_main.xml          # 含底部导航 + 三标签（更新）
    │   ├── activity_player.xml        # 含锁屏/亮度/音量覆盖层（更新）
    │   ├── activity_playlist.xml      # 播放列表管理布局（NEW）
    │   ├── activity_playlist_detail.xml # 播放列表详情布局（NEW）
    │   ├── item_video.xml
    │   ├── item_playlist.xml          # 播放列表卡片（NEW）
    │   └── item_media_progress.xml    # 带进度条的媒体条目（NEW）
    ├── drawable/
    │   ├── ic_lock.xml                # 锁图标（NEW）
    │   ├── ic_lock_open.xml           # 解锁图标（NEW）
    │   ├── ic_brightness.xml          # 亮度图标（NEW）
    │   ├── ic_playlist.xml            # 播放列表图标（NEW）
    │   ├── ic_music.xml               # 音频图标（NEW）
    │   ├── ic_home.xml                # 主页图标（NEW）
    │   ├── ic_builtin.xml             # 内置媒体图标（NEW）
    │   └── ic_delete.xml              # 删除图标（NEW）
    ├── menu/
    │   └── bottom_nav_menu.xml        # 底部导航菜单（NEW）
    ├── color/
    │   └── bottom_nav_tint.xml        # 导航选中色选择器（NEW）
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml                # 新增多组字符串
    │   └── themes.xml
    └── xml/
        ├── file_paths.xml
        └── network_security_config.xml
assets/
└── builtin_media/
    └── index.json                     # 内置媒体元数据（NEW）
```

---

## 新功能详解

### 1. 亮度 / 音量手势

| 手势 | 动作 |
|------|------|
| 在屏幕**左半边**上下滑动 | 调节当前窗口亮度（不影响系统亮度） |
| 在屏幕**右半边**上下滑动 | 调节媒体音量 |

- 调节时屏幕侧边会弹出半透明指示器（垂直进度条 + 百分比数字），1.5 秒后自动消失。
- 亮度范围：1%–100%；音量随设备最大音量比例缩放。

---

### 2. 锁屏功能

- 播放时点击顶栏左侧 **锁图标（🔒）** 即可锁定屏幕。
- 锁定后：所有手势（单击/双击/长按/滑动）均被屏蔽，控制栏隐藏。
- 仅显示一个**解锁按钮**（左上角）。点击后恢复正常。
- 适用场景：防止口袋/手持时误触跳进度。

---

### 3. 长按横滑切换媒体

- 在有**播放列表上下文**时（从播放列表详情页打开），长按屏幕后**不松手**，然后向左/右滑动：
  - 向左滑 → 切换到**下一个**媒体
  - 向右滑 → 切换到**上一个**媒体
- 切换时自动保存当前进度，并从目标文件上次播放位置恢复。
- 播放完毕后会自动切换到列表中的下一项。

---

### 4. 自定义音视频列表

通过底部导航栏的「**我的列表**」标签进入：

#### 创建列表
1. 点击右下角 **+** 按钮。
2. 输入列表名称和可选分类（如：电影、音乐）。

#### 管理列表
- 长按列表项 → 弹出「重命名」或「删除」选项。

#### 向列表添加媒体
两种方式：
1. 在**播放列表详情页**点击右下角 **+**，通过系统文件选择器选择音视频文件。
2. 在**播放器**里点击顶栏播放列表图标，选择目标列表后直接添加当前正在播放的文件。

#### 播放列表中播放
- 在列表详情页点击任意条目 → 以整个列表为上下文打开播放器（支持长按横滑切换）。
- 长按条目 → 弹出「从此处开始播放」或「从列表中移除」。

---

### 5. 播放进度可视化

- 每次暂停、切换或退出播放时，自动保存该文件的当前位置。
- 在**播放列表详情页**，每个条目下方会显示一条红色进度条和百分比，直观展示观看进度。
- 重新打开该条目时，自动从上次位置继续播放。

---

### 6. 内置媒体

开发者可以将音视频文件打包进 APK 随 App 发布：

1. 将文件放入 `app/src/main/assets/builtin_media/` 目录。
2. 在 `index.json` 中注册：

```json
[
  {
    "filename": "demo.mp4",
    "title": "演示视频",
    "category": "示例",
    "durationMs": 60000,
    "type": "video"
  },
  {
    "filename": "demo.mp3",
    "title": "背景音乐",
    "category": "音乐",
    "durationMs": 180000,
    "type": "audio"
  }
]
```

3. 通过主界面底部导航 **「内置媒体」** 标签浏览和播放。

**注意**：`index.json` 中列出但在 assets 目录中不存在的文件会被自动跳过。

---

## 底部导航说明

主界面包含三个标签：

| 标签 | 内容 |
|------|------|
| 媒体库 | 自动扫描 MediaStore，展示设备上所有视频和音频（按修改时间倒序） |
| 我的列表 | 打开自定义播放列表管理页 |
| 内置媒体 | 展示 assets/builtin_media/ 内置的音视频文件 |

---

## 依赖库

| 库 | 版本 | 用途 |
|----|------|------|
| androidx.media3:media3-exoplayer | 1.3.0 | 视频/音频解码与播放内核 |
| androidx.media3:media3-ui | 1.3.0 | PlayerView 组件 |
| com.google.android.material | 1.11.0 | Material Design 3 组件（BottomNavigationView 等） |
| androidx.recyclerview | 1.3.2 | 各类列表 |
| androidx.constraintlayout | 2.1.4 | 布局引擎 |
| androidx.documentfile | 1.0.1 | SAF 文档文件访问 |
| androidx.cardview | 1.0.0 | 卡片式列表项 |

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

## 手势操作说明（完整）

| 手势 | 动作 | 备注 |
|------|------|------|
| 单击 | 显示/隐藏控制栏 | 锁屏状态下无效 |
| 双击 | 切换播放/暂停 | 锁屏状态下无效 |
| 长按（不松手） | 激活 2.5× 速度快进 | 松手恢复；若期间横划则切换曲目 |
| 长按 + 向左滑 | 切换到下一个媒体 | 需有播放列表上下文 |
| 长按 + 向右滑 | 切换到上一个媒体 | 需有播放列表上下文 |
| 左侧竖向滑动 | 调节屏幕亮度 | 上划增加，下划降低 |
| 右侧竖向滑动 | 调节媒体音量 | 上划增加，下划降低 |
| 拖拽进度条 | 精确跳转播放位置 | — |

---

## 字幕使用说明

### 导入字幕

1. 播放时点击顶栏字幕图标（CC）
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

## 权限说明

| 权限 | Android 版本 | 原因 |
|------|-------------|------|
| `READ_EXTERNAL_STORAGE` | ≤ 12 | 读取外置存储媒体 |
| `READ_MEDIA_VIDEO` | ≥ 13 | 分区存储视频读取 |
| `READ_MEDIA_AUDIO` | ≥ 13 | 分区存储音频读取（新增） |
| `WAKE_LOCK` | 全版本 | 播放时保持屏幕常亮 |

---

## 扩展开发指南

### 添加内置媒体

1. 将音视频文件放入 `assets/builtin_media/`
2. 更新 `index.json` 注册文件信息

### 添加新字幕格式

实现 `ISubtitleParser` 接口并注册：

```java
SubtitleManager.getInstance().registerParser(new MyCustomParser());
```

### 自定义速度表

```java
playerManager.setSpeedProvider(new MySpeedProvider());
```

### 监听播放事件

```java
playerManager.addListener(myListener);
// 销毁时移除
playerManager.removeListener(myListener);
```

回调方法：`onPlaybackStarted` / `onPlaybackPaused` / `onPlaybackCompleted` / `onPositionChanged` / `onSpeedChanged` / `onError` / `onBufferingChanged`

---

## 构建问题修复记录

### `progressBarStyleVertical` 资源找不到

**错误信息：**
```
resource android:attr/progressBarStyleVertical not found.
error: failed linking file resources.
```

**原因：** `android:attr/progressBarStyleVertical` 在 Android API 26（Oreo）中已被移除，本项目 `minSdk=29`，因此完全不可用。

**受影响文件：** `app/src/main/res/layout/activity_player.xml`（第 74、114 行）

**修复方法：** 将两处竖向 `ProgressBar` 的 `style` 改为标准水平样式，并通过 `android:rotation="270"` 旋转实现竖向视觉效果：

```xml
<!-- 修复前 -->
<ProgressBar
    style="?android:attr/progressBarStyleVertical"
    ... />

<!-- 修复后 -->
<ProgressBar
    style="@android:style/Widget.ProgressBar.Horizontal"
    android:rotation="270"
    ... />
```

此修复适用于亮度指示条（`brightnessBar`）和音量指示条（`volumeBar`）。

---

*VideoMaster v2.0.0 — 纯本地，无联网，完全离线。*
