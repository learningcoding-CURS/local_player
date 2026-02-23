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
| **播放列表自定义缩略图** | 长按播放列表项目可设置任意图片作为缩略图，或清除恢复默认 |
| **手势切换媒体（全面修复）** | VERTICAL 模式全屏任意位置上下划均可切换（移除右半屏限制）；HORIZONTAL 模式全屏左右划有效；亮度调节在切换手势触发后自动停止 |
| **字幕列表再次点击暂停/播放** | 字幕列表面板中，第一次点击某条字幕跳转时间点；再次点击同一条时暂停/继续播放 |
| **字幕默认显示可配置** | 设置中新增开关，控制进入播放器时字幕文字是否默认可见 |
| **“我的列表”列表模式封面** | 列表格式下每个播放列表条目展示封面图（自定义图或默认播放列表图标） |
| **字幕跳转保持隐藏状态** | 在字幕列表点击条目跳转后，若字幕文字已被隐藏则不会意外重新显示 |
| **字幕专用显隐按鈕** | 播放界面新增独立按鈕：只切换字幕文字可见性，不关闭字幕列表面板 |
| **播放控件排序可调** | 设置中可调整顶栏/中央栏按鈕的排列顺序，颜色选择改为弹出对话框 |
| **"我的列表"方块/列表双模式** | 主界面工具栏切换视图时同步作用于播放列表卡片；2 列方格与传统列表行均支持长按设置/清除自定义封面 |
| **播放列表封面图修复** | 保存封面图改为后台线程执行；设置 Bitmap 前 clearColorFilter()，消除颜色滤镜遮盖问题 |
| **播放界面播放列表面板缩略图** | 面板内每个条目展示异步加载的封面缩略图（视频帧截图或音频专辑封面），有缓存机制避免重复解码 |
| **播放列表面板方向可配置** | 设置界面支持分别为竖屏和横屏设置面板滑出方向（上/下/左/右），动态调整布局和内入动画 |
| **播放控件全面可配置** | 设置中可控制每个播放按钮的显隐和颜色（白/红/橙/青/绿/黄） |
| **字幕库多选导入** | 字幕库界面支持一次选择多个字幕文件批量添加 |
| **播放界面列表面板** | 播放时点击顶栏列表图标，从右侧滑出播放列表面板，点击条目直接切换 |
| **播放进度保存** | 自动保存每个文件的播放位置，在列表中以进度条可视化显示 |
| **内置媒体（自动发现）** | 将音视频文件放入 assets/builtin_media/ 即可自动识别，无需编写 index.json |
| **默认主界面可配置** | 默认显示「内置媒体」标签；可在设置中更改默认主界面 |
| **标签顺序可调整** | 通过设置中的 ↑↓ 按钮，随意调整媒体库/我的列表/内置媒体的底部导航顺序 |
| **方块/列表双模式** | 主界面工具栏一键切换方块视图（2列网格）和传统列表，带真实封面缩略图 |
| **真实媒体封面** | 视频自动加载帧缩略图，音频加载内嵌专辑封面，缩略图异步加载不卡 UI |
| **设置图标常驻工具栏** | 主界面右上角直接显示齿轮图标，一键进入设置 |
| 字幕导入 | 支持 SRT、VTT、ASS/SSA、**TXT**、**Markdown(.md)** 格式，自动检测编码；TXT/MD 自动按段落拆分并推算显示时长 |
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
│   ├── PlayerActivity.java            # 播放器主页面（dispatchTouchEvent 手势修复；字幕显隐按钮；按钮排序）
│   ├── SettingsActivity.java          # 设置界面（按钮排序/颜色弹出选择 v7.0 新增）
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
│   │   ├── GestureHandler.java        # 手势处理（feedSwipeEvent Activity 级别拦截，彻底修复切换）
│   │   ├── PlayMode.java
│   │   └── DefaultSpeedProvider.java
│   ├── playlist/
│   │   ├── MediaListManager.java
│   │   ├── PlaylistActivity.java      # 已弃用（功能内嵌到 MainActivity）
│   │   ├── PlaylistDetailActivity.java
│   │   ├── PlaylistAdapter.java       # 支持 GRID/LIST 模式 + 异步缩略图
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

