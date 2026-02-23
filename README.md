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
| **播放控件全面可配置** | 设置中可控制每个播放按钮的显隐和颜色（白/红/橙/青/绿/黄），含播放界面内置设置入口 |
| **字幕库多选导入** | 字幕库界面支持一次选择多个字幕文件批量添加 |
| **播放界面列表面板** | 播放时点击顶栏列表图标，从右侧滑出播放列表面板，点击条目直接切换 |
| **播放进度保存** | 自动保存每个文件的播放位置，在列表中以进度条可视化显示 |
| **内置媒体（自动发现）** | 将音视频文件放入 assets/builtin_media/ 即可自动识别，无需编写 index.json |
| **默认主界面可配置** | 默认显示「内置媒体」标签；可在设置中更改默认主界面 |
| **标签顺序可调整** | 通过设置中的 ↑↓ 按钮，随意调整媒体库/我的列表/内置媒体的底部导航顺序 |
| **方块/列表双模式** | 主界面工具栏一键切换方块视图（2列网格）和传统列表，带真实封面缩略图 |
| **真实媒体封面** | 视频自动加载帧缩略图，音频加载内嵌专辑封面，缩略图异步加载不卡 UI |
| **工具栏按钮可排序** | 主界面标题栏右侧集成媒体库/统计/布局切换/设置四个图标，顺序可自由调整 |
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

## 主页布局说明

### 工具栏按钮

标题栏右侧集成了以下按钮，排列顺序可在**设置 → 工具栏按钮排序**中自由调整：

| 按钮 | 功能 |
|------|------|
| 媒体库 | 显示设备上所有视频和音频（MediaStore 扫描） |
| 统计 | 在主界面内联显示播放时长统计（含导出/导入/清除） |
| 布局切换 | 在方块视图（网格）和传统列表之间切换 |
| 设置 | 打开设置页面 |

> 活跃状态（如"媒体库"标签显示中）的按钮图标会高亮为主题色。

### 底部导航

| 标签 | 内容 |
|------|------|
| 内置媒体（默认首位） | 展示 assets/builtin_media/ 内置的音视频文件（自动发现） |
| 我的列表 | 内联展示自定义播放列表，点击直接进入列表详情 |

> 底部标签顺序和默认选中标签均可在**设置**中自由调整。

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
| 底部标签顺序 | 通过 ↑↓ 按钮调整底部导航标签的排列顺序 |
| **工具栏按钮排序** | 通过 ↑↓ 按钮调整工具栏按钮（媒体库/统计/布局切换/设置）的排列顺序 |
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
| 按钮大小 | 32–160 dp | 64 dp | 控制区中央跳转按钮的图标尺寸 |
| 跳转时长 | 1–60 秒 | 5 秒 | 单次点击跳转的秒数 |
| 按钮透明度 | 10–100% | 100% | 跳转按钮图标的不透明度 |
| 按钮上下偏移 | -150 ~ +150 dp | 0 dp | 调整跳转按钮在播放界面中的垂直位置（正值向上） |

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
| **跳转按钮大小** | 32–160 dp，默认 64 dp |
| **跳转时长** | 1–60 秒，默认 5 秒 |
| **跳转按钮透明度** | 10–100%，默认 100% |
| **跳转按钮上下偏移** | -150 ~ +150 dp，默认 0 |
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
| 播放/暂停按钮 | ✅ | 白/红/橙/青/绿/黄 |
| **设置按钮** | ✅ | 白/红/橙/青/绿/黄 |

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

---

## v14.0 变更记录

### 1. 修复底部导航「统计」按钮消失

**根因**：`rebuildBottomNav()` 在根据用户自定义标签顺序重建菜单时，清空了所有菜单项后只添加了三个可排序标签（内置媒体/媒体库/我的列表），遗漏了「统计」Tab。

**修复**：在 `rebuildBottomNav()` 末尾始终追加 `nav_stats` 菜单项（不参与用户排序，固定末尾位置）。

| 位置 | 变更 |
|------|------|
| `MainActivity.java` | `rebuildBottomNav()` 末尾追加 `menu.add(nav_stats)` |

---

### 2. 修复「我的列表」自定义封面不显示

**根因**：`PlaylistAdapter.loadThumbnail()` 在后台线程加载自定义封面图后，未校验 ViewHolder 是否已被 RecyclerView 回收复用，导致异步结果可能被丢弃或贴错。同时缺少 `setScaleType(CENTER_CROP)` 使得小图无法正确铺满。

**修复**：
- 绑定时用 `setTag(R.id.ivPlaylistThumb, listId)` 标记 ImageView
- 后台加载完成后校验 `getTag()` 是否仍匹配当前 `listId`，不匹配则跳过
- 自定义封面使用 `CENTER_CROP`，默认图标使用 `CENTER_INSIDE`
- 增加 `BitmapFactory.Options` 异常保护

