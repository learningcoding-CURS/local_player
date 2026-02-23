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
| 进度条 | 可拖拽 SeekBar，实时显示当前时间 / 总时长 |
| 前进/后退 5 秒 | 点击控制区左右按钮快速跳转 ±5 秒 |
| 长按 2.5× 快进 | 长按屏幕激活 2.5× 速，松手立即恢复 |
| 双击暂停/播放 | 双击视频区域切换播放状态 |
| **左侧上划调亮度** | 在屏幕左半边上下滑动调节当前窗口亮度（上划更亮，下划更暗） |
| **竖屏：长按+上下滑切换** | 竖屏状态下，长按后向上/下滑动可切换上一首/下一首 |
| **横屏：左右滑切换** | 横屏状态下，直接向左/右滑动即可切换下一首/上一首（无需长按） |
| **锁屏** | 点击顶栏左侧锁图标锁定所有手势和按钮，防误触；再次点击解锁 |
| **多种播放模式** | 顺序播放 / 乱序播放 / 单曲循环 / 播放一次，点击顶栏模式图标循环切换 |
| **自定义播放列表** | 创建、重命名、删除列表；向列表**多选**添加音视频 |
| **播放列表多选导入** | 在列表详情页点击 + 后可同时选择多个文件一次性添加 |
| **播放界面列表面板** | 播放时点击顶栏列表图标，从右侧滑出播放列表面板，点击条目直接切换 |
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
│   ├── PlayerActivity.java            # 播放器主页面（含播放模式、列表面板）
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
│   │   ├── PlayerManager.java         # ExoPlayer 封装（含 PlayMode 存储）
│   │   ├── GestureHandler.java        # 手势（重构：竖屏长按竖滑、横屏横滑切换）
│   │   ├── PlayMode.java              # 播放模式枚举（NEW）
│   │   └── DefaultSpeedProvider.java
│   ├── playlist/
│   │   ├── MediaListManager.java      # 播放列表 JSON 持久化
│   │   ├── PlaylistActivity.java      # 播放列表管理页
│   │   ├── PlaylistDetailActivity.java# 播放列表内条目页（支持多选添加）
│   │   ├── PlaylistAdapter.java       # 列表适配器
│   │   └── MediaItemListAdapter.java  # 条目适配器，含进度条
│   ├── subtitle/
│   │   ├── SubtitleEntry.java
│   │   ├── SubtitleManager.java
│   │   ├── SrtParser.java
│   │   ├── VttParser.java
│   │   ├── AssParser.java
│   │   └── SubtitleExporter.java
│   ├── ui/
│   │   ├── SubtitleView.java
│   │   ├── VideoAdapter.java
│   │   └── PlayerPlaylistAdapter.java # 播放界面列表面板适配器（NEW）
│   └── util/
│       ├── TimeUtils.java
│       ├── FileUtils.java
│       ├── PermissionUtils.java
│       └── BuiltinMediaProvider.java
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_player.xml        # 新增：列表面板、播放模式按钮（更新）
    │   ├── activity_playlist.xml
    │   ├── activity_playlist_detail.xml
    │   ├── item_video.xml
    │   ├── item_playlist.xml
    │   ├── item_media_progress.xml
    │   └── item_player_playlist.xml   # 列表面板条目（NEW）
    ├── drawable/
    │   ├── ic_lock.xml / ic_lock_open.xml
    │   ├── ic_brightness.xml          # 已换为更简洁的太阳图标
    │   ├── ic_playlist.xml
    │   ├── ic_playlist_panel.xml      # 列表面板切换按钮图标（NEW）
    │   ├── ic_mode_sequential.xml     # 顺序播放图标（NEW）
    │   ├── ic_mode_shuffle.xml        # 乱序播放图标（NEW）
    │   ├── ic_mode_repeat_one.xml     # 单曲循环图标（NEW）
    │   ├── ic_mode_play_once.xml      # 播放一次图标（NEW）
    │   └── selector_panel_item.xml    # 面板条目选中状态（NEW）
    └── values/
        ├── colors.xml
        ├── strings.xml                # 新增播放模式、面板等字符串
        └── themes.xml
