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
| **超粗透明进度条** | 12dp 高轨道 + 28dp 实心圆滑块，透明背景，视觉清晰、易拖动 |
| 前进/后退 5 秒 | 点击控制区左右按钮快速跳转 ±5 秒 |
| 长按 2.5× 快进 | 长按屏幕激活 2.5× 速，松手立即恢复 |
| 双击暂停/播放 | 双击视频区域切换播放状态 |
| **左侧上划调亮度** | 在屏幕左半边上下滑动调节当前窗口亮度（仅显示百分比数字，无进度条） |
| **手势切换（无需长按）** | 直接滑动即可切换上一首/下一首，方向可在设置中独立配置 |
| **竖屏手势可配置** | 可选"上下滑（右侧）"或"左右滑"切换视频 |
| **横屏手势可配置** | 可选"左右滑"或"上下滑（右侧）"切换视频 |
| **锁屏** | 点击顶栏左侧锁图标锁定所有手势和按钮，防误触；再次点击解锁 |
| **多种播放模式** | 顺序播放 / 乱序播放 / 单曲循环 / 播放一次，点击顶栏模式图标循环切换 |
| **自定义播放列表（内联）** | 创建、重命名、删除列表；在主界面"我的列表"标签直接展开列表，点击即可播放，无需跳转另一页 |
| **播放列表多选导入** | 在列表详情页点击 + 后可同时选择多个文件一次性添加 |
| **播放界面列表面板** | 播放时点击顶栏列表图标，从右侧滑出播放列表面板，点击条目直接切换 |
| **播放进度保存** | 自动保存每个文件的播放位置，在列表中以进度条可视化显示 |
| **内置媒体（自动发现）** | 将音视频文件放入 assets/builtin_media/ 即可自动识别，无需编写 index.json |
| **默认主界面可配置** | 默认显示「内置媒体」标签；可在设置中更改默认主界面 |
| **标签顺序可调整** | 通过设置中的 ↑↓ 按钮，随意调整媒体库/我的列表/内置媒体的底部导航顺序 |
| **方块/列表双模式** | 主界面工具栏一键切换方块视图（2列网格）和传统列表，带真实封面缩略图 |
| **真实媒体封面** | 视频自动加载帧缩略图，音频加载内嵌专辑封面，缩略图异步加载不卡 UI |
| **设置图标常驻工具栏** | 主界面右上角直接显示齿轮图标，一键进入设置 |
| 字幕导入 | 支持 SRT、VTT、ASS/SSA 格式，自动检测编码 |
| **字幕导出（合并按钮）** | 点击「导出字幕」后选择格式（SRT / VTT），操作更简洁 |
| **全屏字幕列表面板** | 播放时点击字幕列表按钮，弹出占满全屏的字幕列表；点击条目跳转时间点，**面板保持不关闭** |
| **时间点显隐切换** | 字幕列表面板支持一键显示/隐藏时间戳，仅看字幕文字 |
| **字幕外观设置** | 在设置中调整字幕文字大小（10–40 sp）和行间距（1.0–3.0） |
| 横竖屏切换 | 点击旋转按钮或系统自动旋转均可 |
| 全屏沉浸模式 | 自动隐藏状态栏 / 导航栏，手势召回 |
| 控制栏自动隐藏 | 3 秒无操作后控制区渐隐，单击恢复 |
| **流畅播放优化** | ExoPlayer 预缓冲 20s / 最大 60s，位置更新加速至 200ms，启动缓冲降至 1s |
| 完全离线 | 网络安全配置禁止所有明文/加密流量，无任何联网请求 |

---

## 项目结构

```
app/src/main/
├── java/com/videomaster/app/
│   ├── MainActivity.java              # 媒体库首页（底部导航三标签，含内联播放列表）
│   ├── PlayerActivity.java            # 播放器主页面（含字幕列表面板、全屏展开）
│   ├── SettingsActivity.java          # 独立设置界面（字幕外观设置 v6.0 新增）
│   ├── interfaces/
│   │   ├── ISubtitleParser.java
│   │   ├── ISubtitleExporter.java
│   │   ├── IPlayerEventListener.java
│   │   ├── ISpeedProvider.java
│   │   └── IVideoLibraryProvider.java
│   ├── model/
│   │   ├── VideoItem.java
│   │   └── MediaList.java
│   ├── player/
│   │   ├── PlayerManager.java         # ExoPlayer 封装（缓冲进一步优化）
│   │   ├── GestureHandler.java        # 手势处理（修复空指针崩溃）
│   │   ├── PlayMode.java
│   │   └── DefaultSpeedProvider.java
│   ├── playlist/
│   │   ├── MediaListManager.java
│   │   ├── PlaylistActivity.java      # 已弃用（功能内嵌到 MainActivity）
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
│   │   ├── SubtitleView.java          # 支持动态字幕大小和行间距
│   │   ├── VideoAdapter.java          # GRID / LIST 双模式 + 异步缩略图
│   │   ├── SubtitleListAdapter.java
│   │   └── PlayerPlaylistAdapter.java
│   └── util/
│       ├── TimeUtils.java
│       ├── FileUtils.java
│       ├── PermissionUtils.java
│       └── BuiltinMediaProvider.java  # 支持自动发现（v6.0）
└── res/
    ├── layout/
    │   ├── activity_main.xml          # 新增内联播放列表 Tab（v6.0）
    │   ├── activity_player.xml        # 字幕面板改为全屏；亮度指示器移除进度条
    │   ├── activity_settings.xml      # 新增字幕大小/行间距 SeekBar（v6.0）
    │   ├── activity_playlist.xml
    │   ├── activity_playlist_detail.xml
    │   ├── item_video.xml
    │   ├── item_video_list.xml
    │   ├── item_subtitle_entry.xml
    │   ├── item_playlist.xml
    │   ├── item_media_progress.xml
    │   └── item_player_playlist.xml
    ├── drawable/
    │   ├── seekbar_progress.xml       # 12dp 粗进度条，透明背景（v6.0）
    │   ├── seekbar_thumb.xml          # 28dp 滑块（v6.0）
    │   ├── ic_settings.xml            # 齿轮设置图标（v6.0）
    │   └── ...（其他图标）
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── themes.xml
```

