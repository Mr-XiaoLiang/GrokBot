package com.lollipop.grok.face

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lollipop.grok.face.ui.theme.GrokBotTheme
import com.lollipop.grokbot.GrokColor
import com.lollipop.grokbot.GrokBot
import com.lollipop.grokbot.GrokBotConfig
import com.lollipop.grokbot.GrokBotState
import com.lollipop.grokbot.GrokMode
import com.lollipop.grokbot.GrokMood
import com.lollipop.grokbot.GrokScheme
import com.lollipop.grokbot.GrokShape
import com.lollipop.grokbot.rememberGrokBotState
import com.lollipop.grokbot.toColor
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrokBotTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GrokBotDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/** Size of the main preview, the same square the web build shows. */
private val PreviewSize = 190.dp

/** Size of the secondary preview, the login screen corner of the shipped product. */
private val ShowcaseSize = 64.dp

/** Logcat tag of the curve record, so the noise can be dropped with `adb logcat -s GrokCurve`. */
private const val TraceTag = "GrokCurve"

/** Sink of `GrokBotState.frameTrace`: one line per frame, in the field names of the fixture. */
private fun logCurve(line: String) {
    Log.d(TraceTag, line)
}

/**
 * Every option of the character on one page: the live preview, the login sized showcase, the body
 * pickers, the one shot actions and the mood picker grouped like the artwork.
 *
 * Only the controls scroll. The preview row is pinned to the top, so a mood stays visible while the
 * picker below it is scrolled - which matters for the moods that take a while to play out.
 */
@Composable
private fun GrokBotDemo(modifier: Modifier = Modifier) {
    var config by remember { mutableStateOf(GrokBotConfig()) }
    val state = rememberGrokBotState(config)

    // Host side, and deliberately not part of `config`: recording is something the host does *to* a
    // running character, and routing it through the configuration would push it onto the engine.
    var tracing by remember { mutableStateOf(false) }

    // The showcase is held on one mood at a time so the beats can be driven from here, which is the
    // `pjn(n)` rotation of the web build.
    val showcaseConfig = config.copy(sizePx = ShowcaseSize, mode = GrokMode.HOLD)
    val showcase = rememberGrokBotState(showcaseConfig)
    LaunchedEffect(showcase) {
        var beat = 0
        while (true) {
            delay(GrokMood.onboardingIntervalMillis)
            beat += 1
            showcase.show(GrokMood.onboardMood(beat))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PreviewPanel(config, state, showcaseConfig, showcase)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Section("驱动方式") {
                OutlinedButton(onClick = { config = config.copy(mode = GrokMode.ONBOARDING) }) {
                    Text("登录轮换")
                }
                OutlinedButton(onClick = { config = config.copy(mode = GrokMode.HOLD) }) {
                    Text("保持")
                }
                OutlinedButton(onClick = { config = config.copy(paused = !config.paused) }) {
                    Text(if (config.paused) "继续" else "暂停")
                }
            }

            Section("单次动作") {
                OutlinedButton(onClick = state::spin) { Text("旋转") }
                OutlinedButton(onClick = state::bounce) { Text("弹跳") }
                OutlinedButton(onClick = state::burst) { Text("迸发") }
            }

            Section("调试") {
                OutlinedButton(onClick = {
                    tracing = !tracing
                    state.frameTrace = if (tracing) ::logCurve else null
                }) {
                    Text(if (tracing) "停止曲线记录" else "逐帧曲线记录")
                }
            }

            Text("体型", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GrokShape.entries.forEach { shape ->
                    OutlinedButton(onClick = { config = config.copy(shape = shape) }) {
                        Text(shape.label)
                    }
                }
            }

            Text("配色", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GrokColor.entries.forEach { color ->
                    Swatch(
                        color = color.toColor(config.scheme),
                        selected = color == config.color,
                        onClick = { config = config.copy(color = color) },
                    )
                }
                OutlinedButton(onClick = {
                    config = config.copy(
                        scheme = if (config.scheme == GrokScheme.LIGHT) GrokScheme.DARK else GrokScheme.LIGHT,
                    )
                }) {
                    Text(if (config.scheme == GrokScheme.LIGHT) "浅色" else "深色")
                }
            }

            GrokMood.groups.forEach { group ->
                Text(group.label, style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    group.moods.forEach { mood ->
                        OutlinedButton(onClick = { config = config.copy(mode = GrokMode.HOLD, mood = mood) }) {
                            Text(mood.id, color = if (mood == state.mood) MaterialTheme.colorScheme.primary else Color.Unspecified)
                        }
                    }
                }
            }
        }
    }
}

/**
 * The main preview and the login sized showcase, with the mood each of them is showing.
 *
 * It sits outside the scrolling area of [GrokBotDemo], so the character is always on screen.
 */
@Composable
private fun PreviewPanel(
    config: GrokBotConfig,
    state: GrokBotState,
    showcaseConfig: GrokBotConfig,
    showcase: GrokBotState,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GrokBot(modifier = Modifier.size(PreviewSize), config = config, state = state)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GrokBot(modifier = Modifier.size(ShowcaseSize), config = showcaseConfig, state = showcase)
            Text("登录示例 ${ShowcaseSize.value.toInt()}dp", style = MaterialTheme.typography.labelMedium)
            Text("主图：${state.mood.id}", style = MaterialTheme.typography.labelMedium)
            Text("小图：${showcase.mood.id}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** A titled row of controls that wraps the content of [content] onto one line. */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

/** A round colour button of the palette picker. */
@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

@Preview(showBackground = true)
@Composable
private fun GrokBotDemoPreview() {
    GrokBotTheme {
        GrokBotDemo(modifier = Modifier.fillMaxWidth())
    }
}