| 位置 | 变更 |
|------|------|
| `PlaylistAdapter.java` | `loadThumbnail()` 增加 tag 校验、ScaleType 切换、异常保护 |

---

### 3. 彻底修复手势切换媒体无效

**根因**：`GestureDetector` 内部在用户手指按下约 500ms 后触发 `onLongPress`，将 `lpActive` 置为 `true`。旧代码在 `feedSwipeEvent()` 和 `onTouch()` 的 `ACTION_MOVE` 中用 `!lpActive` 作为前置条件，导致长按触发后所有滑动检测被完全屏蔽。用户稍慢的滑动操作（触摸到开始移动 >500ms）会被错误地识别为长按而非切换手势。

**修复**：
- 移除 `feedSwipeEvent()` 和 `onTouch()` 中 `ACTION_MOVE` 对 `!lpActive` 的守卫条件
- 在 `detectSwipe()` 中，当滑动阈值被触发且 `lpActive == true` 时：自动取消长按状态（`lpActive = false`），设置 `lpSwiped = true` 防止多次触发，然后正常执行 `onSwipeMedia()`
- 结果：滑动手势始终优先于长按速度增强，用户无论何时开始滑动都能可靠切换视频

**手势规则（修复后）**：所有三个入口（媒体库、内置媒体、我的列表）打开播放器后，横屏/竖屏的左右/上下滑动均可**每次、每个视频**可靠切换。

| 位置 | 变更 |
|------|------|
| `GestureHandler.java` | `feedSwipeEvent()` 移除 `!lpActive` 守卫 |
| `GestureHandler.java` | `onTouch()` 移除 `!lpActive` 守卫 |
| `GestureHandler.java` | `detectSwipe()` 新增长按自动取消逻辑 |

---

### 4. 播放界面新增切换视频按钮

在播放器中央控制栏两侧新增「上一个」（⏮）和「下一个」（⏭）按钮，直接点击即可切换到播放列表中的前一个/后一个视频。

- 按钮仅在存在播放列表上下文时显示（单文件播放时自动隐藏）
- 按钮大小、颜色、显隐、排列顺序均可在设置中自定义

#### 新增文件

| 文件 | 说明 |
|------|------|
| `res/drawable/ic_skip_prev.xml` | 上一个视频矢量图标（⏮） |
| `res/drawable/ic_skip_next.xml` | 下一个视频矢量图标（⏭） |

---

### 5. 切换按钮全面可配置

在「设置」页面中，切换按钮（上一个/下一个）支持以下自定义：

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 显示/隐藏 | 开/关 | 开 | 控制按钮是否可见 |
| 按钮大小 | 32–160 dp | 48 dp | 按钮图标尺寸 |
| 颜色 | 白/红/橙/青/绿/黄 | 白 | 按钮图标着色 |
| **透明度** | 10–100% | 100% | 按钮不透明度 |
| **上下偏移** | -150 ~ +150 dp | 0 dp | 在播放界面中的垂直位置偏移（正值向上） |

---

## v14.0 修改记录

| 文件 | 修改内容 |
|------|---------|
| `MainActivity.java` | `rebuildBottomNav()` 末尾追加 `nav_stats` 菜单项，修复统计 Tab 消失 |
| `PlaylistAdapter.java` | `loadThumbnail()` 增加 tag 校验防止 ViewHolder 复用错图；ScaleType 切换；异常保护 |
| `GestureHandler.java` | 移除 `feedSwipeEvent` / `onTouch` 中 `!lpActive` 守卫；`detectSwipe()` 新增长按自动取消逻辑 |
| `activity_player.xml` | 中央控制栏新增 `btnSkipPrev` / `btnSkipNext` |
| `PlayerActivity.java` | 新增 `btnSkipPrev` / `btnSkipNext` 字段、点击事件、设置读取（显隐/大小/颜色）；`applyButtonOrder()` 支持 skip-prev / skip-next |
| `SettingsActivity.java` | 新增 `PREF_BTN_SKIP_VISIBLE` / `PREF_BTN_SKIP_COLOR` / `PREF_SKIP_BTN_SIZE` 常量；控件数组扩展为 10 项；新增切换按钮大小 SeekBar；`DEFAULT_CENTER_BTN_ORDER` 更新为含 skip-prev / skip-next；`getCenterBtnLabels()` 扩展 |
| `ic_skip_prev.xml` | 新增上一个视频矢量图标 |
| `ic_skip_next.xml` | 新增下一个视频矢量图标 |
| `strings.xml` | 新增 `btn_skip_prev`、`btn_skip_next`、`settings_ctrl_skip`、`settings_skip_btn_size` |

---

## v14.1 变更记录

### 1. 底部导航标签顺序调整

将「统计」标签纳入可排序标签系统（原先固定在末尾），默认顺序调整为：**内置媒体 → 媒体库 → 统计 → 我的列表**，确保统计和媒体库位于我的列表前面。

- 「统计」现在可以和其他标签一起在设置中自由排序
- 对已有用户的兼容迁移：若保存的标签顺序不含 "stats"，自动插入到 "playlist" 前

