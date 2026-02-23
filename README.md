# VideoMaster — Android 本地音视频播放器

> 纯 Java · 无联网 · Android 10+（API 29+）

---

## 项目概览

VideoMaster 是一款功能完整的本地音视频播放器，使用纯 Java 开发，基于 ExoPlayer（Media3）内核，提供流畅的播放体验、多格式字幕支持、丰富的手势操作（含亮度调节、锁屏、切换媒体）、自定义播放列表、多种播放模式、内置媒体管理和优美的深色 UI。

---

## 功能列表

| 功能 | 说明 |
|------|------|
| 媒体库浏览 | 自动扫描设备 MediaStore，展示视频 + 音频列表 |
| 导入媒体 | 通过系统文件选择器导入任意视频/音频（**支持多选**），也支持从其他 App 分享打开 |
| 多倍速播放 | 0.5× / 1× / 1.25× / 1.5× / 1.75× / 2× / 2.5× / 3× |
| **倍速即点即用** | 点击速度标签（如"1×"）弹出选择框，点哪个倍速就立即生效，无需点确认 |
| **粗进度条** | 8dp 高轨道 + 22dp 实心圆滑块，视觉更清晰、更易拖动 |
| 前进/后退 5 秒 | 点击控制区左右按钮快速跳转 ±5 秒 |
| 长按 2.5× 快进 | 长按屏幕激活 2.5× 速，松手立即恢复 |
| 双击暂停/播放 | 双击视频区域切换播放状态 |
| **左侧上划调亮度** | 在屏幕左半边上下滑动调节当前窗口亮度（无图标，仅进度条+百分比） |
| **手势切换（无需长按）** | 直接滑动即可切换上一首/下一首，方向可在设置中独立配置 |
| **竖屏手势可配置** | 可选"上下滑（右侧）"或"左右滑"切换视频 |
| **横屏手势可配置** | 可选"左右滑"或"上下滑（右侧）"切换视频 |
| **锁屏** | 点击顶栏左侧锁图标锁定所有手势和按钮，防误触；再次点击解锁 |
| **多种播放模式** | 顺序播放 / 乱序播放 / 单曲循环 / 播放一次，点击顶栏模式图标循环切换 |
| **自定义播放列表** | 创建、重命名、删除列表；向列表**多选**添加音视频 |
| **播放列表多选导入** | 在列表详情页点击 + 后可同时选择多个文件一次性添加 |
| **播放界面列表面板** | 播放时点击顶栏列表图标，从右侧滑出播放列表面板，点击条目直接切换 |
| **播放进度保存** | 自动保存每个文件的播放位置，在列表中以进度条可视化显示 |
| **内置媒体** | 开发者可在 assets/builtin_media/ 内置音视频文件，随 APK 分发 |
| **默认主界面可配置** | 默认显示「内置媒体」标签；可在设置中更改默认主界面 |
| **标签顺序可调整** | 通过设置中的 ↑↓ 按钮，随意调整媒体库/我的列表/内置媒体的底部导航顺序 |
| **方块/列表双模式** | 主界面工具栏一键切换方块视图（2列网格）和传统列表，带真实封面缩略图 |
| **真实媒体封面** | 视频自动加载帧缩略图，音频加载内嵌专辑封面，缩略图异步加载不卡 UI |
| **独立设置界面** | 主界面右上角菜单进入完整设置页（全屏 Activity），包含所有配置项 |
| 字幕导入 | 支持 SRT、VTT、ASS/SSA 格式，自动检测编码 |
| 字幕导出 | 可将已加载字幕导出为 SRT 或 VTT 文件 |
| **字幕列表面板** | 播放时点击字幕列表按钮，从底部弹出所有字幕条目；点击任意条目跳转到对应时间点 |
| **时间点显隐切换** | 字幕列表面板支持一键显示/隐藏时间戳，仅看字幕文字 |
| 横竖屏切换 | 点击旋转按钮或系统自动旋转均可 |
| 全屏沉浸模式 | 自动隐藏状态栏 / 导航栏，手势召回 |
| 控制栏自动隐藏 | 3 秒无操作后控制区渐隐，单击恢复 |
| **流畅播放优化** | ExoPlayer 预缓冲 15s / 最大 50s，修复字幕索引每帧重置的性能 Bug |
| 完全离线 | 网络安全配置禁止所有明文/加密流量，无任何联网请求 |

---

## 项目结构