---

## v6.0 新增/修改详解

### 1. 进度条视觉升级

- **轨道高度**：8dp → **12dp**（更粗，更易拖动）
- **背景透明度**：`#44FFFFFF` → `#22FFFFFF`（更透明，视觉更干净）
- **滑块尺寸**：22dp → **28dp**（更大，手指更容易精准点击）

---

### 2. 导出字幕合并按钮

字幕菜单中"导出为 SRT"和"导出为 VTT"合并为单一入口「**导出字幕**」。
点击后弹出格式选择弹窗（SRT / VTT），再选择保存路径。

---

### 3. 设置图标常驻工具栏

主界面右上角不再是溢出菜单（三个点），而是直接显示**齿轮图标**，点击即进入设置界面。

---

### 4. 亮度调节优化

- **移除亮度进度条**：调节亮度时仅显示一个小方块 + 百分比数字（如 `72%`），更简洁
- 触发区域不变：屏幕左半部分纵向滑动

---

### 5. 「我的列表」内联展示（不再跳转）

| 旧版本 | v6.0 |
|--------|------|
| 点击「我的列表」Tab → 跳转到 PlaylistActivity | 直接在主界面内展开，无页面跳转 |
| 列表内容无缩略图 | **列表项目使用 VideoAdapter 展示，有缩略图、方块/列表布局** |
| 需要返回键退出 | 点击列表进入详情，点击返回键回到列表层级 |

#### 操作流程
1. 底部导航点击「我的列表」→ 展示所有播放列表
2. 点击某个列表 → 内联展开该列表的媒体项目（带缩略图）
3. 点击媒体项目 → 进入播放器，整个列表作为播放队列
4. 点击返回键 → 回到播放列表一览
5. 右下角 + → 创建新列表；进入列表详情后 + → 添加媒体文件（支持多选）
6. 长按列表条目 → 重命名 / 删除

---

### 6. 手势切换崩溃修复

`GestureHandler.onScroll()` 中对 `e1` 和 `e2` 均增加了 null 检查（Android 高版本中两者均可能为 null），彻底修复左右/上下滑切换媒体时的崩溃问题。

---

### 7. 字幕面板全屏 + 外观可调

#### 字幕列表面板全屏
- 字幕列表面板高度从固定 350dp 改为 `match_parent`（全屏显示）
- 从底部滑入/滑出动画不变

#### 点击字幕条目不关闭面板
- 旧版：点击条目 → 跳转时间点 + 关闭面板
- v6.0：点击条目 → 跳转时间点，**面板保持打开**，可继续点击其他字幕

#### 字幕外观设置（设置界面）

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 字幕大小 | 10–40 sp | 18 sp | 拖动滑块调节 |
| 字幕行间距 | 1.0–3.0 | 1.2 | 同一字幕块内多行文字的行高倍率 |

---

### 8. 内置媒体自动发现

**旧版**：必须维护 `assets/builtin_media/index.json` 才能让内置媒体显示。
**v6.0**：只需将文件放入 `assets/builtin_media/` 目录即可自动识别，**无需 index.json**。

- 自动识别所有主流音视频扩展名（mp4、mkv、avi、mov、wmv、flv、webm、ts、m4v、mp3、aac、flac、wav、ogg、m4a、opus、wma 等）
- 文件名去掉扩展名后作为显示标题
- 若仍有 `index.json`，优先使用 index.json 中的自定义标题和排序

---

### 9. 播放器流畅度再优化