| 位置 | 变更 |
|------|------|
| `SettingsActivity.java` | `DEFAULT_TAB_ORDER` 改为 `"builtin,library,stats,playlist"`；`getTabLabel()` 新增 stats 分支；加载已保存顺序时自动迁移注入 stats |
| `MainActivity.java` | `rebuildBottomNav()` 在循环中处理 "stats"（不再固定追加末尾）；`getTabLabel()` 新增 stats 分支 |

---

### 2. 前进/后退按钮可设置显隐

在设置的播放控件区域中，「前进/后退」（快退/快进）按钮新增显示/隐藏开关，与锁屏、播放模式等按钮一致。

| 位置 | 变更 |
|------|------|
| `SettingsActivity.java` | 新增 `PREF_BTN_SEEK_VISIBLE` 常量；`ctrlPrefsVis[7]` 从 `null` 改为 `PREF_BTN_SEEK_VISIBLE` |
| `PlayerActivity.java` | `btnRewind` / `btnForward` 改用 `applyButtonSettings()`（含显隐判断），不再仅调颜色 |

---

### 3. 切换按钮与前进/后退按钮分行显示

播放界面中央控制区改为两行布局：
- **第一行**：后退 | 播放/暂停 | 前进（原有控件）
- **第二行**：上一个视频 | 下一个视频（切换按钮，独立一行）

切换按钮不再参与中央控制栏的排序系统，仅保留自身的显隐/大小/颜色设置。

| 位置 | 变更 |
|------|------|
| `activity_player.xml` | 中央区域改为 vertical LinearLayout 包裹两行；切换按钮移至独立 `skipRow` |
| `PlayerActivity.java` | `applyButtonOrder()` 移除 skip-prev/skip-next 相关映射；新增 `findCenterBar()` 递归查找方法适配嵌套布局 |
| `SettingsActivity.java` | `DEFAULT_CENTER_BTN_ORDER` 回退为 `"rewind,play-pause,forward"`；`getCenterBtnLabels()` 回退为 3 项 |

---

## v14.1 修改记录

| 文件 | 修改内容 |
|------|---------|
| `MainActivity.java` | `rebuildBottomNav()` 循环内处理 "stats"；`getTabLabel()` 新增 stats |
| `SettingsActivity.java` | `DEFAULT_TAB_ORDER` 含 stats；标签迁移逻辑；`PREF_BTN_SEEK_VISIBLE`；`DEFAULT_CENTER_BTN_ORDER` 回退为 3 项；`getCenterBtnLabels()` 回退为 3 项 |
| `PlayerActivity.java` | 前后退按钮用 `applyButtonSettings` 支持显隐；`findCenterBar()` 递归查找；`applyButtonOrder()` 移除 skip 按钮映射 |
| `activity_player.xml` | 中央控制区改为两行垂直布局，切换按钮独立一行 |

---

*VideoMaster v14.1 — 纯本地，无联网，完全离线。*

---

## v15.0 变更记录

### 主页快捷按钮（统计 / 媒体库移至顶部）

**功能**：将「统计」和「媒体库」从底部导航栏移至主页顶部，以快捷按钮形式展示。底部导航栏仅保留「内置媒体」和「我的列表」两个标签。

#### 布局变更

| 旧版 | v15.0 |
|------|-------|
| 底部导航：内置媒体 / 媒体库 / 统计 / 我的列表（4项） | 底部导航：内置媒体 / 我的列表（2项） |
| — | 主页顶部新增快捷按钮行：统计 / 媒体库 |

#### 快捷按钮行为

| 按钮 | 点击效果 |
|------|---------|
| 统计 | 打开播放时长统计页面（StatsActivity） |
| 媒体库 | 在主页内显示媒体库内容（与原底部导航「媒体库」行为一致） |

- 「媒体库」按钮被点击时高亮显示（红色描边），表示当前正在查看媒体库
- 切换回底部导航标签时自动取消快捷按钮高亮

#### 按钮排序可配置

在「设置」中的「工具栏按钮排序」区块，通过 ↑↓ 按钮调整工具栏中四个按钮（媒体库/统计/布局切换/设置）的排列顺序。

| 设置项 | 说明 |
|--------|------|
| 工具栏按钮排序 | 通过 ↑↓ 按钮调整工具栏图标按钮的排列顺序 |

#### 默认主界面兼容

默认主界面设置仍支持「媒体库」选项。若设为「媒体库」，App 启动时直接显示媒体库内容并高亮工具栏中的媒体库图标。

#### 迁移逻辑

已保存的底部导航标签顺序（含 stats/library）会自动迁移：
- "stats" 和 "library" 提取到新的 `home_shortcut_order` 偏好中
- 底部导航标签仅保留 "builtin" 和 "playlist"
- 旧的快捷按钮顺序（仅含 stats/library）会自动追加 "toggle_view" 和 "settings"