```
app/src/main/
├── java/com/videomaster/app/
│   ├── MainActivity.java              # 媒体库首页（底部导航三标签，含视图切换）
│   ├── PlayerActivity.java            # 播放器主页面（含字幕列表面板）
│   ├── SettingsActivity.java          # 独立设置界面（NEW v5.0）
│   ├── interfaces/
│   │   ├── ISubtitleParser.java
│   │   ├── ISubtitleExporter.java
│   │   ├── IPlayerEventListener.java
│   │   ├── ISpeedProvider.java
│   │   └── IVideoLibraryProvider.java
│   ├── model/
│   │   ├── VideoItem.java             # 视频/音频数据模型
│   │   └── MediaList.java             # 自定义播放列表数据模型
│   ├── player/
│   │   ├── PlayerManager.java         # ExoPlayer 封装（含缓冲优化、性能修复）
│   │   ├── GestureHandler.java        # 手势处理
│   │   ├── PlayMode.java              # 播放模式枚举
│   │   └── DefaultSpeedProvider.java
│   ├── playlist/
│   │   ├── MediaListManager.java
│   │   ├── PlaylistActivity.java
│   │   ├── PlaylistDetailActivity.java
│   │   ├── PlaylistAdapter.java
│   │   └── MediaItemListAdapter.java
│   ├── subtitle/
│   │   ├── SubtitleEntry.java
│   │   ├── SubtitleManager.java
│   │   ├── SrtParser.java
│   │   ├── VttParser.java
│   │   ├── AssParser.java
│   │   └── SubtitleExporter.java
│   ├── ui/
│   │   ├── SubtitleView.java
│   │   ├── VideoAdapter.java          # 支持 GRID / LIST 双模式 + 异步缩略图（更新）
│   │   ├── SubtitleListAdapter.java   # 字幕列表适配器（NEW v5.0）
│   │   └── PlayerPlaylistAdapter.java
│   └── util/
│       ├── TimeUtils.java
│       ├── FileUtils.java
│       ├── PermissionUtils.java
│       └── BuiltinMediaProvider.java
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_player.xml        # 新增字幕列表面板（更新）
    │   ├── activity_settings.xml      # 设置页面布局（NEW v5.0）
    │   ├── activity_playlist.xml
    │   ├── activity_playlist_detail.xml
    │   ├── item_video.xml             # 网格视图条目（含真实缩略图，更新）
    │   ├── item_video_list.xml        # 传统列表视图条目（NEW v5.0）
    │   ├── item_subtitle_entry.xml    # 字幕列表条目（NEW v5.0）
    │   ├── item_playlist.xml
    │   ├── item_media_progress.xml
    │   └── item_player_playlist.xml
    ├── drawable/
    │   ├── ic_lock.xml / ic_lock_open.xml
    │   ├── ic_playlist.xml
    │   ├── ic_playlist_panel.xml
    │   ├── ic_mode_*.xml              # 播放模式图标
    │   ├── ic_view_grid.xml           # 网格视图图标（NEW v5.0）
    │   ├── ic_view_list.xml           # 列表视图图标（NEW v5.0）
    │   ├── ic_subtitle_list.xml       # 字幕列表图标（NEW v5.0）
    │   └── selector_panel_item.xml
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── themes.xml
```

---

## 新功能详解（v5.0）

### 1. 亮度调节（无图标）

- **图标已彻底移除**：亮度调节过程中仅显示竖向进度条 + 百分比数字。
- `ic_brightness.xml` 文件已从项目中删除（彻底清理）。
- **触发区域**：屏幕左半部分纵向滑动触发；上划 = 亮度升高，下划 = 亮度降低。

---

### 2. 独立设置界面（NEW）

主界面右上角 **⋮ → 设置** 进入全屏设置 Activity（不再是对话框）。

| 设置项 | 说明 |
|--------|------|
| 默认主界面 | 选择 App 启动时默认显示的标签（内置媒体 / 媒体库 / 我的列表） |
| 标签顺序 | 通过 ↑↓ 按钮调整底部三个导航标签的排列顺序 |
| 列表显示模式 | 切换方块视图（2列网格）或传统列表 |
| 竖屏切换媒体手势 | 选择竖屏时切换媒体的滑动方向 |
| 横屏切换媒体手势 | 选择横屏时切换媒体的滑动方向 |

> 点击"保存并返回"后立即生效，返回主界面时自动应用新配置。

---

### 3. 方块/列表双模式 + 真实封面（NEW）

#### 切换方式
- 主界面工具栏右侧有一个**视图切换按钮**，点击即可在方块和列表模式间切换。
- 切换状态保存至 SharedPreferences，重启后保留。

