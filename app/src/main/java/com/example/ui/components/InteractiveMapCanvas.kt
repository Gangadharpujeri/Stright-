package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReportEntity
import com.example.data.model.ReportStatus
import kotlin.math.sqrt

@Composable
fun InteractiveMapCanvas(
    modifier: Modifier = Modifier,
    reports: List<ReportEntity> = emptyList(),
    selectedReportId: Long? = null,
    userLatitude: Double = 37.7749,
    userLongitude: Double = -122.4194,
    centerLatitude: Double = 37.7780,
    centerLongitude: Double = -122.4180,
    isPinSelectionMode: Boolean = false,
    selectedPinLat: Double? = null,
    selectedPinLng: Double? = null,
    onPinMoved: ((lat: Double, lng: Double) -> Unit)? = null,
    onReportSelected: ((ReportEntity) -> Unit)? = null
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing radar animation for real GPS position
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("map_canvas")
                .pointerInput(isPinSelectionMode, reports, zoomLevel, panOffsetX, panOffsetY) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f + panOffsetX
                        val cy = h / 2f + panOffsetY
                        val scale = 5000f * zoomLevel

                        if (isPinSelectionMode && onPinMoved != null) {
                            val lng = centerLongitude + (tapOffset.x - cx) / scale
                            val lat = centerLatitude - (tapOffset.y - cy) / scale
                            onPinMoved(lat, lng)
                        } else if (onReportSelected != null) {
                            // Find closest report pin within tap radius
                            var closest: ReportEntity? = null
                            var minDist = 40f
                            reports.forEach { report ->
                                val px = cx + ((report.longitude - centerLongitude) * scale).toFloat()
                                val py = cy - ((report.latitude - centerLatitude) * scale).toFloat()
                                val dist = sqrt((tapOffset.x - px) * (tapOffset.x - px) + (tapOffset.y - py) * (tapOffset.y - py))
                                if (dist < minDist) {
                                    minDist = dist
                                    closest = report
                                }
                            }
                            closest?.let { onReportSelected(it) }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f + panOffsetX
            val cy = h / 2f + panOffsetY
            val scale = 5000f * zoomLevel

            // 1. Draw Map Land & Water
            drawRect(color = Color(0xFF131D31), size = size)

            // Draw civic park zones
            drawRoundRect(
                color = Color(0xFF132A24),
                topLeft = Offset(cx - 300f * zoomLevel, cy - 250f * zoomLevel),
                size = Size(200f * zoomLevel, 140f * zoomLevel),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFF132A24),
                topLeft = Offset(cx + 100f * zoomLevel, cy + 120f * zoomLevel),
                size = Size(180f * zoomLevel, 100f * zoomLevel),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // Draw river / bay water curve
            val riverPath = Path().apply {
                moveTo(0f, cy + 280f * zoomLevel)
                cubicTo(
                    w * 0.4f, cy + 220f * zoomLevel,
                    w * 0.6f, cy + 320f * zoomLevel,
                    w, cy + 260f * zoomLevel
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = riverPath, color = Color(0xFF0C243B))

            // 2. Draw Street Grid Lines
            val gridStep = 70f * zoomLevel
            val streetColor = Color(0xFF23354E)
            val majorAvenueColor = Color(0xFF2E4666)

            var x = (cx % gridStep)
            while (x < w) {
                val isMajor = ((x - cx) / gridStep).toInt() % 3 == 0
                drawLine(
                    color = if (isMajor) majorAvenueColor else streetColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = if (isMajor) 4f * zoomLevel else 2f * zoomLevel
                )
                x += gridStep
            }

            var y = (cy % gridStep)
            while (y < h) {
                val isMajor = ((y - cy) / gridStep).toInt() % 3 == 0
                drawLine(
                    color = if (isMajor) majorAvenueColor else streetColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (isMajor) 4f * zoomLevel else 2f * zoomLevel
                )
                y += gridStep
            }

            // Diagonal boulevard
            drawLine(
                color = Color(0xFF384D6B),
                start = Offset(cx - 400f * zoomLevel, cy + 300f * zoomLevel),
                end = Offset(cx + 400f * zoomLevel, cy - 300f * zoomLevel),
                strokeWidth = 6f * zoomLevel,
                cap = StrokeCap.Round
            )

            // 3. Draw Reported Streetlights
            reports.forEach { report ->
                val px = cx + ((report.longitude - centerLongitude) * scale).toFloat()
                val py = cy - ((report.latitude - centerLatitude) * scale).toFloat()

                if (px in -50f..(w + 50f) && py in -50f..(h + 50f)) {
                    val status = ReportStatus.fromString(report.status)
                    val isSelected = report.id == selectedReportId

                    val pinColor = when (status) {
                        ReportStatus.SUBMITTED -> Color(0xFFEF4444) // Red: Open fault
                        ReportStatus.ACKNOWLEDGED -> Color(0xFFF97316) // Orange: Dispatched
                        ReportStatus.IN_PROGRESS -> Color(0xFFF59E0B) // Amber: Repairing
                        ReportStatus.RESOLVED, ReportStatus.CLOSED -> Color(0xFF10B981) // Green: Fixed
                    }

                    // Soft aura
                    drawCircle(
                        color = pinColor.copy(alpha = if (isSelected) 0.45f else 0.2f),
                        radius = if (isSelected) 22f else 14f,
                        center = Offset(px, py)
                    )

                    // Pin marker base circle
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 10f else 7f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = pinColor,
                        radius = if (isSelected) 8f else 5.5f,
                        center = Offset(px, py)
                    )

                    // Selection halo ring
                    if (isSelected) {
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 26f,
                            center = Offset(px, py),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }

            // 4. Draw Selected Pin in Pin Mode (User fine-tuning report location)
            if (isPinSelectionMode && selectedPinLat != null && selectedPinLng != null) {
                val px = cx + ((selectedPinLng - centerLongitude) * scale).toFloat()
                val py = cy - ((selectedPinLat - centerLatitude) * scale).toFloat()

                // Target reticle
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.3f),
                    radius = 32f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 16f,
                    center = Offset(px, py),
                    style = Stroke(width = 3f)
                )
                // Center point
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 6f,
                    center = Offset(px, py)
                )
            }

            // 5. Draw User's Real GPS Location
            val userPx = cx + ((userLongitude - centerLongitude) * scale).toFloat()
            val userPy = cy - ((userLatitude - centerLatitude) * scale).toFloat()

            // Radar pulse ring
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(userPx, userPy),
                style = Stroke(width = 2.5f)
            )
            // Solid center user dot
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(userPx, userPy)
            )
            drawCircle(
                color = Color(0xFF0284C7),
                radius = 6f,
                center = Offset(userPx, userPy)
            )
        }

        // Overlay Map Controls (Zoom in, Zoom out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(3.0f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.6f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        zoomLevel = 1.0f
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Map Legend at bottom left
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFFEF4444), label = "Open")
            LegendItem(color = Color(0xFFF59E0B), label = "Repair")
            LegendItem(color = Color(0xFF10B981), label = "Fixed")
            LegendItem(color = Color(0xFF0284C7), label = "My GPS")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