| 参数 | v5.0 | v6.0 |
|------|------|------|
| 最小缓冲时长 | 15s | **20s** |
| 最大缓冲时长 | 50s | **60s** |
| 开始播放所需缓冲 | 1500ms | **1000ms** |
| 重缓冲后恢复 | 3000ms | **2000ms** |
| 位置更新间隔 | 250ms | **200ms**（字幕更流畅） |
| 音频焦点 | 未处理 | `setHandleAudioBecomingNoisy(true)` |

---

## 底部导航说明

| 标签 | 内容 |
|------|------|
| 内置媒体（默认首位） | 展示 assets/builtin_media/ 内置的音视频文件（自动发现） |
| 媒体库 | 自动扫描 MediaStore，展示设备上所有视频和音频 |
| 我的列表 | 内联展示自定义播放列表，点击直接进入列表详情 |

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
| 左侧竖向滑动 | 调节屏幕亮度（上↑亮，下↓暗） | 调节屏幕亮度 | 两种模式均有效 |
| 拖拽进度条 | 精确跳转播放位置 | 精确跳转播放位置 | — |

---

## 字幕使用说明

### 导入字幕
1. 播放时点击顶栏字幕图标（CC）
2. 选择「导入字幕」，从文件选择器选取 `.srt` / `.vtt` / `.ass` / `.ssa` 文件
3. 字幕将自动与播放进度同步显示
4. 导入成功后，顶栏出现**字幕列表图标**，可查看全部字幕并点击跳转

### 查看字幕列表
1. 点击顶栏字幕列表图标 → 全屏弹出字幕列表面板
2. 点击任意条目 → 跳转到该字幕时间点（**面板保持不关闭**）
3. 点击面板头部"隐藏时间"→ 仅显示字幕文字（无时间戳）
4. 点击关闭按钮 → 收回面板

### 导出字幕
1. 在字幕菜单中选择「导出字幕」
2. 弹窗中选择格式（SRT / VTT）
3. 系统文件选择器选择保存路径

### 支持格式

| 格式 | 扩展名 | 导入 | 导出 |
|------|--------|------|------|
| SubRip | `.srt` | ✅ | ✅ |
| WebVTT | `.vtt` | ✅ | ✅ |
| ASS/SSA | `.ass` `.ssa` | ✅ | — |

---

## 应用设置（主界面右上角齿轮图标）

| 设置项 | 说明 |
|--------|------|
| 默认主界面 | 选择 App 启动时默认显示的标签（内置媒体 / 媒体库 / 我的列表） |
| 标签顺序 | 通过 ↑↓ 按钮调整底部三个导航标签的排列顺序 |
| 列表显示模式 | 方块视图（网格，2列）或传统列表 |
| 竖屏切换媒体手势 | 选择竖屏时切换媒体的滑动方向：上下滑（右侧）或左右滑 |
| 横屏切换媒体手势 | 选择横屏时切换媒体的滑动方向：左右滑或上下滑（右侧） |
| 字幕大小 | 播放器字幕文字大小（10–40 sp），拖动滑块调整 |
| 字幕行间距 | 同一字幕块内的行高倍率（1.0–3.0），拖动滑块调整 |

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

### 添加内置媒体（v6.0 起无需 index.json）

直接将音视频文件放入 `assets/builtin_media/` 目录，启动时自动识别。

若需要自定义标题/排序，仍可创建 `index.json`（index.json 优先级更高）：

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

## 修改记录（v6.0）

| 文件 | 修改内容 |
|------|---------|
| `seekbar_progress.xml` | 轨道加粗至 12dp，背景更透明 |
| `seekbar_thumb.xml` | 滑块加大至 28dp |
| `ic_settings.xml` | 新增齿轮图标 |
| `menu_main.xml` | 设置项改为常驻工具栏（showAsAction=always）+ 齿轮图标 |
| `activity_player.xml` | 亮度指示器移除进度条；字幕面板高度改为 match_parent |
| `activity_main.xml` | 新增内联播放列表 Tab（tabPlaylist 含两级视图） |
| `activity_settings.xml` | 新增字幕大小 SeekBar + 行间距 SeekBar |
| `PlayerActivity.java` | 字幕导出合并为一个入口；移除亮度进度条；字幕面板点击不关闭；读取字幕设置 |
| `MainActivity.java` | 完整重写：内联播放列表 Tab，含播放列表创建/重命名/删除/多选导入/缩略图展示 |
| `SettingsActivity.java` | 新增字幕大小/行间距字段和 SeekBar 逻辑 |
| `GestureHandler.java` | onScroll 中同时 null-check e1 和 e2，修复崩溃 |
| `BuiltinMediaProvider.java` | 支持自动扫描 builtin_media/ 目录，无需 index.json |
| `PlayerManager.java` | 缓冲参数进一步优化；添加 handleAudioBecomingNoisy |
| `SubtitleView.java` | 新增 setLineSpacingMultiplier() 动态行间距 API |
| `strings.xml` | 新增字幕设置相关字符串 |

---

*VideoMaster v6.0.0 — 纯本地，无联网，完全离线。*