#### 方块视图（Grid）
- 2 列网格展示
- 每个条目显示封面缩略图、标题、时长、文件大小

#### 传统列表（List）
- 单列展示，左侧正方形封面 + 右侧三行信息（标题、时长、大小）

#### 封面缩略图
- **视频**：调用 `ContentResolver.loadThumbnail()` 异步加载帧缩略图（API 29+）
- **音频**：通过 `MediaMetadataRetriever` 提取内嵌专辑封面
- 封面异步加载，不阻塞主线程；加载前显示默认播放圆圈图标
- 缩略图本地缓存（内存 HashMap），滑动流畅不重复加载

---

### 4. 字幕列表面板（NEW）

导入字幕后，播放界面顶栏会出现一个**字幕列表按钮**（CC 样式图标）。

#### 使用方法
1. 导入字幕文件后，顶栏自动显示字幕列表按钮
2. 点击按钮 → 底部弹出字幕列表面板（高度 350dp，可滚动）
3. 面板中按顺序展示所有字幕条目
4. **当前播放位置对应的字幕**会以红色高亮背景标记，并自动滚动到可视区
5. **点击任意条目** → 立即跳转到该字幕的开始时间点，面板自动关闭
6. 面板顶部有**显示时间 / 隐藏时间**切换按钮 → 一键隐藏时间戳，仅显示字幕文字
7. 点击面板顶部关闭按钮收回面板

#### 时间点格式
```
00:01:23  →  00:01:25
字幕文字内容
```

---

### 5. 播放流畅度优化（NEW）

#### ExoPlayer 缓冲配置优化
| 参数 | 旧值（默认） | 新值 |
|------|------------|------|
| 最小缓冲时长 | 15s（默认） | 15s |
| 最大缓冲时长 | 50s | **50s**（显式设置） |
| 开始播放所需缓冲 | 2500ms | **1500ms**（更快启动） |
| 重缓冲后恢复缓冲 | 5000ms | **3000ms**（更快恢复） |

#### 关键 Bug 修复
- **字幕搜索索引每帧重置 Bug**：原代码在 positionUpdater（每 250ms 执行）中调用 `subtitleManager.onSeek()`，该方法会将字幕搜索索引重置为 0，导致每次位置更新都从头线性扫描所有字幕条目（O(n) 开销）。修复后，`onSeek()` 仅在用户手动拖拽进度条时调用，字幕搜索使用增量前向扫描，大幅降低 CPU 占用。

---

## 底部导航说明

| 标签 | 内容 |
|------|------|
| 内置媒体（默认首位） | 展示 assets/builtin_media/ 内置的音视频文件 |
| 媒体库 | 自动扫描 MediaStore，展示设备上所有视频和音频 |
| 我的列表 | 打开自定义播放列表管理页 |

> 标签顺序和默认选中标签均可在**设置**中自由调整。

---

## 手势操作说明（完整）

| 手势 | 竖屏 | 横屏 | 备注 |
|------|------|------|------|
| 单击 | 显示/隐藏控制栏 | 显示/隐藏控制栏 | 锁屏状态下无效 |
| 双击 | 切换播放/暂停 | 切换播放/暂停 | 锁屏状态下无效 |
| 长按（不移动） | 激活 2.5× 速度 | 激活 2.5× 速度 | 松手恢复 |
| 右侧向下/向上滑 | 切换下一首/上一首（默认上下滑模式） | 可配置为上下滑模式 | 需有播放列表上下文 |
| 向左/向右横滑 | 可配置为左右滑模式 | 切换下一首/上一首（默认左右滑模式） | 需有播放列表上下文 |
| 左侧竖向滑动 | 调节屏幕亮度（上↑亮，下↓暗，无图标） | 调节屏幕亮度 | 两种模式均有效 |
| 拖拽进度条 | 精确跳转播放位置 | 精确跳转播放位置 | — |

---

## 字幕使用说明

### 导入字幕
1. 播放时点击顶栏字幕图标（CC）
2. 选择「导入字幕」，从文件选择器选取 `.srt` / `.vtt` / `.ass` / `.ssa` 文件
3. 字幕将自动与播放进度同步显示
4. 导入成功后，顶栏出现**字幕列表图标**，可查看全部字幕并点击跳转

### 查看字幕列表
1. 点击顶栏字幕列表图标 → 底部弹出面板
2. 点击任意条目 → 跳转到该字幕时间点
3. 点击面板头部"隐藏时间"→ 仅显示字幕文字（无时间戳）