| 格式 | 扩展名 | 导入 | 导出 | 说明 |
|------|--------|------|------|------|
| SubRip | `.srt` | ✅ | ✅ | 标准时间轴字幕 |
| WebVTT | `.vtt` | ✅ | ✅ | Web 字幕格式 |
| ASS/SSA | `.ass` `.ssa` | ✅ | — | 高级字幕格式 |
| 纯文本 | `.txt` | ✅ | — | 自动按段落分割，依阅读速度推算每条显示时长（2–8 秒） |
| Markdown | `.md` `.markdown` | ✅ | — | 自动剥离 `#`/`**`/链接等语法标记，内容处理同 TXT |

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

---

## v6.1 新增：TXT / Markdown 字幕导入

### 功能说明

| 格式 | 处理逻辑 |
|------|---------|
| `.txt` | 以空行为段落分隔符；无空行时按中英文句号/问号/感叹号断句；每段落生成一条字幕条目 |
| `.md` `.markdown` | 在 TXT 基础上先剥离 Markdown 语法（标题 `#`、加粗 `**`、斜体 `*`、链接 `[]()`、图片 `![]()`、代码块 ` ``` `、HTML 标签等），再按段落分割 |

### 时长推算模型

```
显示时长 = max(2s, min(8s, 字符数 × 60s / 300字/分钟))
条目间间隔 = 300ms
```

例如：50 字的段落 → 显示约 10s（上限截为 8s）；10 字的段落 → 显示 2s（下限）。

### 新增文件

| 文件 | 说明 |
|------|------|
| `subtitle/TxtParser.java` | 纯文本字幕解析器 |
| `subtitle/MarkdownParser.java` | Markdown 字幕解析器（含语法剥离） |

### 修改文件

| 文件 | 修改内容 |
|------|---------|
| `subtitle/SubtitleManager.java` | 注册 TxtParser、MarkdownParser |
| `PlayerActivity.java` | 文件选择器 MIME 类型加入 `text/plain`、`text/markdown`、`text/x-markdown`、`application/octet-stream` |
| `strings.xml` | 字幕导入提示文字加入 TXT / MD |

---

---

## v7.0 新增/修改详解

### 1. 跳转按钮（前进/后退）可配置

在「设置」中新增三项控制：

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 按钮大小 | 32–96 dp | 64 dp | 控制区中央跳转按钮的图标尺寸 |
| 跳转时长 | 1–60 秒 | 5 秒 | 单次点击跳转的秒数 |
| 按钮透明度 | 10–100% | 100% | 跳转按钮图标的不透明度 |

### 2. 进度条可配置

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 进度条粗细 | 4–20 dp | 12 dp | 轨道高度 |
| 已播放透明度 | 10–100% | 80% | 已播放部分颜色的不透明度（低值更透明，可看见底层轨道） |

进度条的样式（高度和已播放颜色透明度）在播放器启动时读取设置并以代码动态生成 Drawable，实现实时变更。

### 3. 字幕库（新功能）

新增独立「字幕库」管理界面：

- 所有从本地导入的字幕文件自动保存到应用内部存储（`filesDir/subtitle_lib/`）
- 支持**添加**（从本地文件选择器）、**重命名**、**删除**字幕文件
- 点击条目即可选中并在播放器中加载该字幕

**访问方式：**
- 播放器中单击顶栏字幕图标（CC）→ 弹出"从字幕库选择"/"从本地文件选择"两个入口
- 从本地文件导入字幕时，会同时**自动保存到字幕库**，方便后续复用

**双击字幕按钮：** 快速清除/隐藏当前字幕（400ms 内两次点击触发）

### 4. 字幕列表面板改造

#### 外观
- **背景透明化**：面板背景透明度可在设置中调节（0–90%），隔着字幕列表能看见视频画面
- **顶部拖拽条**：面板顶部新增白色横条拖拽标记，视觉提示可以下拉关闭

#### 交互
- **下拉关闭**：手指向下滑动拖拽条或标题栏区域 ≥ 80dp 即关闭面板
- **系统返回键**：字幕列表打开时，按返回键仅关闭字幕列表，不退出播放器
- **关闭按钮**：右上角改为向下箭头图标（↓），更直观

#### 布局精简
- 移除面板顶部大标题"字幕列表"
- "显示/隐藏时间戳"按钮移至左侧，占据空出的空间
- 字幕 CC 按钮的弹出菜单中移除「导出字幕」和「清除字幕」两个选项，仅保留字幕来源选择

### 5. 手势崩溃修复（全面加固）

对 `GestureHandler` 的 `onTouch()` 和 `onScroll()` 回调全面增加 try-catch 保护：
- `onTouch`：MotionEvent null 检查 + 整体 try-catch
- `onScroll`：整体 try-catch
- `handleNormalScroll`：listener null 检查

这些修复解决了在「媒体库」「内置媒体」「我的列表」三个入口打开播放器后，左右/上下手势切换视频时的偶发崩溃问题。

### 6. 播放器流畅度再优化

| 参数 | v6.x | v7.0 |
|------|------|------|
| 最小缓冲时长 | 20s | **25s** |
| 最大缓冲时长 | 60s | **90s** |
| 开始播放所需缓冲 | 1000ms | **800ms** |
| 重缓冲后恢复 | 2000ms | **1500ms** |
| 位置更新间隔 | 200ms | **150ms**（字幕更流畅）|

---

## 字幕库使用说明

### 添加字幕到库
1. 播放时单击顶栏字幕图标（CC）→ 选择「从本地文件选择」
2. 字幕自动加载并**同时保存到字幕库**，下次可直接从库中选择

### 从字幕库加载字幕
1. 单击字幕图标（CC）→ 选择「从字幕库选择」
2. 点击列表中的字幕文件名即可加载

### 管理字幕库（字幕库界面）
- 从播放器字幕选择对话框进入，或通过其他入口打开 `SubtitleLibraryActivity`
- 点 FAB（+）添加新文件
- 长按条目 → 重命名 / 删除

### 隐藏字幕
- **双击**字幕图标（CC）快速清除当前字幕

---

## 应用设置（v7.0 完整）

| 设置项 | 说明 |
|--------|------|
| 默认主界面 | 选择 App 启动时默认显示的标签 |
| 标签顺序 | 通过 ↑↓ 按钮调整底部三个导航标签的排列顺序 |
| 列表显示模式 | 方块视图（网格，2列）或传统列表 |
| 竖屏切换媒体手势 | 上下滑（右侧）或左右滑 |
| 横屏切换媒体手势 | 左右滑或上下滑（右侧） |
| 字幕大小 | 10–40 sp，默认 18 sp |
| 字幕行间距 | 1.0–3.0，默认 1.2 |
| **跳转按钮大小** | 32–96 dp，默认 64 dp |
| **跳转时长** | 1–60 秒，默认 5 秒 |
| **跳转按钮透明度** | 10–100%，默认 100% |
| **进度条粗细** | 4–20 dp，默认 12 dp |
| **已播放透明度** | 10–100%，默认 80%（半透明） |
| **字幕面板透明度** | 0–90%，默认 60%（可透过看见视频） |

---

## v7.0 修改记录

| 文件 | 修改内容 |
|------|---------|
| `PlayerActivity.java` | 跳转按钮/进度条/字幕面板动态应用设置；字幕双击清除；字幕导入弹出菜单简化；onBackPressed 拦截字幕面板 |
| `SettingsActivity.java` | 新增6项播放器设置键（跳转大小/秒数/透明度、进度条高度/透明度、字幕面板透明度）及对应 SeekBar 逻辑 |
| `activity_settings.xml` | 新增跳转按钮/进度条/字幕面板三组 SeekBar |
| `activity_player.xml` | 字幕面板移除标题；关闭按钮改为↓箭头；顶部加拖拽条 |
| `SubtitleLibraryActivity.java` | 新增字幕库界面，支持添加/重命名/删除 |
| `SubtitleLibraryManager.java` | 新增字幕文件管理器（内部存储） |
| `SubtitleFileInfo.java` | 新增字幕文件信息数据类 |
| `SubtitleManager.java` | 新增 `loadSubtitlesFromFile(File, String)` 方法 |
| `activity_subtitle_library.xml` | 新增字幕库界面布局 |
| `ic_arrow_down.xml` | 新增向下箭头图标 |
| `player/GestureHandler.java` | onTouch/onScroll 增加 try-catch 和 null 检查，彻底修复手势崩溃 |
| `player/PlayerManager.java` | 缓冲参数再优化（最大 90s）；位置更新加速至 150ms |
| `strings.xml` | 新增字幕库、跳转/进度条/字幕面板设置相关字符串 |
| `AndroidManifest.xml` | 注册 SubtitleLibraryActivity |

---

*VideoMaster v7.0.0 — 纯本地，无联网，完全离线。*

---

---

## v8.0 新增/修改详解

### 1. 删除「添加到列表」按钮

播放界面顶栏移除了「添加到列表」（btnAddToPlaylist）按钮，简化播放界面。

---

### 2. 跳转按钮图标更换（无数字）

前进/后退跳转按钮图标改为纯双三角箭头样式（⏩/⏪），不再显示任何数字，视觉更简洁。

---

### 3. 播放列表图标移位 & 醒目颜色

- 「播放列表面板」按钮（播放时查看当前播放列表）现在紧挨在倍速标签右侧
- 默认使用醒目的品牌红色（`#E94560`）着色，便于快速找到
- 在设置中可更改为白/红/橙/青/绿/黄 等颜色

