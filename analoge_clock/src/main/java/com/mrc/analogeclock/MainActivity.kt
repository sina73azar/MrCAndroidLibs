package com.mrc.analogeclock

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnalogeClockApp()
        }
    }
}

@Composable
private fun AnalogeClockApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF101418),
            surface = Color(0xFF101418),
            primary = Color(0xFF62D6A3),
            onBackground = Color(0xFFE8EEF2),
            onSurface = Color(0xFFE8EEF2),
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClockScreen()
        }
    }
}

@Composable
private fun ClockScreen() {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = System.currentTimeMillis()
            nowMillis = current
            delay(1000L - current % 1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF182027),
                        Color(0xFF101418),
                        Color(0xFF0C0F12),
                    )
                )
            )
            .padding(horizontal = 28.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = formatTime(nowMillis),
                color = Color(0xFFE8EEF2),
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatDate(nowMillis),
                color = Color(0xFF99A7B2),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(28.dp))
            AnalogClock(
                nowMillis = nowMillis,
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxWidth = 430.dp)
                    .aspectRatio(1f),
            )
        }
    }
}

@Composable
private fun AnalogClock(
    nowMillis: Long,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val diameter = min(size.width, size.height)
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val faceRadius = radius * 0.88f
        val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF25313A), Color(0xFF141A20)),
                center = center,
                radius = faceRadius,
            ),
            radius = faceRadius,
            center = center,
        )
        drawCircle(
            color = Color(0xFF3A4650),
            radius = faceRadius,
            center = center,
            style = Stroke(width = 3.dp.toPx()),
        )

        repeat(60) { tick ->
            val major = tick % 5 == 0
            val angle = tick * 6f
            val outer = pointOnCircle(center, faceRadius * 0.94f, angle)
            val inner = pointOnCircle(center, faceRadius * if (major) 0.82f else 0.89f, angle)
            drawLine(
                color = if (major) Color(0xFFE8EEF2) else Color(0xFF65717A),
                start = inner,
                end = outer,
                strokeWidth = if (major) 3.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(232, 238, 242)
            textAlign = Paint.Align.CENTER
            textSize = diameter * 0.065f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }
        repeat(12) { index ->
            val number = index + 1
            val position = pointOnCircle(center, faceRadius * 0.68f, number * 30f)
            drawContext.canvas.nativeCanvas.drawText(
                number.toString(),
                position.x,
                position.y + numberPaint.textSize * 0.35f,
                numberPaint,
            )
        }

        val hourAngle = (hour + minute / 60f) * 30f
        val minuteAngle = (minute + second / 60f) * 6f
        val secondAngle = second * 6f

        drawHand(center, faceRadius * 0.48f, hourAngle, Color(0xFFE8EEF2), 8.dp.toPx())
        drawHand(center, faceRadius * 0.68f, minuteAngle, Color(0xFFE8EEF2), 5.dp.toPx())
        drawHand(center, faceRadius * 0.76f, secondAngle, Color(0xFF62D6A3), 2.dp.toPx())

        drawCircle(color = Color(0xFF62D6A3), radius = 7.dp.toPx(), center = center)
        drawCircle(color = Color(0xFF101418), radius = 3.dp.toPx(), center = center)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHand(
    center: Offset,
    length: Float,
    angleDegrees: Float,
    color: Color,
    strokeWidth: Float,
) {
    drawLine(
        color = color,
        start = center,
        end = pointOnCircle(center, length, angleDegrees),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

private fun pointOnCircle(
    center: Offset,
    radius: Float,
    angleDegrees: Float,
): Offset {
    val angle = (angleDegrees - 90f) * PI.toFloat() / 180f
    return Offset(
        x = center.x + cos(angle) * radius,
        y = center.y + sin(angle) * radius,
    )
}

private fun formatTime(nowMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(nowMillis))

private fun formatDate(nowMillis: Long): String =
    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(nowMillis))

@Preview
@Composable
private fun AnalogeClockPreview() {
    AnalogeClockApp()
}