### 导出字幕
1. 在字幕菜单中选择「导出为 SRT」或「导出为 VTT」
2. 系统文件选择器选择保存路径

### 支持格式

| 格式 | 扩展名 | 导入 | 导出 |
|------|--------|------|------|
| SubRip | `.srt` | ✅ | ✅ |
| WebVTT | `.vtt` | ✅ | ✅ |
| ASS/SSA | `.ass` `.ssa` | ✅ | — |

---

## 应用设置（主界面右上角 ⋮ → 设置）

| 设置项 | 说明 |
|--------|------|
| 默认主界面 | 选择 App 启动时默认显示的标签（内置媒体 / 媒体库 / 我的列表） |
| 标签顺序 | 通过 ↑↓ 按钮调整底部三个导航标签的排列顺序 |
| 列表显示模式 | 方块视图（网格，2列）或传统列表 |
| 竖屏切换媒体手势 | 选择竖屏时切换媒体的滑动方向：上下滑（右侧）或左右滑 |
| 横屏切换媒体手势 | 选择横屏时切换媒体的滑动方向：左右滑或上下滑（右侧） |

---

## 依赖库

| 库 | 版本 | 用途 |
|----|------|------|
| androidx.media3:media3-exoplayer | 1.3.0 | 视频/音频解码与播放内核 |
| androidx.media3:media3-ui | 1.3.0 | PlayerView 组件 |
| com.google.android.material | 1.11.0 | Material Design 3 组件 |
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
# 在 Android Studio 中打开根目录，同步 Gradle
# 连接 Android 10+ 设备或模拟器后运行
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 权限说明

| 权限 | Android 版本 | 原因 |
|------|-------------|------|
| `READ_EXTERNAL_STORAGE` | ≤ 12 | 读取外置存储媒体 |
| `READ_MEDIA_VIDEO` | ≥ 13 | 分区存储视频读取 |
| `READ_MEDIA_AUDIO` | ≥ 13 | 分区存储音频读取 |
| `WAKE_LOCK` | 全版本 | 播放时保持屏幕常亮 |

---

## 扩展开发指南

### 添加内置媒体

1. 将音视频文件放入 `assets/builtin_media/`
2. 更新 `index.json`：

```json
[
  {
    "filename": "demo.mp4",
    "title": "演示视频",
    "category": "示例",
    "durationMs": 60000,
    "type": "video"
  }
]
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

### 添加新字幕格式

```java
SubtitleManager.getInstance().registerParser(new MyCustomParser());
```

---

## 新增文件（v5.0）

| 文件 | 用途 |
|------|------|
| `SettingsActivity.java` | 独立设置界面（替换原对话框） |
| `res/layout/activity_settings.xml` | 设置页面布局 |
| `ui/SubtitleListAdapter.java` | 字幕列表适配器（支持时间戳显隐、高亮） |
| `res/layout/item_subtitle_entry.xml` | 字幕列表条目布局 |
| `res/layout/item_video_list.xml` | 列表模式媒体条目布局 |
| `res/drawable/ic_view_grid.xml` | 网格视图切换图标 |
| `res/drawable/ic_view_list.xml` | 列表视图切换图标 |
| `res/drawable/ic_subtitle_list.xml` | 字幕列表面板图标 |

## 修改文件（v5.0）

| 文件 | 修改内容 |
|------|---------|
| `PlayerManager.java` | ExoPlayer 缓冲配置优化；修复 positionUpdater 中调用 onSeek() 的性能 Bug |
| `VideoAdapter.java` | 支持 GRID / LIST 双布局模式；异步加载真实缩略图（视频帧/音频专辑封面） |
| `item_video.xml` | 添加真实缩略图 ImageView + fallback 图标 |
| `PlayerActivity.java` | 添加字幕列表面板逻辑；清除字幕/导入字幕时刷新按钮可见性 |
| `activity_player.xml` | 添加字幕列表面板（底部上滑）及其按钮 |
| `MainActivity.java` | 改为启动 SettingsActivity；添加视图切换工具栏按钮 |
| `menu_main.xml` | 添加视图切换按钮（常驻工具栏） |
| `strings.xml` | 新增设置、字幕列表相关字符串 |
| `AndroidManifest.xml` | 注册 SettingsActivity |
| `res/drawable/ic_brightness.xml` | 已删除（旧图标，布局中已无引用） |

---

*VideoMaster v5.0.0 — 纯本地，无联网，完全离线。*