### 新增偏好键

| 偏好键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `home_shortcut_order` | String | `"library,stats,toggle_view,settings"` | 工具栏按钮的排列顺序 |

### v15.0 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_main.xml` | 移除独立的 `topShortcutRow`；Toolbar 内新增 `toolbarActions` 水平布局；contentFrame 改回 FrameLayout |
| `activity_settings.xml` | 在标签顺序与列表显示模式之间新增「工具栏按钮排序」区块（`shortcutOrderContainer`） |
| `strings.xml` | 新增 `settings_shortcut_order`、`shortcut_toggle_view`、`shortcut_settings`；`settings_tab_order` 改为"底部标签顺序" |
| `SettingsActivity.java` | `DEFAULT_HOME_SHORTCUT_ORDER` 改为 `"library,stats,toggle_view,settings"`；`getShortcutLabel()` 增加 toggle_view/settings 标签；新增 `migrateShortcutOrder()` 自动补全缺失项 |
| `MainActivity.java` | 移除 `topShortcutRow` 和 Options Menu；新增 `toolbarActions` / `btnToggleView` 字段；新增 `setupToolbarActions()` / `createToolbarAction()` / `onToolbarActionClick()` / `highlightToolbarAction()` / `updateToggleViewIcon()` / `migrateShortcutOrder()` 方法；四个图标（媒体库/统计/布局切换/设置）动态添加到工具栏 |

---

*VideoMaster v15.0 — 纯本地，无联网，完全离线。*

---

## v15.1 变更记录

### 切换按钮新增透明度与上下偏移设置

在「设置」的切换按钮（上一个/下一个）区块中新增两项控制：

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 切换按钮大小 | 32–160 dp | 48 dp | 控制切换按钮图标尺寸 |
| 切换按钮透明度 | 10–100% | 100% | 控制切换按钮图标的不透明度，低值更透明 |
| 切换按钮上下偏移 | -150 ~ +150 dp | 0 dp | 调整切换按钮行在播放界面中的垂直位置；正值向上移，负值向下移 |

#### 技术实现

- **透明度**：通过 `View.setAlpha()` 对 `btnSkipPrev` / `btnSkipNext` 分别设置
- **上下偏移**：通过 `View.setTranslationY(-dpToPx(offset))` 对 `skipRow` 整体偏移（取反使正值=上移）

### 跳转按钮（前进/后退）新增上下偏移设置

跳转按钮新增垂直偏移控制，按钮大小范围扩大到 32–160 dp：

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 按钮大小 | 32–160 dp | 64 dp | 跳转按钮图标尺寸 |
| 按钮上下偏移 | -150 ~ +150 dp | 0 dp | 调整跳转按钮行在播放界面中的垂直位置；正值向上移 |

#### 技术实现

- **上下偏移**：`activity_player.xml` 中跳转按钮行增加 `android:id="@+id/seekRow"`，通过 `View.setTranslationY(-dpToPx(offset))` 整体偏移

### 新增偏好键

| 偏好键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `skip_btn_alpha` | int | 100 | 切换按钮透明度（10–100） |
| `skip_btn_offset_y` | int | 0 | 切换按钮垂直偏移（-150 ~ +150 dp） |
| `seek_offset_y` | int | 0 | 跳转按钮垂直偏移（-150 ~ +150 dp） |

### v15.1 修改记录

| 文件 | 修改内容 |
|------|---------|
| `SettingsActivity.java` | 新增 `PREF_SKIP_BTN_ALPHA` / `PREF_SKIP_BTN_OFFSET_Y` / `PREF_SEEK_OFFSET_Y` 常量；切换按钮和跳转按钮大小上限增至 160dp；新增跳转按钮上下偏移 SeekBar |
| `PlayerActivity.java` | `applyPlayerSettings()` 读取透明度设置并调用 `setAlpha()`；偏移方向修正为正值=上移（取反 translationY）；新增 seekRow 偏移逻辑 |
| `activity_player.xml` | 跳转按钮行增加 `android:id="@+id/seekRow"` |
| `activity_settings.xml` | 新增跳转按钮上下偏移 SeekBar；跳转按钮大小上限改为 128（32-160dp） |
| `strings.xml` | 新增 `settings_skip_btn_alpha`、`settings_skip_btn_offset`、`settings_seek_offset` |

---

*VideoMaster v15.1 — 纯本地，无联网，完全离线。*

---

## v15.2 变更记录

### 统计数据导出 / 导入

在统计页面新增「导出」和「导入」按钮，支持将播放时长统计数据以 JSON 文件保存到本地或从文件恢复。

#### 导出

1. 点击统计页面的「导出」按钮
2. 系统文件选择器弹出，选择保存位置（默认文件名 `videomaster_stats.json`）
3. 所有统计记录序列化为 JSON 数组写入文件

#### 导入

