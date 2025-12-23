# 📖 文档索引

## 欢迎使用本地播放器项目！

这是一个完整的 Android 本地视频/音频播放器应用，基于 Kotlin + Jetpack Compose + ExoPlayer 开发。

---

## 🚀 快速导航

### 新手入门

1. **[快速开始指南 (QUICKSTART.md)](QUICKSTART.md)** ⭐ 必读
   - 环境要求和配置
   - 导入和运行项目
   - 常见问题解决
   - 开发技巧

2. **[项目完成总结 (PROJECT_SUMMARY.md)](PROJECT_SUMMARY.md)** ⭐ 推荐
   - 已完成功能清单
   - 项目统计数据
   - 核心代码亮点
   - 开发检查清单

### 核心文档

3. **[主文档 (README.md)](README.md)** ⭐ 最重要
   - 项目概述
   - 技术架构说明
   - 数据库 Schema 设计
   - ExoPlayerManager 实现
   - PlayerScreen 手势处理
   - 字幕和文稿解析
   - Gradle 依赖清单
   - APK 打包指南
   - 完整测试用例清单

4. **[架构设计文档 (ARCHITECTURE.md)](ARCHITECTURE.md)** ⭐ 深入理解
   - 整体架构设计
   - 核心组件详解
   - 数据流设计
   - 状态管理策略
   - 线程模型
   - 性能优化
   - 扩展性设计

### 辅助文档

5. **[测试指南 (TESTING.md)](TESTING.md)**
   - 18 个详细测试用例
   - 手动测试步骤
   - 性能测试指南
   - 兼容性测试清单
   - 测试报告模板

6. **[项目结构 (PROJECT_STRUCTURE.md)](PROJECT_STRUCTURE.md)**
   - 完整文件列表
   - 核心文件说明
   - 代码统计
   - 快速文件导航

---

## 📚 按使用场景查找

### 场景 1: 我是新手，第一次接触这个项目

**阅读顺序**:
1. [快速开始 (QUICKSTART.md)](QUICKSTART.md) - 配置环境并运行
2. [项目总结 (PROJECT_SUMMARY.md)](PROJECT_SUMMARY.md) - 了解项目全貌
3. [主文档 (README.md)](README.md) - 学习功能和使用方法

**时间**: 约 30-60 分钟

---

### 场景 2: 我想了解技术实现细节

**阅读顺序**:
1. [主文档 (README.md)](README.md) - 核心功能说明
2. [架构设计 (ARCHITECTURE.md)](ARCHITECTURE.md) - 深入架构设计
3. [项目结构 (PROJECT_STRUCTURE.md)](PROJECT_STRUCTURE.md) - 文件组织

**然后查看源代码**:
- `ExoPlayerManager.kt` - 播放器实现
- `PlayerScreen.kt` - UI 和手势
- `SubtitleParser.kt` - 字幕解析

**时间**: 约 1-2 小时

---

### 场景 3: 我要开始开发/修改代码

**阅读顺序**:
1. [快速开始 (QUICKSTART.md)](QUICKSTART.md) - 配置开发环境
2. [架构设计 (ARCHITECTURE.md)](ARCHITECTURE.md) - 理解架构
3. [项目结构 (PROJECT_STRUCTURE.md)](PROJECT_STRUCTURE.md) - 找到要修改的文件

**开发前检查**:
- [ ] 环境已配置
- [ ] 项目能运行
- [ ] 理解架构设计
- [ ] 找到相关文件

**时间**: 约 1 小时准备 + 开发时间

---

### 场景 4: 我要测试应用

**阅读顺序**:
1. [测试指南 (TESTING.md)](TESTING.md) - 完整测试清单
2. [快速开始 (QUICKSTART.md)](QUICKSTART.md) - 构建 APK

**测试步骤**:
1. 准备测试设备和媒体文件
2. 按照测试清单逐项测试
3. 记录问题和结果