---

### 4. 播放控件全面可配置（v8.0 新增设置区块）

在「设置」页面新增「播放控件（显隐 · 颜色）」区块，可对以下控件独立设置：

| 控件 | 显示开关 | 颜色选项 |
|------|---------|---------|
| 锁屏按钮 | ✅ | 白/红/橙/青/绿/黄 |
| 播放模式 | ✅ | 白/红/橙/青/绿/黄 |
| 播放列表面板 | ✅ | 白/红/橙/青/绿/黄（默认红） |
| 字幕按钮 | ✅ | 白/红/橙/青/绿/黄 |
| 字幕列表 | ✅ | 白/红/橙/青/绿/黄 |
| 旋转按钮 | ✅ | 白/红/橙/青/绿/黄 |
| 跳转按钮（前进/后退） | — | 白/红/橙/青/绿/黄 |
| 播放/暂停按钮 | — | 白/红/橙/青/绿/黄 |

---

### 5. 手势切换视频修复（彻底覆盖所有来源）

此前「媒体库」标签直接打开播放器时未传入播放列表，导致手势切换无效。

**修复**：媒体库标签点击任意项目时，整个当前媒体列表作为播放队列传入播放器，与「内置媒体」和「我的列表」行为一致。现在三个入口均支持手势滑动切换视频。