1. 点击统计页面的「导入」按钮
2. 系统文件选择器弹出，选择之前导出的 JSON 文件
3. 文件中的记录与现有数据**合并**（相同 ID 的条目时长累加，新条目直接创建）
4. 导入完成后自动刷新统计列表

#### JSON 格式

```json
[
  {
    "id": "playlist_id_or___single__",
    "name": "列表显示名称",
    "totalMs": 123456
  }
]
```

#### 使用场景

- **备份**：导出后保存到云盘或其他存储，防止数据丢失
- **迁移**：在新设备上导入旧设备导出的统计数据
- **合并**：多设备的统计数据可以导入到同一设备合并查看

### v15.2 修改记录

| 文件 | 修改内容 |
|------|---------|
| `PlayStats.java` | 新增 `exportToJson()` 将所有统计条目序列化为 JSON 字符串；新增 `importFromJson(String)` 解析 JSON 并合并到现有数据（同 ID 时长累加） |
| `StatsActivity.java` | 新增 `exportLauncher` / `importLauncher`（SAF 文件选择器）；新增 `startExport()` / `writeExportFile()` / `startImport()` / `readImportFile()` 方法；`onCreate` 绑定导出/导入按钮 |
| `activity_stats.xml` | 摘要区域改为两行布局：第一行摘要文字，第二行「导出」（青色）/「导入」（绿色）/「清除」（红色）三个按钮 |
| `strings.xml` | 新增 `stats_export`、`stats_import`、`stats_export_success`、`stats_export_failed`、`stats_import_success`、`stats_import_failed`、`stats_no_data` |

---

*VideoMaster v15.2 — 纯本地，无联网，完全离线。*

---

## v15.3 变更记录

### 播放界面新增设置按钮

在播放器顶栏新增「设置」按钮（齿轮图标），点击可直接跳转到设置页面。返回后自动重新应用所有播放器设置。

| 功能 | 说明 |
|------|------|
| 显示/隐藏 | 可在「播放控件（显隐 · 颜色）」中控制 |
| 颜色 | 支持白/红/橙/青/绿/黄 |
| 位置排序 | 在「顶栏按钮排序」中与其他按钮一起调整顺序 |

#### 迁移逻辑

- 旧版顶栏按钮顺序不含 "settings" 时，自动追加到末尾（SettingsActivity 和 PlayerActivity 均处理）

### 新增偏好键

| 偏好键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `btn_player_settings_visible` | boolean | true | 播放界面设置按钮显隐 |
| `btn_player_settings_color` | String | `"white"` | 播放界面设置按钮颜色 |

### v15.3 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_player.xml` | topBar 末尾新增 `btnPlayerSettings` ImageButton；`btnRewind`、`btnForward`、`btnSkipPrev`、`btnSkipNext` 添加 `scaleType="fitCenter"` 使图标随按钮大小缩放 |
| `PlayerActivity.java` | 新增 `btnPlayerSettings` 字段和 `settingsLauncher`；`applyPlayerSettings()` 应用显隐/颜色；`applyButtonOrder()` 加入 "settings" 映射和迁移逻辑 |
| `SettingsActivity.java` | 新增 `PREF_BTN_PLAYER_SETTINGS_VISIBLE` / `PREF_BTN_PLAYER_SETTINGS_COLOR` 常量；`DEFAULT_TOP_BTN_ORDER` 追加 "settings"；控件列表和顶栏标签数组扩展；新增 `migrateTopBtnOrder()` 自动补全 |
| `strings.xml` | 新增 `settings_ctrl_player_settings` |

---

*VideoMaster v15.3 — 纯本地，无联网，完全离线。*

---

## v15.4 变更记录

### 统计页面内联显示

点击工具栏「统计」按钮不再跳转到独立的 StatsActivity，而是在主界面中直接展示统计内容（与媒体库、内置媒体、我的列表同级的内联标签页）。

| 功能 | 说明 |
|------|------|
| 内联显示 | 统计内容在主界面中显示，不跳转新页面 |
| 导出/导入/清除 | 三个操作按钮保留，功能不变 |
| 标签高亮 | 查看统计时工具栏「统计」图标高亮为主题色 |
| 自动刷新 | 从播放界面返回时自动刷新统计数据 |

### v15.4 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_main.xml` | contentFrame 内新增 `tabStats` LinearLayout，内含摘要文字、导出/导入/清除按钮和 RecyclerView |
| `MainActivity.java` | 新增 `tabStats` / `recyclerStats` / `tvStatsEmpty` / `tvStatsSummary` 字段；新增 `statsExportLauncher` / `statsImportLauncher`；新增 `loadStats()` / `confirmClearStats()` / `startStatsExport()` / `writeStatsExport()` / `startStatsImport()` / `readStatsImport()` 方法和 `StatsInlineAdapter` 内部类；`showTab()` 增加 stats 分支；`onToolbarActionClick("stats")` 改为内联显示；`onResume()` 在 stats 标签时自动刷新 |

---

## v15.5 变更记录

