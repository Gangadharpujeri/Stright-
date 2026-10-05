package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReportEntity
import com.example.data.model.FaultType
import com.example.data.model.ReportStatus
import com.example.ui.StreetlightViewModel
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatusBadge

@Composable
fun CityMapScreen(
    viewModel: StreetlightViewModel,
    modifier: Modifier = Modifier
) {
    val allReports by viewModel.allReports.collectAsState()
    val draft by viewModel.draft.collectAsState()

    var showOnlyOpen by remember { mutableStateOf(false) }
    var selectedMapReport by remember { mutableStateOf<ReportEntity?>(null) }

    val displayedReports = remember(allReports, showOnlyOpen) {
        if (showOnlyOpen) {
            allReports.filter { it.status != ReportStatus.RESOLVED.name && it.status != ReportStatus.CLOSED.name }
        } else {
            allReports
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Full Interactive Map Canvas
        InteractiveMapCanvas(
            modifier = Modifier.fillMaxSize(),
            reports = displayedReports,
            selectedReportId = selectedMapReport?.id,
            userLatitude = draft.latitude,
            userLongitude = draft.longitude,
            centerLatitude = 37.7780,
            centerLongitude = -122.4180,
            isPinSelectionMode = false,
            onReportSelected = { report ->
                selectedMapReport = report
            }
        )

        // Top Filter Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Citywide Outages (${displayedReports.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FilterChip(
                    selected = showOnlyOpen,
                    onClick = { showOnlyOpen = !showOnlyOpen },
                    label = { Text("Only Active", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("map_filter_active_only")
                )
            }
        }

        // Bottom Selected Report Card Overlay
        AnimatedVisibility(
            visible = selectedMapReport != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            selectedMapReport?.let { report ->
                val faultType = FaultType.fromString(report.faultType)
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_selected_report_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "#${report.ticketId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Pole ${report.poleNumber}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(statusStr = report.status)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { selectedMapReport = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close card")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = faultType.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            SeverityBadge(severityStr = report.severity)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.address,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.upvoteReport(report.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Me Too (${report.upvotes})", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.selectReport(report) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inspect", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