---

### 6. 播放列表缩略图修复（支持 SAF 内容 URI）

通过系统文件选择器（SAF）添加到播放列表的视频，之前因为 `ContentResolver.loadThumbnail()` 不支持文档 URI 而显示不出缩略图。

**修复**：在 `VideoAdapter.fetchThumbnail()` 中添加 `MediaMetadataRetriever` 回退路径：
1. 优先尝试 `ContentResolver.loadThumbnail()`（快，支持 MediaStore URI）
2. 若失败则使用 `MediaMetadataRetriever.getFrameAtTime()`（通用，支持任意内容 URI）

---

### 7. 播放列表点击闪退修复

- 使用 `holder.getAdapterPosition()` 代替捕获的 `position` 变量，防止 RecyclerView 回收后点击位置偏移
- 启动 PlayerActivity 时加入 `FLAG_GRANT_READ_URI_PERMISSION`，确保 SAF URI 播放权限
- 点击启动播放器加 try-catch 保护，崩溃时改为弹出友好提示

---

### 8. 字幕库支持多选导入

字幕库界面点 FAB（+）时文件选择器已启用 `EXTRA_ALLOW_MULTIPLE`，可一次选中多个字幕文件批量导入，导入数量以 Toast 提示。

---

### 9. 我的列表 · 自定义缩略图