### 设置界面：跳转按钮与切换按钮设置紧邻排列

将"切换按钮"的设置（大小、透明度、上下偏移）从原来动态插入在播放控件上方的位置，移至"跳转按钮"设置的正下方，使两组相关设置紧密排列、更易对比调整。

| 变更 | 说明 |
|------|------|
| 布局调整 | 切换按钮设置从 Java 动态创建改为 XML 静态布局，紧跟在跳转按钮设置下方 |
| 顺序 | 跳转按钮（大小→跳转时长→透明度→上下偏移）→ 分割线 → 切换按钮（大小→透明度→上下偏移）→ 分割线 → 进度条设置 |

### v15.5 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_settings.xml` | 在跳转按钮偏移 SeekBar 下方新增切换按钮设置区块（标题 + 大小/透明度/偏移各一对 TextView+SeekBar），使用 XML 静态定义替代动态创建 |
| `SettingsActivity.java` | 跳转按钮偏移代码之后新增切换按钮的 `findViewById` + SeekBar 初始化代码；删除原来在播放控件上方动态创建切换按钮 UI 的整个代码块（约 110 行） |

---

## v15.6 变更记录

### 主页工具栏按钮显隐可配置

设置中的「工具栏按钮排序」区域新增开关，可分别控制"统计"、"媒体库"、"布局切换"三个按钮的显隐。"设置"按钮始终显示，不可隐藏。

| 按钮 | 可隐藏 | 默认 |
|------|--------|------|
| 媒体库 | ✅ | 显示 |
| 统计 | ✅ | 显示 |
| 布局切换 | ✅ | 显示 |
| 设置 | — | 始终显示 |

### v15.6 修改记录

| 文件 | 修改内容 |
|------|---------|
| `SettingsActivity.java` | 新增 `PREF_HOME_BTN_STATS_VISIBLE` / `PREF_HOME_BTN_LIBRARY_VISIBLE` / `PREF_HOME_BTN_TOGGLE_VIEW_VISIBLE` 常量；新增 `getShortcutVisPref()` 方法；`refreshShortcutOrderRows()` 中为可隐藏按钮添加 Switch 开关 |
| `MainActivity.java` | 新增 `isToolbarActionVisible()` 方法；`setupToolbarActions()` 中根据 visibility 偏好跳过被隐藏的按钮 |

---

## v15.7 变更记录

### 设置界面：面板方向选项两列布局

播放列表面板方向（竖屏/横屏）的四个选项从原来每行一个改为两行两列（上/下 一行，左/右 一行），节省纵向空间。

### v15.7 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_settings.xml` | 竖屏/横屏面板方向的 `RadioGroup` 替换为嵌套 `LinearLayout`（外层 vertical，内层两个 horizontal 各含两个 `RadioButton`，各占 50% 宽度） |
| `SettingsActivity.java` | 原来逐个设置 `onCheckedChangeListener` 的代码合并为 `setupPanelDirGroup()` 通用方法，用 `setOnClickListener` 手动管理 RadioButton 互斥 |

---

## v15.8 变更记录

### 设置界面：双选项 RadioGroup 改为并排布局

将只有两个选项的 RadioGroup（列表显示模式、竖屏切换手势、横屏切换手势）从垂直排列改为水平并排，每组节省一行空间。

| 设置项 | 选项 A | 选项 B |
|--------|--------|--------|
| 列表显示模式 | 方块视图（网格） | 传统列表 |
| 竖屏切换媒体手势 | 上下滑动 | 左右滑动 |
| 横屏切换媒体手势 | 左右滑动 | 上下滑动 |

### v15.8 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_settings.xml` | `rgViewMode` / `rgPortraitSwipe` / `rgLandscapeSwipe` 三个 RadioGroup 的 `orientation` 从 `vertical` 改为 `horizontal`；内部 RadioButton 宽度从 `match_parent` 改为 `0dp` + `layout_weight="1"` 各占 50% |

---

## v15.9 变更记录

### 设置页背景透明度可调

设置界面底部新增"设置页背景透明度"滑块（0%–100%），默认 100%（不透明）。降低透明度后，在播放界面打开设置时可以透过设置页看到正在播放的画面。

| 项目 | 说明 |
|------|------|
| 偏好键 | `settings_bg_alpha` |
| 范围 | 0%（全透明）– 100%（不透明） |
| 默认值 | 100% |
| 实时预览 | 拖动滑块即时更新背景透明度 |
| 影响区域 | CoordinatorLayout 背景 + AppBarLayout 背景同步变化 |

### v15.9 修改记录

