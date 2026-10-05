package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ReportEntity
import com.example.data.model.FaultType
import com.example.data.model.ReportStatus
import com.example.ui.StreetlightViewModel
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatusBadge

@Composable
fun TechnicianScreen(
    viewModel: StreetlightViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allReports by viewModel.allReports.collectAsState()

    var filterCategory by remember { mutableStateOf("OPEN") }
    var technicianName by remember { mutableStateOf("Tech Crew #7 (Alvarez)") }

    var updatingReport by remember { mutableStateOf<ReportEntity?>(null) }
    var notesInput by remember { mutableStateOf("") }
    var targetStatus by remember { mutableStateOf(ReportStatus.IN_PROGRESS) }

    val filteredList = remember(allReports, filterCategory) {
        when (filterCategory) {
            "OPEN" -> allReports.filter { it.status == ReportStatus.SUBMITTED.name }
                .sortedByDescending { it.severity == "CRITICAL" }
            "IN_PROGRESS" -> allReports.filter { it.status == ReportStatus.IN_PROGRESS.name || it.status == ReportStatus.ACKNOWLEDGED.name }
            "RESOLVED" -> allReports.filter { it.status == ReportStatus.RESOLVED.name || it.status == ReportStatus.CLOSED.name }
            else -> allReports
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Technician Header Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Field Operations & Dispatch",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Signed in as: $technicianName",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf(
                "OPEN" to "Unassigned Queue (${allReports.count { it.status == ReportStatus.SUBMITTED.name }})",
                "IN_PROGRESS" to "In Repair (${allReports.count { it.status == ReportStatus.IN_PROGRESS.name || it.status == ReportStatus.ACKNOWLEDGED.name }})",
                "RESOLVED" to "Completed (${allReports.count { it.status == ReportStatus.RESOLVED.name }})",
                "ALL" to "All Work Orders (${allReports.size})"
            )

            items(tabs) { (key, title) ->
                val isSelected = filterCategory == key
                FilterChip(
                    selected = isSelected,
                    onClick = { filterCategory = key },
                    label = { Text(title, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("tech_filter_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Work Order Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Queue Clear!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "No work orders matching this filter.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { report ->
                    val faultType = FaultType.fromString(report.faultType)
                    val status = ReportStatus.fromString(report.status)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_work_order_${report.ticketId}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Ticket #, Pole, Severity
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
                                        text = "WO #${report.ticketId}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "Pole ${report.poleNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StatusBadge(statusStr = report.status)
                                    SeverityBadge(severityStr = report.severity)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = faultType.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = report.address,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (report.landmark.isNotBlank()) {
                                Text(
                                    text = "Landmark: ${report.landmark}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (report.technicianNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Field Log (${report.technicianName}):",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = report.technicianNotes,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Field Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (status) {
                                    ReportStatus.SUBMITTED -> {
                                        Button(
                                            onClick = {
                                                viewModel.updateTechnicianStatus(
                                                    id = report.id,
                                                    newStatus = ReportStatus.IN_PROGRESS,
                                                    technicianNotes = "Dispatched and claimed by $technicianName. En route to pole ${report.poleNumber}.",
                                                    technicianName = technicianName
                                                )
                                                Toast.makeText(context, "Work Order #${report.ticketId} claimed!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Claim & Dispatch", fontSize = 12.sp)
                                        }
                                    }
                                    ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS -> {
                                        OutlinedButton(
                                            onClick = {
                                                updatingReport = report
                                                notesInput = report.technicianNotes
                                                targetStatus = ReportStatus.IN_PROGRESS
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Log Notes", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                updatingReport = report
                                                notesInput = report.technicianNotes
                                                targetStatus = ReportStatus.RESOLVED
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Repaired", fontSize = 12.sp)
                                        }
                                    }
                                    ReportStatus.RESOLVED, ReportStatus.CLOSED -> {
                                        OutlinedButton(
                                            onClick = { viewModel.selectReport(report) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("View Work Receipt", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Log Repair Notes / Confirm Resolution
    updatingReport?.let { report ->
        Dialog(onDismissRequest = { updatingReport = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (targetStatus == ReportStatus.RESOLVED) "Complete Repair #${report.ticketId}" else "Log Field Repair Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { updatingReport = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pole: ${report.poleNumber} • ${report.address}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Parts used & Repair details") },
                        placeholder = { Text("e.g. Replaced 150W LED driver & repaired loose splice wires. Luminaire lux test passed.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_notes_field"),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val notes = notesInput.ifBlank {
                                if (targetStatus == ReportStatus.RESOLVED) "Luminaire inspected, parts replaced, and circuit restored by $technicianName."
                                else "Inspection underway."
                            }
                            viewModel.updateTechnicianStatus(
                                id = report.id,
                                newStatus = targetStatus,
                                technicianNotes = notes,
                                technicianName = technicianName
                            )
                            Toast.makeText(
                                context,
                                if (targetStatus == ReportStatus.RESOLVED) "Streetlight marked repaired & illuminated!" else "Notes saved!",
                                Toast.LENGTH_SHORT
                            ).show()
                            updatingReport = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (targetStatus == ReportStatus.RESOLVED) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_tech_update_button")
                    ) {
                        Text(
                            text = if (targetStatus == ReportStatus.RESOLVED) "Confirm Resolution & Restore Light" else "Save Notes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