在「我的列表」详情视图（播放列表项目）中，长按任意条目弹出选项：

| 选项 | 说明 |
|------|------|
| 设置自定义缩略图 | 从系统图库/文件选择图片，裁剪后保存到 `filesDir/thumbs/` |
| 清除自定义缩略图 | 恢复默认自动缩略图 |
| 从列表中移除 | 将该项目从播放列表删除 |

自定义缩略图路径保存在独立 SharedPreferences（`custom_thumbnails`），键为媒体 URI 字符串，值为图片文件绝对路径。`VideoAdapter` 在加载缩略图前先检查此映射，有自定义图则优先显示。

---

## v8.0 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_player.xml` | 移除 btnAddToPlaylist；将 btnPlaylistPanel 移至 tvSpeed 右侧；btnPlaylistPanel tint 改为 @color/colorAccent |
| `ic_rewind.xml` | 替换为无数字双三角左箭头（⏪） |
| `ic_forward.xml` | 替换为无数字双三角右箭头（⏩） |
| `PlayerActivity.java` | 移除所有 btnAddToPlaylist 相关代码；新增 applyButtonSettings / applyButtonColor / resolveButtonColor 方法；从设置读取各按钮的显隐和颜色并应用 |
| `SettingsActivity.java` | 新增 8 组按钮的显隐/颜色 Pref 常量；新增播放控件设置区块（Switch + RadioGroup 颜色选择）；保存逻辑同步 |
| `activity_settings.xml` | 新增 `playerControlsContainer` 容器和区块标题 |
| `VideoAdapter.java` | `fetchThumbnail()` 对视频添加 MediaMetadataRetriever 回退路径，支持 SAF URI；检查 `custom_thumbnails` SharedPreferences 中的自定义缩略图；新增 `OnItemLongClickListener` |
| `MainActivity.java` | 媒体库标签改用 `openPlayerWithList` 传入完整列表；playlist 详情点击改用 URI 字符串索引防位置偏移；加 try-catch 防崩溃；新增自定义缩略图选取/保存逻辑；`showPlaylistItemOptions()` 长按菜单 |
| `MediaItemListAdapter.java` | 使用 `getAdapterPosition()` 代替捕获的 `position`，防止 IndexOutOfBoundsException |
| `SubtitleLibraryActivity.java` | 文件选择器启用 `EXTRA_ALLOW_MULTIPLE`；新增 `processPickerResult()` 处理多选 |
| `strings.xml` | 新增自定义缩略图、字幕库多选、播放控件设置相关字符串 |

---

## v9.0.0 变更记录

| 文件 | 变更内容 |
|------|---------|
| `GestureHandler.java` | **彻底重构手势切换**：新增 `feedSwipeEvent(ev, windowWidth)` 方法供 Activity 级别调用；使用 `ACTION_DOWN` 时自己存储起点坐标（不再依赖可能被系统回收的 `e1`）；新增 `detectSwipe()` 统一处理两条路径的滑动检测；用共享 `swipeConsumed` 标志防止双重触发；修复旧版在控件覆盖层可见时手势完全失效的根本问题 |
| **字幕专用显隐按鈕** | 播放界面新增独立按鈕：只切换字幕文字可见性，不关闭字幕列表面板 |
| `activity_player.xml` | 新增 `btnSubtitleToggle` ImageButton（眼睛图标），插入字幕相关按钮组 |
| `SettingsActivity.java` | **颜色选择改为弹出式**：每个按钮的颜色设置改为单个色块圆按钮，点击弹出颜色选择对话框；新增 `btnSubtitleToggle` 的显隐/颜色配置项；新增"顶栏按钮排序"与"中央栏按钮排序"区块，支持 ↑↓ 调整顺序；新增 `PREF_TOP_BTN_ORDER` / `PREF_CENTER_BTN_ORDER` 偏好键 |
| `PlaylistAdapter.java` | 重写：支持 `GRID` / `LIST` 双模式；GRID 模式使用 `item_playlist_grid.xml` 方形单元格；异步从 `playlist_thumbnails` SharedPreferences 加载自定义封面，回退显示默认播放列表图标 |
| `item_playlist_grid.xml` | 新增：方形网格单元布局，包含封面 ImageView（高度由 adapter 动态设为与宽度相等）+ 名称 + 分类标签 + 条目数量 |
| `MainActivity.java` | 播放列表标签（`recyclerPlaylists`）改用 2 列 `GridLayoutManager`；长按 MediaList 卡片弹出「设置封面 / 清除封面 / 重命名 / 删除」菜单；新增 `savePlaylistThumbnail()` / `clearPlaylistThumbnail()` 方法；新增 `playlistThumbPickerLauncher` 图片选取 Launcher |
| `ic_subtitle_toggle.xml` | 新增：眼睛图标（字幕显示状态） |
| `ic_subtitle_off.xml` | 新增：带斜线字幕图标（字幕隐藏状态） |
| `strings.xml` | 新增字幕显隐、按钮排序、播放列表封面等相关字符串 |