| 文件 | 修改内容 |
|------|---------|
| `themes.xml` | 新增 `Theme.VideoMaster.Translucent` 主题（`windowIsTranslucent=true` + `windowBackground=transparent`） |
| `AndroidManifest.xml` | SettingsActivity 应用 `Theme.VideoMaster.Translucent` 主题 |
| `strings.xml` | 新增 `settings_bg_alpha` 字符串 |
| `activity_settings.xml` | 恢复按钮上方新增透明度标签 `tvSettingsBgAlphaLabel` 和 SeekBar `sbSettingsBgAlpha` |
| `SettingsActivity.java` | 新增 `PREF_SETTINGS_BG_ALPHA` / `DEFAULT_SETTINGS_BG_ALPHA` 常量；新增 `applySettingsBgAlpha()` 方法，同时调整 CoordinatorLayout 和 AppBarLayout 的背景色 alpha；`onCreate` 中读取偏好并初始化 SeekBar |

---

## v16.0 变更记录

### 设置界面：默认主界面选项并排

"默认主界面"的三个 RadioButton（内置媒体、媒体库、我的列表）从纵向排列改为横向并排一行，节省空间。

### v16.0 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_settings.xml` | `rgDefaultTab` 的 `orientation` 从 `vertical` 改为 `horizontal` |
| `SettingsActivity.java` | 动态添加 RadioButton 时使用 `RadioGroup.LayoutParams(0, WRAP_CONTENT, 1f)` 使各项等宽并排 |

---

## v16.1 变更记录

### 设置界面：播放控件两列排列

播放控件（显隐、颜色）区域由每行一个改为每行两个并排排列，节省约一半纵向空间。同时缩短两个较长的标签以适配半宽布局：

| 原标签 | 新标签 |
|--------|--------|
| 跳转按钮（前进/后退） | 跳转按钮 |
| 切换按钮（上一个/下一个） | 切换按钮 |

### v16.1 修改记录

| 文件 | 修改内容 |
|------|---------|
| `strings.xml` | `settings_ctrl_seek` 缩短为"跳转按钮"；`settings_ctrl_skip` 缩短为"切换按钮" |
| `SettingsActivity.java` | 播放控件循环改为两两配对：每两个控件放入同一个 horizontal `LinearLayout`（各占 50% 宽度）；色块尺寸从 28dp 调为 24dp；标签字号从 14sp 调为 13sp 并加 `singleLine` + `ellipsize` 防溢出 |

---

## v16.2 变更记录

### 手势切换视频彻底重构（所有来源、每次可靠）

**根因分析**：

旧版 GestureHandler 使用**双路径**滑动检测（Activity 级 `feedSwipeEvent` + View 级 `onTouch`），两条路径共享同一个 `swipeConsumed` 标志。这导致以下问题：

| 问题 | 说明 |
|------|------|
| 标志冲突 | `feedSwipeEvent` 和 `onTouch` 在不同时机重置同一个 `swipeConsumed` 标志，可能互相覆盖导致检测失效 |
| 异常静默吞没 | 两条路径均使用 `catch (Exception ignored) {}`，如果回调（`playNext`/`switchToPlaylistItem`）抛出异常（如 URI 权限错误），错误被完全隐藏，用户看不到任何反馈 |
| 控件拦截 | 当控件叠加层的按钮拦截触摸事件时，`onTouch` 路径完全不会触发，仅靠 `feedSwipeEvent` 路径工作，但两条路径共享状态导致行为不可预测 |

**修复方案**：

| 变更 | 说明 |
|------|------|
| `feedSwipeEvent` 成为唯一滑动检测路径 | 移除 `onTouch` 中的所有滑动检测代码，`onTouch` 仅负责 GestureDetector（点击/双击/长按/亮度） |
| 独立状态标志 | 新增 `actSwipeFired` 标志，仅由 `feedSwipeEvent` 使用，不与其他路径共享 |
| 移除 `catch (Exception ignored)` | `feedSwipeEvent` 不再使用 try-catch-ignore，异常可以正常传播和记录 |
| 结构化日志 | 滑动检测成功时输出 `Log.d` 日志（方向、偏移量），方便排查问题 |
| SeekBar 拖拽保护 | `dispatchTouchEvent` 新增 `isDraggingSeekBar` 条件，防止拖拽进度条时误触发切换 |
| 回调错误处理 | `onSwipeMedia` 回调和 `switchToPlaylistItem` 均添加 try-catch，捕获异常后以 Toast 显示错误信息而非静默失败 |
| `playNext`/`playPrevious` 日志 | 当播放列表为空/null 时输出警告日志，帮助定位数据传递问题 |

**三个入口的播放列表传递验证**：

| 来源 | 方法 | 传递方式 | 状态 |
|------|------|---------|------|
| 媒体库 | `openPlayerWithList(item, videoList)` | 整个 videoList 转为 URI 列表传入 | ✅ |
| 内置媒体 | `openPlayerWithList(item, builtinList)` | 整个 builtinList 转为 URI 列表传入 | ✅ |
| 我的列表 | 内联 lambda 构建 uriStrs | currentListItems 快照 + playlistId 传入 | ✅ |

### 播放界面跳转设置不暂停 + 实时设置预览

**功能**：从播放界面点击设置按钮跳转时，视频继续播放不暂停。在透明设置页面调整按钮颜色/大小等参数时，播放界面实时反映修改效果。

