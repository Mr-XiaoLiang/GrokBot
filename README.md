# GrokBot

> 用 Jetpack Compose 重绘的 GrokBot 小角色：一只能眨眼的矢量机器人，他是个组件，可以放到你的项目中。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg)
![Compose](https://img.shields.io/badge/Compose-BOM%202026.02.01-4285F4.svg)

本项目是 [grok-icon-study](https://github.com/blessonism/grok-icon-study) 的 Compose 翻译版本：把原版 Web 实现（`temp/replica/index.html` 那套引擎）逐帧移植为 Kotlin，并用 Compose `Canvas` 绘制。

---

## ✨ 特性

- **纯矢量绘制**：不依赖任何图片资源，全部由 SVG 路径 + 数学曲线实时生成，任意尺寸都清晰。
- **39 种表情（Mood）**：涵盖生命周期（`sleeping` / `waking` / `idle`）、情绪反应（`happy` / `angry` / `laughing` …）与 "Agent 工具态"（`searching` / `writing` / `uploading` / `powering-down` …）。
- **18 种体型（Shape）**：`blob`、`pebble`、`bean`、`egg`、`squircle`、`hex`、`gem`、`cloud`、`teardrop`、`leaf` 等。
- **11 种配色 × 明暗两档**：每种颜色自带上下两个渐变停靠点（`light` / `dark`），可整体切换主题。
- **交互式眼神**：眼睛实时跟随触摸位置，松手后自动回中。
- **一次性动作**：`spin()` 旋转、`bounce()` 弹跳、`burst()` 粒子迸发。
- **声明式配置热更新**：修改 [`GrokBotConfig`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceConfig.kt:34) 不会重建引擎，动画不会被打断（例如切色时不会重头播放）。
- **登录页轮播**：`ONBOARDING` 模式按 `idle` ↔ 表情 的节奏自动循环，`HOLD` 模式则由宿主锁定单个表情。
- **逐帧曲线记录**：`frameTrace` 可把每帧的弹簧、注视、眨眼、眼部形变输出到 logcat，便于在真机上定位表现异常。
- **可测试的引擎**：绘制层与引擎解耦，引擎可脱离 Android 图形栈在纯 JVM 中驱动与比对。

## 📦 模块结构

| 模块 | 说明 |
| --- | --- |
| [`grokBot`](grokBot/build.gradle.kts:1) | Android Library，角色本体（公开 API + `internal` 引擎实现） |
| [`app`](app/build.gradle.kts:1) | Demo 应用，展示全部体型、配色、表情与单次动作 |

`grokBot` 中公开 API 只暴露四个文件，其余全部位于 `internal` 包：

- [`GrokFace.kt`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:1) —— [`GrokBot()`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:252)、[`GrokBotState`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:38)、[`rememberGrokBotState()`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:231)
- [`GrokFaceConfig.kt`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceConfig.kt:1) —— [`GrokBotConfig`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceConfig.kt:34)
- [`GrokFaceModels.kt`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceModels.kt:1) —— [`GrokShape`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceModels.kt:12)、[`GrokColor`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceModels.kt:51)、[`GrokScheme`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceModels.kt:80)、[`GrokMode`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceModels.kt:86)
- [`GrokMood.kt`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokMood.kt:1) —— [`GrokMood`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokMood.kt:11) 与 [`GrokMoodGroup`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokMood.kt:80)

## ⚙️ 环境要求

| 项目 | 版本 |
| --- | --- |
| `minSdk` | 23 |
| `compileSdk` / `targetSdk` | 37 |
| Java | 11 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.02.01 |
| AGP | 9.4.1 |

版本统一由 [`libs.versions.toml`](gradle/libs.versions.toml:1) 管理。

## 🚀 快速开始

### 1. 引入依赖

作为本地模块引入（把 `grokBot` 目录复制进你的工程）：

```kotlin
// settings.gradle.kts
include(":grokBot")

// app/build.gradle.kts
dependencies {
    implementation(project(":grokBot"))
    implementation(platform(libs.androidx.compose.bom))
}
```

`grokBot` 模块已通过 `api` 暴露 Compose UI 与 `ui-graphics`，宿主的 Compose 版本需与 BOM 保持一致。

### 2. 画一只 GrokBot

```kotlin
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lollipop.grokbot.GrokBot
import com.lollipop.grokbot.GrokBotConfig
import com.lollipop.grokbot.rememberGrokBotState

@Composable
fun RobotFace() {
    var config by remember { mutableStateOf(GrokBotConfig()) }
    val state = rememberGrokBotState(config)

    GrokBot(
        modifier = Modifier.size(190.dp),
        config = config,
        state = state,
    )
}
```

[`GrokBot()`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:252) 绘制的是一个**正方形**：它会填满约束中最大的居中正方形，或用 [`GrokBotConfig.sizePx`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceConfig.kt:45) 固定边长。

### 3. 切换表情与体型

```kotlin
// 锁定单个表情
config = config.copy(mode = GrokMode.HOLD, mood = GrokMood.THINKING)

// 恢复登录页轮播
config = config.copy(mode = GrokMode.ONBOARDING)

// 换个马甲
config = config.copy(shape = GrokShape.CLOUD, color = GrokColor.VIOLET, scheme = GrokScheme.DARK)
```

[`GrokMood.entries`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokMood.kt:11) 与 [`GrokMood.groups`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokMood.kt:63) 可直接用来构建表情选择器（Demo 即按原版分组渲染）。

### 4. 触发一次性动作

```kotlin
state.spin(turns = 2f)   // 旋转（旋转进行中会被忽略）
state.bounce()           // 弹跳一次
state.burst()            // 粒子迸发（reduceMotion 开启时忽略）
state.show(GrokMood.CELEBRATE)  // 进入并保持某个表情
state.playOnboarding()   // 重新开始轮播
```

这些方法在任何时刻调用都是安全的，包括首帧渲染之前。

## 🎛️ 配置项

[`GrokBotConfig`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFaceConfig.kt:34) 是不可变的 data class，用 `copy()` 派生新配置即可：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape` | `GrokShape` | `BLOB` | 身体轮廓 |
| `color` | `GrokColor` | `BLACK` | 身体色调 |
| `scheme` | `GrokScheme` | `LIGHT` | 取用 `color` 的哪一个渐变停靠点 |
| `mood` | `GrokMood` | `IDLE` | 启动时进入的表情 |
| `mode` | `GrokMode` | `ONBOARDING` | 表情由轮播驱动，还是由宿主保持 |
| `followPointer` | `Boolean` | `true` | 眼睛是否跟随触摸位置 |
| `loginWrap` | `Boolean` | `true` | 保留登录页文案与简化眼部拓扑 |
| `emphasis` | `Boolean` | `false` | 使用强调版瞳孔与眼睑调参 |
| `paused` | `Boolean` | `false` | 冻结在当前帧（眼神仍跟随） |
| `reduceMotion` | `Boolean` | `false` | 关闭脉冲与眨眼，供宿主响应系统「减弱动效」 |
| `sizePx` | `Dp?` | `null` | 固定正方形边长；`null` 表示填满约束 |
| `flatInk` | `Color?` | `null` | 用单一纯色代替渐变 |
| `eyeColor` | `Color?` | `null` | 覆盖眼盘颜色 |
| `badgeColor` | `Color` | `#1D9BF0` | 眼球内徽标色 |

> ⚠️ `loginWrap` 与 `reduceMotion` 在引擎创建时读取一次，之后修改不再生效；其余选项均可在运行时热切换。

## 🧩 公开 API

| API | 说明 |
| --- | --- |
| [`rememberGrokBotState(config)`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:231) | 记住一个与配置绑定的状态；引擎只会创建一次 |
| [`GrokBot(modifier, config, state, onMoodChange)`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:252) | 绘制并驱动角色；`config` 变化会推送到运行中的引擎 |
| [`GrokBotState.mood`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:84) | 当前表情（可观察，可用于高亮宿主自己的选择器） |
| [`GrokBotState.currentConfig`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:88) | 引擎最近一次收到的配置 |
| [`GrokBotState.paused`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:91) / [`followPointer`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:98) | 运行时开关 |
| [`GrokBotState.frameTrace`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:117) | 每帧回调一行曲线记录；`null`（默认）时不产生开销 |

[`GrokBotState`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:38) 持有引擎与渲染器，因此必须比绘制它的组合生命周期更长——请通过 [`rememberGrokBotState()`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:231) 获取，或使用 [`GrokBot()`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:252) 的 `state` 参数。

## 🐞 调试：逐帧曲线记录

把 [`frameTrace`](grokBot/src/main/kotlin/com/lollipop/grokbot/GrokFace.kt:117) 指向一个日志接收器，即可把每帧的弹簧、注视、眨眼、眼部形变和变换输出到 logcat：

```kotlin
state.frameTrace = { line -> Log.d("GrokCurve", line) }

// adb logcat -s GrokCurve
```

Demo 应用中的「逐帧曲线记录」按钮就是这一开关的实现，见 [`MainActivity.kt`](app/src/main/java/com/lollipop/grok/face/MainActivity.kt:76)。

## 🛠️ 构建与运行

```bash
# 编译 Demo
./gradlew :app:assembleDebug

# 安装到已连接设备
./gradlew :app:installDebug

# 编译角色库
./gradlew :grokBot:assembleRelease

# 运行单元测试
./gradlew :grokBot:test
```

## 📄 版权与免责声明

1. **形象与商标归属（Intellectual Property）**
   - 本项目涉及的 **GrokBot** 形象、名称、商标及其相关知识产权均归 **xAI / Grok** 所有。
   - 本项目仅为社区爱好者通过代码绘制的动画演示（Fan Art），**并非 xAI / Grok 的官方项目**，与官方无任何关联或授权关系。

2. **代码开源许可（Software License）**
   - 本项目中由作者独立编写的实现代码遵循 [MIT 许可证](./LICENSE) 开源。
   - 你可以自由学习、修改和交流本项目的代码，但**不得将包含 GrokBot 形象的任何内容用于未经授权的商业用途**。

3. **免责条款（Disclaimer）**
   - 本项目按「原样（As-Is）」提供，作者不对代码的完整性、安全性或适用性作任何明示或暗示的保证。
   - 作者不承担因使用本代码或相关衍生作品所引起的任何直接或间接法律责任。
   - 如版权方（xAI / Grok）认为本项目存在侵权或不当使用，请联系作者，作者将及时配合修改或下架相关内容。

## 📜 License

[MIT](./LICENSE) © 2026 [Mr-XiaoLiang](https://github.com/Mr-XiaoLiang)

> 提示：MIT 许可仅覆盖本仓库中的源代码，**不覆盖** GrokBot 形象、名称、商标等归 xAI / Grok 所有的知识产权。