---

## v10.0 变更记录

| 文件 | 变更内容 |
|------|----------|
| item_player_playlist.xml | 播放界面侧边播放列表条目新增缩略图区域（72×52dp FrameLayout）：包含 imgPanelThumb（视频帧/音频封面）、imgPanelThumbFallback（默认图标）、序号角标 |
| PlayerPlaylistAdapter.java | 完全重写：新增异步缩略图加载（ExecutorService + Handler）；优先 ContentResolver.loadThumbnail()，失败后回退 MediaMetadataRetriever（视频帧 → 音频嵌入封面）；使用 setTag 防止 RecyclerView 复用时贴错图 |
| strings.xml | 新增播放列表面板方向相关字符串（竖屏/横屏标题、上/下/左/右选项） |
| activity_settings.xml | 设置界面新增「播放列表面板方向（竖屏）」和「播放列表面板方向（横屏）」两组 RadioGroup，各含 4 个方向选项 |
| SettingsActivity.java | 新增 PREF_PANEL_DIR_PORTRAIT / PREF_PANEL_DIR_LANDSCAPE 常量及方向値常量；在 onCreate 中绑定并初始化新 RadioButton；保存时写入两个方向偏好 |
| PlayerActivity.java | 新增 getPanelDirection() 根据当前屏幕方向读取对应面板方向偏好；重写 openPlaylistPanel() 动态设置 FrameLayout.LayoutParams（START/END/TOP/BOTTOM）并按方向播放滑入动画；同步旋转关闭按钒箭头指向关闭方向；重写 closePlaylistPanel() 按对应方向滑出 |

---

---

## v11.0 变更记录

### 1. 字幕列表点击交互升级

**功能**：在字幕列表面板中，点击某条字幕时跳转到对应时间点；**再次点击同一条**时切换播放/暂停。

**实现**：
- `PlayerActivity.java` 新增字段 `lastClickedSubEntry`，记录上一次点击的字幕条目引用
- 字幕列表适配器的点击回调中：若 `entry == lastClickedSubEntry` 则调用 `playerManager.togglePlayPause()`，否则跳转时间并记录
- 关闭字幕面板时重置 `lastClickedSubEntry = null`，确保重新打开面板后首次点击始终跳转

---

### 2. 字幕按钮默认显示状态可设置

**功能**：在「设置」中新增开关"字幕默认显示"，控制进入播放器时字幕文字是否默认可见。

| 位置 | 变更 |
|------|------|
| `SettingsActivity.java` | 新增常量 `PREF_SUBTITLE_DEFAULT_VISIBLE = "subtitle_default_visible"`；`onCreate` 读取并绑定 Switch；保存时写入 |
| `activity_settings.xml` | 在字幕面板透明度下方新增 Switch 行（`swSubtitleDefaultVisible`） |
| `PlayerActivity.java` | `applyPlayerSettings()` 读取 `PREF_SUBTITLE_DEFAULT_VISIBLE` 初始化 `isSubtitleTextVisible` 并同步更新 `subtitleView` 可见性及 `btnSubtitleToggle` 图标 |
| `strings.xml` | 新增 `settings_subtitle_default_visible`（"字幕默认显示"） |

