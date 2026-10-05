package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportStatus
import com.example.data.model.Severity

@Composable
fun StatusBadge(
    statusStr: String,
    modifier: Modifier = Modifier
) {
    val status = ReportStatus.fromString(statusStr)
    val (bgColor, textColor, icon) = when (status) {
        ReportStatus.SUBMITTED -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.HourglassEmpty)
        ReportStatus.ACKNOWLEDGED -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.DirectionsWalk)
        ReportStatus.IN_PROGRESS -> Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), Icons.Default.Build)
        ReportStatus.RESOLVED -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Icons.Default.CheckCircle)
        ReportStatus.CLOSED -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Default.CheckCircle)
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier
                .size(14.dp)
                .padding(end = 4.dp)
        )
        Text(
            text = status.title,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SeverityBadge(
    severityStr: String,
    modifier: Modifier = Modifier
) {
    val severity = Severity.fromString(severityStr)
    val (dotColor, bgColor, textColor) = when (severity) {
        Severity.LOW -> Triple(Color(0xFF10B981), Color(0xFFECFDF5), Color(0xFF047857))
        Severity.MEDIUM -> Triple(Color(0xFFF59E0B), Color(0xFFFFFBEB), Color(0xFFB45309))
        Severity.HIGH -> Triple(Color(0xFFF97316), Color(0xFFFFF7ED), Color(0xFFC2410C))
        Severity.CRITICAL -> Triple(Color(0xFFEF4444), Color(0xFFFEF2F2), Color(0xFFB91C1C))
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 5.dp)
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = severity.title,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
