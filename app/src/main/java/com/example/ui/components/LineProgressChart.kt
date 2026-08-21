package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.ChartPoint
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary

@Composable
fun LineProgressChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = IndigoPrimary,
    fillStartColor: Color = IndigoLight.copy(alpha = 0.35f),
    fillEndColor: Color = IndigoLight.copy(alpha = 0.0f)
) {
    if (points.isEmpty()) return

    var selectedIndex by remember(points) { mutableStateOf(points.size - 1) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val textLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-Axis Labels
            Column(
                modifier = Modifier
                    .height(160.dp)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = textLabelColor))
                Spacer(modifier = Modifier.weight(1f))
                Text("75%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = textLabelColor))
                Spacer(modifier = Modifier.weight(1f))
                Text("50%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = textLabelColor))
                Spacer(modifier = Modifier.weight(1f))
                Text("25%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = textLabelColor))
                Spacer(modifier = Modifier.weight(1f))
                Text("0%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = textLabelColor))
            }

            // Chart Canvas
            Box(modifier = Modifier
                .weight(1f)
                .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (points.size.coerceAtLeast(2) - 1).toFloat()
                                val clickedIdx = ((offset.x + spacing / 2) / spacing).toInt().coerceIn(0, points.size - 1)
                                selectedIndex = clickedIdx
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 12.dp.toPx()
                    val paddingTop = 24.dp.toPx()
                    val chartHeight = height - paddingTop - paddingBottom
                    val stepX = if (points.size > 1) width / (points.size - 1) else width

                    // Draw 5 dashed horizontal grid lines
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0..4) {
                        val y = paddingTop + (chartHeight * (i / 4f))
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }

                    // Compute Point Coordinates
                    val coords = points.mapIndexed { idx, point ->
                        val x = idx * stepX
                        val normalizedVal = (point.value / 100f).coerceIn(0f, 1f)
                        val y = paddingTop + chartHeight * (1f - normalizedVal * animProgress.value)
                        Offset(x, y)
                    }

                    // Draw Gradient Area & Line using cubic bezier
                    if (coords.isNotEmpty()) {
                        val linePath = Path().apply {
                            moveTo(coords[0].x, coords[0].y)
                            for (i in 1 until coords.size) {
                                val prev = coords[i - 1]
                                val cur = coords[i]
                                val cx1 = prev.x + (cur.x - prev.x) / 2
                                val cy1 = prev.y
                                val cx2 = prev.x + (cur.x - prev.x) / 2
                                val cy2 = cur.y
                                cubicTo(cx1, cy1, cx2, cy2, cur.x, cur.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(coords.last().x, height - paddingBottom)
                            lineTo(coords.first().x, height - paddingBottom)
                            close()
                        }

                        // Draw Fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(fillStartColor, fillEndColor),
                                startY = paddingTop,
                                endY = height - paddingBottom
                            )
                        )

                        // Draw Curve Line
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Points & Tooltips
                        coords.forEachIndexed { idx, offset ->
                            val isSelected = idx == selectedIndex

                            // Outer glow for selected
                            if (isSelected) {
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.25f),
                                    radius = 10.dp.toPx(),
                                    center = offset
                                )
                            }

                            // Inner point
                            drawCircle(
                                color = lineColor,
                                radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                                center = offset
                            )

                            // Tooltip badge for selected point
                            if (isSelected) {
                                val pointVal = points[idx].value.toInt()
                                val tagText = "$pointVal%"
                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 28f
                                    isAntiAlias = true
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                                val bgPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.rgb(79, 70, 229) // IndigoPrimary
                                    isAntiAlias = true
                                    setShadowLayer(8f, 0f, 4f, android.graphics.Color.argb(50, 0, 0, 0))
                                }

                                val tagY = (offset.y - 18.dp.toPx()).coerceAtLeast(14.dp.toPx())
                                val tagRect = android.graphics.RectF(
                                    offset.x - 36f,
                                    tagY - 26f,
                                    offset.x + 36f,
                                    tagY + 12f
                                )
                                drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 14f, 14f, bgPaint)
                                drawContext.canvas.nativeCanvas.drawText(tagText, offset.x, tagY, textPaint)
                            }
                        }
                    }
                }
            }
        }

        // X-Axis Labels (Mon, Tue, Wed, ...)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 6.dp)
        ) {
            points.forEachIndexed { idx, point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (idx == selectedIndex) IndigoPrimary else textLabelColor
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