---

### 3. 「我的列表」列表模式封面展示

**功能**：「我的列表」从方格切换为列表布局时，每个播放列表条目也展示其封面图（自定义封面或默认播放列表图标）。

| 位置 | 变更 |
|------|------|
| `item_playlist.xml` | 原固定播放列表图标替换为带 `id="ivPlaylistThumb"` 的 56×56dp ImageView，支持 `centerCrop` 缩略图 |
| `PlaylistAdapter.java` | `onBindViewHolder` 中去掉 `h.isGrid` 判断，无论方格还是列表模式均调用 `loadThumbnail()` 异步加载封面；未设置自定义封面时显示默认着色图标 |

---

### 4. 切换媒体手势全屏修复（横屏 & 竖屏）

**根因**：VERTICAL 模式下，`detectSwipe()` 有 `!isLeft` 条件限制——只有从屏幕**右半边**上下滑动才能触发切换，左半边完全无效（仅调节亮度）。用户在任意位置上下滑动均无法切换媒体。

**修复**：

| 位置 | 变更 |
|------|------|
| `GestureHandler.java` | **`detectSwipe()`**：HORIZONTAL 模式保持原逻辑（左侧竖划 = 亮度，横划 = 切换）；VERTICAL 模式**移除** `!isLeft` 限制，改为全屏任意位置上下划均可触发切换，只需超过 50dp 阈值且垂直分量大于水平分量 |
| `GestureHandler.java` | **`handleBrightnessScroll()`**：新增 `if (swipeConsumed) return` 守卫，切换手势确认后停止继续调节亮度，避免同一手势同时触发两种操作 |

**手势规则（修复后）**：

| 模式 | 方向 | 任意位置 | 操作 |
|------|------|---------|------|
| 竖屏 VERTICAL（默认） | 下划 | ✅ | 下一个 |
| 竖屏 VERTICAL（默认） | 上划 | ✅ | 上一个 |
| 竖屏 HORIZONTAL | 左划 | ✅ | 下一个 |
| 竖屏 HORIZONTAL | 右划 | ✅ | 上一个 |
| 横屏 HORIZONTAL（默认） | 左划 | ✅ | 下一个 |
| 横屏 HORIZONTAL（默认） | 右划 | ✅ | 上一个 |
| 横屏 VERTICAL | 下划 | ✅ | 下一个 |
| 横屏 VERTICAL | 上划 | ✅ | 上一个 |
| 任意模式 | 左侧短距竖划（HORIZONTAL模式） | 左半屏 | 调节亮度 |

---

## v12.0 变更记录

### 设置页面：自动保存 + 恢复进入前状态按钮

**变更概述**：

| 旧行为 | 新行为 |
|--------|--------|
| 所有修改需点击「保存并返回」才生效 | 每次调整控件后立即写入 SharedPreferences（自动保存） |
| 底部有「保存并返回」按钮 | 底部改为「恢复进入前的设置」按钮，点击后弹出确认对话框，确认后撤销本次打开设置期间的所有修改并刷新界面 |
| 返回键不保存 | 返回键 / 工具栏返回均触发 `setResult(RESULT_OK)` 直接退出（设置已实时保存） |

**技术实现**：