```

---

## 新功能详解（v3.0）

### 1. 手势切换视频（重构）

#### 竖屏模式
| 手势 | 动作 |
|------|------|
| 长按（不松手）+ 向上滑 | 切换到**上一个**媒体 |
| 长按（不松手）+ 向下滑 | 切换到**下一个**媒体 |
| 左侧竖向滑动（无需长按） | 调节屏幕亮度 |

#### 横屏模式
| 手势 | 动作 |
|------|------|
| 向左横滑（无需长按） | 切换到**下一个**媒体 |
| 向右横滑（无需长按） | 切换到**上一个**媒体 |
| 左侧竖向滑动 | 调节屏幕亮度 |

> **注意**：右侧滑动调音量功能已移除，请使用系统音量按键。

---

### 2. 亮度调节（改进）

- **方向修正**：上划 = 亮度升高，下划 = 亮度降低（更符合直觉）。
- **灵敏度提升**：响应更敏感，轻划即有明显变化。
- **图标更新**：亮度指示器宽度缩小为 36dp，背景更透明（66% 不透明度），图标换为更简洁的圆形太阳，减少遮挡视频。
- **触发区域**：屏幕左半部分纵向滑动触发（无需长按）。

---

### 3. 播放模式（NEW）

点击顶栏的**播放模式按钮**（`→→` 图标），循环切换以下四种模式：

| 模式 | 图标 | 行为 |
|------|------|------|
| 顺序播放 | `→→` | 按列表顺序自动播放下一首，播完最后一首停止 |
| 乱序播放 | 交叉箭头 | 随机顺序播放所有曲目，全部播完后停止 |
| 单曲循环 | 圆圈 1 | 当前文件播完后立即重播，无限循环 |
| 播放一次 | `▶|` | 当前文件播完后停止，不自动切换下一首 |

切换时屏幕下方会短暂弹出当前模式名称提示。

---

### 4. 播放列表面板（NEW）

在播放器界面中可以查看并切换同列表中的所有媒体：

1. 点击顶栏**列表面板图标**（三横线+播放三角）→ 右侧滑出播放列表面板。
2. 面板中高亮显示当前正在播放的条目（红色背景标记）。
3. 点击任意条目 → 立即切换播放，面板自动关闭。
4. 点击面板右上角关闭按钮 → 收回面板。
5. 单击视频区域时，若面板已展开则优先关闭面板。

> 仅在从播放列表详情页打开播放器时才会显示该按钮（单文件播放时按钮隐藏）。

---

### 5. 多选添加媒体（NEW）

在**播放列表详情页**点击右下角 **+** 按钮后：

- 系统文件选择器会以**多选模式**打开。
- 长按或按 Ctrl+点击（取决于设备文件管理器）可选择多个文件。
- 确认后所有选中的音视频文件一次性添加到播放列表。
- 完成后显示「已添加 N 个文件」提示。

---

### 6. 原有功能（沿用）

#### 锁屏功能
- 点击顶栏左侧**锁图标（🔒）** 锁定屏幕。
- 锁定后所有手势屏蔽，仅显示解锁按钮。
- 再次点击解锁恢复正常。

#### 播放进度保存
- 每次暂停、切换或退出时自动保存进度。
- 列表详情页每个条目下方显示红色进度条和百分比。
- 重新打开时从上次位置继续播放。

#### 长按快进（2.5×）
- 长按屏幕任意区域激活 2.5× 速度。
- 松手立即恢复正常速度。
- 在竖屏：长按后再竖向滑动可切换媒体（松手后取消快进）。

---

## 底部导航说明

| 标签 | 内容 |
|------|------|
| 媒体库 | 自动扫描 MediaStore，展示设备上所有视频和音频 |
| 我的列表 | 打开自定义播放列表管理页 |
| 内置媒体 | 展示 assets/builtin_media/ 内置的音视频文件 |

---

## 手势操作说明（完整）

| 手势 | 竖屏 | 横屏 | 备注 |
|------|------|------|------|
| 单击 | 显示/隐藏控制栏 | 显示/隐藏控制栏 | 锁屏状态下无效 |
| 双击 | 切换播放/暂停 | 切换播放/暂停 | 锁屏状态下无效 |
| 长按（不移动） | 激活 2.5× 速度 | 激活 2.5× 速度 | 松手恢复 |
| 长按 + 向上滑 | 切换到**上一个**媒体 | — | 需有播放列表上下文 |
| 长按 + 向下滑 | 切换到**下一个**媒体 | — | 需有播放列表上下文 |
| 向左横滑 | — | 切换到**下一个**媒体 | 需有播放列表上下文 |
| 向右横滑 | — | 切换到**上一个**媒体 | 需有播放列表上下文 |
| 左侧竖向滑动 | 调节屏幕亮度（上↑亮，下↓暗） | 调节屏幕亮度（上↑亮，下↓暗） | — |
| 拖拽进度条 | 精确跳转播放位置 | 精确跳转播放位置 | — |

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
| `READ_MEDIA_AUDIO` | ≥ 13 | 分区存储音频读取 |
| `WAKE_LOCK` | 全版本 | 播放时保持屏幕常亮 |

---

## 扩展开发指南

### 添加新播放模式

在 `player/PlayMode.java` 枚举中添加新值，然后在 `PlayerActivity.handlePlayModeOnCompletion()` 中添加对应的 `case` 分支。

```java
// PlayMode.java
public enum PlayMode {
    SEQUENTIAL, SHUFFLE, REPEAT_ONE, PLAY_ONCE,
    MY_CUSTOM_MODE // 新增
}

// PlayerActivity.java
case MY_CUSTOM_MODE:
    // 自定义逻辑
    break;
```

### 自定义手势行为

实现 `GestureHandler.GestureListener` 接口：

```java
new GestureHandler(context, new GestureHandler.GestureListener() {
    @Override public void onBrightnessScroll(float delta) { /* 亮度调节 */ }
    @Override public void onSwipeMedia(boolean toNext) { /* 切换媒体 */ }
    // ...
});
```

### 监听播放模式变化

覆盖 `PlayerActivity.onPlayModeChanged(PlayMode newMode)`：

```java
@Override
protected void onPlayModeChanged(PlayMode newMode) {
    super.onPlayModeChanged(newMode); // 显示 Toast 提示
    // 自定义响应（如持久化设置）
}
```

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

### 添加新字幕格式

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

**原因：** `android:attr/progressBarStyleVertical` 在 API 26 中已被移除（minSdk=29）。

**修复：** 将竖向 ProgressBar 的 style 改为标准水平样式 + `android:rotation="270"`。

---

*VideoMaster v3.0.0 — 纯本地，无联网，完全离线。*
