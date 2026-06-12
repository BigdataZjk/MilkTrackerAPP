package com.milktrackerapp.ui.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milktrackerapp.ui.theme.Rule
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Pebble
import com.milktrackerapp.ui.theme.Navy
import com.milktrackerapp.ui.theme.NavyMuted

@Composable
fun ChartView(
    dailyTotals: List<Pair<String, Int>>,
    selectedDays: Int,
    onDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Tile),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题 + 天数选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "趋势图",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(7, 14, 30).forEach { days ->
                        val selected = selectedDays == days
                        Text(
                            text = "${days}天",
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) Navy else Pebble,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (selected) Modifier.background(NavyMuted, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { onDaysChange(days) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 图表区域（含 X 轴标签）
            MilkChart(
                data = dailyTotals,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun MilkChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("暂无数据", color = Pebble, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxAmount = (data.maxOfOrNull { it.second } ?: 100).coerceAtLeast(1)
    val lineColor = Navy
    val fillColor = NavyMuted

    // X 轴标签 Paint
    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF90867E.toInt() // Pebble
            textSize = 26f // ~10sp in px at default density
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier.padding(start = 40.dp, end = 12.dp, top = 8.dp, bottom = 24.dp)) {
        val w = size.width
        val h = size.height
        val n = data.size

        if (n <= 1) {
            val pointY = h - (data[0].second.toFloat() / maxAmount * h)
            drawCircle(color = lineColor, radius = 6f, center = Offset(w / 2, pointY))
            drawContext.canvas.nativeCanvas.drawText(
                data[0].first,
                w / 2,
                h + 30f,
                labelPaint
            )
            return@Canvas
        }

        val stepX = w / (n - 1)

        // 水平网格线
        for (i in 0..3) {
            val y = h * i / 3
            drawLine(color = Rule, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
        }

        // 数据点
        val points = data.mapIndexed { index, (_, amount) ->
            Offset(index * stepX, h - (amount.toFloat() / maxAmount * h))
        }

        // 填充区域
        val fillPath = Path().apply {
            moveTo(points.first().x, h)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, h)
            close()
        }
        drawPath(path = fillPath, color = fillColor.copy(alpha = 0.3f))

        // 折线
        for (i in 1 until points.size) {
            drawLine(
                color = lineColor,
                start = points[i - 1],
                end = points[i],
                strokeWidth = 2.5f
            )
        }

        // 数据点
        points.forEach { point ->
            drawCircle(color = Tile, radius = 4f, center = point)
            drawCircle(color = lineColor, radius = 4f, center = point, style = Stroke(width = 2f))
        }

        // X 轴标签（用 nativeCanvas 绘制，保证对齐精准）
        val bottomY = h + 28f
        val showEvery = when {
            n <= 7 -> 1
            n <= 14 -> 2
            else -> (n / 4).coerceAtLeast(1)
        }

        for (i in 0 until n) {
            if (i % showEvery == 0 || i == n - 1 || i == 0) {
                // 相同标签去重（避免末位重复）
                val label = data[i].first
                val x = i * stepX
                drawContext.canvas.nativeCanvas.drawText(label, x, bottomY, labelPaint)
            }
        }
    }
}