| 位置 | 变更 |
|------|------|
| `SettingsActivity.java` | `prefs` 升为实例字段；`onCreate` 入口处 `originalPrefs = new HashMap<>(prefs.getAll())` 快照当前设置 |
| `SettingsActivity.java` | 所有 SeekBar 的 `onStopTrackingTouch` 立即写对应 Pref；所有 RadioGroup / RadioButton 的 `onCheckedChanged` 立即写对应 Pref；控件 Switch 的 `onCheckedChanged` 立即写对应 Pref；字幕默认显示 Switch 同上 |
| `SettingsActivity.java` | 标签顺序 / 顶栏按钮排序 / 中央按钮排序的 ↑↓ 按钮点击后分别调用 `saveTabOrder()` / `saveBtnOrders()` 写入 Pref |
| `SettingsActivity.java` | 颜色选择弹窗选定颜色时立即 `putString(ctrlPrefsColor[...], color)` 写入 Pref |
| `SettingsActivity.java` | 新增 `confirmRestore()` 弹出确认对话框；`restoreSnapshot()` 用 `editor.clear()` + 遍历 `originalPrefs` 完整还原，再调用 `recreate()` 刷新界面 |
| `SettingsActivity.java` | 重写 `onBackPressed()` 加 `setResult(RESULT_OK)`；`onOptionsItemSelected` 同步更新 |
| `ctrlPrefsColor` / `ctrlPrefsVis` | 从 `onCreate` 局部变量升为实例字段，供颜色选择弹窗和恢复逻辑访问 |
| `activity_settings.xml` | 底部按钮文字改为 `@string/settings_restore`；背景改为深色描边样式（区别于原强调色实心按钮） |
| `strings.xml` | 新增 `settings_restore`（"恢复进入前的设置"）、`settings_restore_title`、`settings_restore_message`、`settings_restored` |

**用户交互流程**：

1. 打开设置 → 系统静默快照当前所有配置
2. 任意调整（拖动进度条、切换单选项、打开/关闭开关等）→ 立即生效并持久化
3. 若想撤销所有本次修改：点击「恢复进入前的设置」→ 确认 → 自动还原并刷新界面
4. 点击返回 / 工具栏返回 → 直接退出设置（保留当前已调整的状态）

---

## v13.0 变更记录

### 播放时长统计页面

**功能**：在主界面底部导航栏新增「统计」Tab，点击后打开播放时长统计页面，以**横向柱形图**按累计播放时长从高到低排列各播放列表。

#### 触发条件

| 场景 | 统计键 | 显示名称 |
|------|--------|---------|
| 从播放列表进入播放 | 播放列表 ID | 播放列表名称 |
| 直接打开单个文件 | `__single__` | 单文件播放 |

#### 统计页面功能

- **横向进度条**：每条记录的进度条宽度与最大值成比例（最大值 = 100%），颜色使用强调色（红）
- **排序**：按累计时长降序，第 1 名进度条最长
- **摘要**：页面顶部显示"共 N 个列表 · 累计 HH:MM:SS"
- **清除**：点击"清除记录"按钮，确认后清空所有统计数据并刷新页面

#### 新增文件

| 文件 | 说明 |
|------|------|
| `stats/PlayStats.java` | 单例数据层，SharedPreferences 持久化；`addTime(id, name, ms)` 累加；`getStats()` 返回按时长降序排列的列表；`clearAll()` 清除 |
| `stats/StatsActivity.java` | 统计页面 Activity；内含 `StatsAdapter`（RecyclerView 适配器），加载数据、渲染柱形图、处理清除确认弹窗 |
| `res/layout/activity_stats.xml` | 统计页布局：Toolbar + 摘要 + 清除按钮 + RecyclerView |
| `res/layout/item_stats_bar.xml` | 单行布局：名称 · 累计时长（右上）· 横向 ProgressBar |
| `res/drawable/ic_stats.xml` | 底部导航栏用横向柱图矢量图标 |

#### 修改文件

| 文件 | 变更 |
|------|------|
| `PlayerActivity.java` | 新增字段 `playStatsStartMs` / `playStatsAccumMs`；`onPlaybackStarted()` 记录开始时刻；`onPlaybackPaused()` / `onPlaybackCompleted()` 累加时长；`onDestroy()` 调用 `PlayStats.addTime()` 持久化，通过 `mediaListManager.getList(playlistId).getName()` 获取列表名 |
| `bottom_nav_menu.xml` | 新增 `nav_stats` 菜单项 |
| `MainActivity.java` | 导航监听中新增 `nav_stats` 分支：`startActivity(StatsActivity)`，返回 `false` 保持当前 Tab 高亮 |
| `AndroidManifest.xml` | 注册 `.stats.StatsActivity` |
| `strings.xml` | 新增 `tab_stats`、`stats_title`、`stats_empty`、`stats_summary`、`stats_clear`、`stats_clear_title`、`stats_clear_message`、`stats_single_file` |

---

*VideoMaster v13.0 — 纯本地，无联网，完全离线。*