**时间**: 约 2-4 小时（完整测试）

---

### 场景 5: 我想了解项目结构和文件组织

**直接查看**:
- [项目结构 (PROJECT_STRUCTURE.md)](PROJECT_STRUCTURE.md)

**包含内容**:
- 完整文件列表
- 文件用途说明
- 代码统计
- 快速导航

**时间**: 约 15 分钟

---

## 🔍 按主题查找

### 播放器相关
- **播放器实现**: [README.md § 3. ExoPlayerManager](README.md#3-exoplayermanager-核心功能)
- **倍速控制**: [README.md § 3. 倍速控制](README.md#倍速控制)
- **长按加速**: [README.md § 3. 长按临时加速](README.md#长按临时加速实现)

### 手势控制
- **手势处理**: [README.md § 4. PlayerScreen 手势处理](README.md#4-playerscreen---手势处理)
- **亮度调节**: [README.md § 4. 亮度控制](README.md#亮度控制无需系统权限)
- **音量调节**: [README.md § 4. 音量控制](README.md#音量控制)

### 字幕和文稿
- **字幕解析**: [README.md § 5. 字幕解析器](README.md#5-字幕与文稿解析)
- **文稿解析**: [README.md § 5. 文稿解析器](README.md#文稿解析器-transcriptparser)

### 数据库
- **数据库设计**: [README.md § 2. 数据库 Schema](README.md#2-数据库-schema)
- **Repository**: [ARCHITECTURE.md § MediaRepository](ARCHITECTURE.md#4-mediarepository)

### UI 设计
- **UI 层实现**: [README.md § 4. PlayerScreen](README.md#4-playerscreen---手势处理)
- **主题配置**: [PROJECT_STRUCTURE.md § UI 层](PROJECT_STRUCTURE.md#ui-层-7-个)

### 后台播放
- **服务实现**: [README.md § 6. 后台播放](README.md#6-后台播放与-mediasession)
- **MediaSession**: [README.md § 6. MediaSession](README.md#playbackservice-实现)

### 屏幕方向
- **横竖屏切换**: [README.md § 7. 横竖屏](README.md#7-横竖屏自动切换)
- **方向管理器**: [README.md § 7. OrientationManager](README.md#orientationmanager-实现)

### 构建和打包
- **Gradle 配置**: [README.md § 8. Gradle 依赖](README.md#8-gradle-依赖清单)
- **APK 打包**: [README.md § 9. APK 打包](README.md#9-apk-打包与-abi-拆分)
- **ProGuard**: [QUICKSTART.md § Release APK](QUICKSTART.md#构建-release-apk)

### 测试
- **测试清单**: [README.md § 10. 测试用例](README.md#10-测试用例清单)
- **详细测试**: [TESTING.md](TESTING.md)

---

## 🎯 按角色查找

### 👨‍💻 开发者
**必读**:
1. [快速开始 (QUICKSTART.md)](QUICKSTART.md)
2. [架构设计 (ARCHITECTURE.md)](ARCHITECTURE.md)
3. [项目结构 (PROJECT_STRUCTURE.md)](PROJECT_STRUCTURE.md)

**推荐**:
- [主文档 (README.md)](README.md) - 了解所有功能
- [测试指南 (TESTING.md)](TESTING.md) - 测试你的改动

### 🎨 UI/UX 设计师
**必读**:
1. [主文档 (README.md)](README.md) - 了解功能
2. [测试指南 (TESTING.md)](TESTING.md) - 体验完整流程

**推荐**:
- PlayerScreen.kt 源代码 - 查看 UI 实现

### 🧪 测试工程师
**必读**:
1. [测试指南 (TESTING.md)](TESTING.md) - 完整测试清单
2. [快速开始 (QUICKSTART.md)](QUICKSTART.md) - 运行应用

**推荐**:
- [主文档 (README.md)](README.md) - 了解所有功能

### 📊 项目经理
**必读**:
1. [项目总结 (PROJECT_SUMMARY.md)](PROJECT_SUMMARY.md) - 项目概览
2. [主文档 (README.md)](README.md) - 功能清单

**推荐**:
- [架构设计 (ARCHITECTURE.md)](ARCHITECTURE.md) - 技术方案

### 🎓 学习者
**必读**:
1. [快速开始 (QUICKSTART.md)](QUICKSTART.md) - 上手指南
2. [主文档 (README.md)](README.md) - 功能学习
3. [架构设计 (ARCHITECTURE.md)](ARCHITECTURE.md) - 深入学习

**推荐**:
- 按照学习路径逐步深入（见 [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-学习路径)）

---

## 📋 文档清单

| 文档 | 页数估算 | 难度 | 重要性 |
|------|---------|------|--------|
| [README.md](README.md) | 25 页 | ⭐⭐⭐ 中 | ⭐⭐⭐⭐⭐ 最高 |
| [QUICKSTART.md](QUICKSTART.md) | 12 页 | ⭐ 易 | ⭐⭐⭐⭐⭐ 最高 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 20 页 | ⭐⭐⭐⭐ 难 | ⭐⭐⭐⭐ 高 |
| [TESTING.md](TESTING.md) | 15 页 | ⭐⭐ 中易 | ⭐⭐⭐⭐ 高 |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | 8 页 | ⭐ 易 | ⭐⭐⭐ 中 |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | 10 页 | ⭐ 易 | ⭐⭐⭐⭐ 高 |
| [INDEX.md](INDEX.md) | 5 页 | ⭐ 易 | ⭐⭐⭐ 中 |

**总计**: ~95 页文档

---

## 🔗 外部资源

### 官方文档
- [Android 开发者文档](https://developer.android.com/)
- [ExoPlayer 文档](https://developer.android.com/guide/topics/media/exoplayer)
- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)
- [Room 持久化库](https://developer.android.com/training/data-storage/room)
- [Kotlin 协程](https://kotlinlang.org/docs/coroutines-overview.html)

### 学习资源
- [Compose 教程](https://developer.android.com/jetpack/compose/tutorial)
- [ExoPlayer 入门](https://exoplayer.dev/)
- [Kotlin 官方教程](https://kotlinlang.org/docs/home.html)

---

## 📞 获取帮助

### 遇到问题？

1. **查看文档**
   - 先查看 [QUICKSTART.md 常见问题](QUICKSTART.md#常见问题解决)
   
2. **检查日志**
   - 查看 Logcat 输出
   - 搜索错误信息

3. **参考示例**
   - 查看源代码注释
   - 参考测试用例

4. **搜索资料**
   - Google 搜索错误信息
   - 查看 Stack Overflow

---

## ✨ 推荐阅读顺序

### 快速上手（30 分钟）
```
QUICKSTART.md → PROJECT_SUMMARY.md → 运行应用
```

### 完整学习（3 小时）
```
QUICKSTART.md 
    ↓
PROJECT_SUMMARY.md 
    ↓
README.md 
    ↓
ARCHITECTURE.md 
    ↓
源代码阅读
```

### 深入研究（1 周）
```
所有文档阅读
    ↓
源代码详细分析
    ↓
功能测试
    ↓
代码修改实践
    ↓
新功能开发
```

---

## 🎉 开始您的旅程

选择适合您的起点：

- 🚀 **我想快速运行看看** → [QUICKSTART.md](QUICKSTART.md)
- 📖 **我想全面了解项目** → [README.md](README.md)
- 🏗️ **我想理解架构设计** → [ARCHITECTURE.md](ARCHITECTURE.md)
- 🧪 **我想测试应用** → [TESTING.md](TESTING.md)
- 📊 **我想看项目总结** → [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

---

**祝您使用愉快！** 🎊✨

_最后更新: 2024-12-23_