| 行为 | 旧版 | v16.2 |
|------|------|-------|
| 点击播放界面设置按钮 | 视频暂停 | 视频继续播放 |
| 在设置页改变按钮颜色 | 返回后才能看到效果 | 通过透明设置页实时看到效果 |
| 在设置页改变按钮大小 | 返回后才能看到效果 | 实时更新 |
| 跳转到设置时控件状态 | 控件可能自动隐藏 | 控件保持显示，方便透过设置页预览 |

**技术实现**：

| 机制 | 说明 |
|------|------|
| `goingToSettings` 标志 | 点击设置按钮时置 true，`onPause()` 据此跳过 `playerManager.pause()` |
| `OnSharedPreferenceChangeListener` | 在 `onCreate` 注册、`onDestroy` 注销；当 `goingToSettings=true` 时每次设置变更自动调用 `applyPlayerSettings()` 刷新 UI |
| 控件保持可见 | 跳转前取消自动隐藏定时器，确保控件叠加层保持 `VISIBLE` 且 `alpha=1` |
| `onResume()` 兜底 | 返回时重置标志并再次调用 `applyPlayerSettings()`，确保所有设置生效 |

### v16.2 修改记录

| 文件 | 修改内容 |
|------|---------|
| `GestureHandler.java` | 彻底重构：`feedSwipeEvent` 作为唯一滑动检测路径；新增独立 `actSwipeFired` 标志；移除 `onTouch` 中的滑动检测和 `swipeConsumed` 共享标志；`handleBrightnessScroll` 改用 `actSwipeFired` 判断；移除 `feedSwipeEvent` 的 catch-all try-catch；新增 `checkAndFireSwipe` 私有方法集中滑动逻辑；添加 Log.d 日志 |
| `PlayerActivity.java` | 新增 `goingToSettings` 标志控制 `onPause` 跳过暂停；新增 `settingsChangeListener` 监听 SharedPreferences 变化实时调用 `applyPlayerSettings()`；设置按钮点击时保持控件可见并取消自动隐藏；`onResume` 重置标志并重新应用设置；`onCreate` 注册 / `onDestroy` 注销偏好监听器；`dispatchTouchEvent` 新增 `isDraggingSeekBar` 条件；`onSwipeMedia`/`switchToPlaylistItem` 添加错误处理 |

---

## v16.3 变更记录

### 设置界面：按钮调试区可折叠标签切换

**功能**：跳转按钮、切换按钮、暂停按钮、进度条四组设置改为并排标签切换，点击展开对应设置面板，再次点击收起。支持同时展开多个面板。

| 标签 | 内容 |
|------|------|
| 跳转 | 按钮大小、跳转时长、透明度、上下偏移 |
| 切换 | 按钮大小、透明度、上下偏移 |
| 暂停 | **新增** — 按钮大小、透明度、上下偏移 |
| 进度条 | 进度条粗细、已播放透明度 |

选中的标签高亮为主题色，未选中恢复默认背景。

### 暂停按钮可配置（新增）

| 设置项 | 范围 | 默认值 | 说明 |
|--------|------|--------|------|
| 按钮大小 | 32–160 dp | 72 dp | 播放/暂停按钮图标尺寸 |
| 按钮透明度 | 10–100% | 100% | 不透明度 |
| 按钮上下偏移 | -150 ~ +150 dp | 0 dp | 仅偏移播放/暂停按钮自身（正值向上） |

### 新增偏好键

| 偏好键 | 类型 | 默认值 |
|--------|------|--------|
| `playpause_btn_size` | int | 72 |
| `playpause_btn_alpha` | int | 100 |
| `playpause_btn_offset_y` | int | 0 |

### v16.3 修改记录

| 文件 | 修改内容 |
|------|---------|
| `activity_settings.xml` | 跳转/切换/进度条三个独立区块替换为4个并排 `TextView` 标签（跳转/切换/暂停/进度条）+ 4个可折叠 `LinearLayout` 容器（默认 GONE）；新增暂停按钮的大小/透明度/偏移 SeekBar |
| `SettingsActivity.java` | 新增 `PREF_PLAYPAUSE_BTN_SIZE` / `PREF_PLAYPAUSE_BTN_ALPHA` / `PREF_PLAYPAUSE_BTN_OFFSET_Y` 常量及默认值；新增标签切换逻辑（点击切换 VISIBLE/GONE + 背景色高亮）；新增暂停按钮的 SeekBar 绑定和保存逻辑 |
| `PlayerActivity.java` | `applyPlayerSettings()` 新增暂停按钮大小（LayoutParams）、透明度（setAlpha）、偏移（setTranslationY）的读取和应用 |
| `strings.xml` | 新增 `settings_playpause_btn_size`、`settings_playpause_btn_alpha`、`settings_playpause_btn_offset` |

---

*VideoMaster v16.3 — 纯本地，无联网，完全离线。*