package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.location.LocationHelper
import com.example.data.model.FaultType
import com.example.data.model.Severity
import com.example.ui.StreetlightViewModel
import com.example.ui.components.ImageAttachmentRow
import com.example.ui.components.InteractiveMapCanvas
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    viewModel: StreetlightViewModel,
    onNavigateToFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val draft by viewModel.draft.collectAsState()
    val scrollState = rememberScrollState()

    var showReceiptDialog by remember { mutableStateOf<String?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }

    // Runtime Permission Request for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            viewModel.setLocatingState(true, "Acquiring satellite GPS fix...")
            LocationHelper.requestRealTimeLocation(
                context = context,
                onSuccess = { lat, lng, address ->
                    viewModel.updateDraftLocation(lat, lng, address)
                    Toast.makeText(context, "Location tagged successfully!", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    viewModel.setLocatingState(false, err)
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            viewModel.setLocatingState(false, "Permission denied")
            Toast.makeText(context, "GPS permission is needed for real-time location tagging", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Civic Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Report Streetlight Fault",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Public Works & Electrical Maintenance Dispatch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "💡", fontSize = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 1: Real-time Location Tagging
        SectionHeader(
            icon = Icons.Default.LocationOn,
            title = "1. Real-Time Location Tagging",
            subtitle = "Tag the exact streetlight coordinates via GPS or adjust on map"
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // GPS Tagging Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (LocationHelper.hasLocationPermission(context)) {
                                viewModel.setLocatingState(true, "Acquiring satellite GPS fix...")
                                LocationHelper.requestRealTimeLocation(
                                    context = context,
                                    onSuccess = { lat, lng, address ->
                                        viewModel.updateDraftLocation(lat, lng, address)
                                        Toast.makeText(context, "Location tagged: $address", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        viewModel.setLocatingState(false, err)
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (draft.isGpsTagged) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tag_gps_button")
                    ) {
                        if (draft.isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Locking GPS...", fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = if (draft.isGpsTagged) Icons.Default.CheckCircle else Icons.Default.GpsFixed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (draft.isGpsTagged) "Re-Tag Real GPS" else "Tag My Real GPS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { isMapExpanded = !isMapExpanded },
                        modifier = Modifier.testTag("toggle_pin_map_button")
                    ) {
                        Text(if (isMapExpanded) "Hide Map" else "Fine-Tune Pin", fontSize = 12.sp)
                    }
                }

                if (draft.locationMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = draft.locationMessage,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Street Address Field
                OutlinedTextField(
                    value = draft.address,
                    onValueChange = { viewModel.updateDraftAddress(it) },
                    label = { Text("Street Address / Intersection") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // GPS Coordinates Pill & Pole Number Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft.poleNumber,
                        onValueChange = { viewModel.updateDraftPole(it) },
                        label = { Text("Pole ID (e.g. SL-104)") },
                        placeholder = { Text("SL-204") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pole_id_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "GPS COORDINATES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.US, "%.4f, %.4f", draft.latitude, draft.longitude),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = draft.landmark,
                    onValueChange = { viewModel.updateDraftLandmark(it) },
                    label = { Text("Nearby Landmark / Cross Street (Optional)") },
                    placeholder = { Text("e.g. In front of Central Library steps") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("landmark_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Interactive Mini Map Pin fine-tuning
                AnimatedVisibility(visible = isMapExpanded) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Tap on the map grid below to move the streetlight pin:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        InteractiveMapCanvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            userLatitude = draft.latitude,
                            userLongitude = draft.longitude,
                            centerLatitude = draft.latitude,
                            centerLongitude = draft.longitude,
                            isPinSelectionMode = true,
                            selectedPinLat = draft.latitude,
                            selectedPinLng = draft.longitude,
                            onPinMoved = { newLat, newLng ->
                                val resolvedAddr = LocationHelper.resolveAddress(context, newLat, newLng)
                                viewModel.updateDraftLocation(newLat, newLng, resolvedAddr)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 2: Fault Type & Hazard Priority
        SectionHeader(
            icon = Icons.Default.Warning,
            title = "2. Fault Classification & Hazard",
            subtitle = "Select observed behavior and urgency level"
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Select Fault Symptom:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fault Types Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FaultType.entries.forEach { type ->
                        val isSelected = draft.faultType == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateDraftFaultType(type) }
                                .testTag("fault_type_${type.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getFaultIcon(type),
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = type.description,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority / Severity Selector Chips
                Text(
                    text = "Hazard / Severity Level:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Severity.entries.forEach { severity ->
                        val isSelected = draft.severity == severity
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateDraftSeverity(severity) },
                            label = { Text(severity.title, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (severity) {
                                    Severity.CRITICAL -> Color(0xFFEF4444)
                                    Severity.HIGH -> Color(0xFFF97316)
                                    Severity.MEDIUM -> Color(0xFFF59E0B)
                                    Severity.LOW -> Color(0xFF10B981)
                                },
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("severity_${severity.name}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 3: Image Evidence Uploads
        SectionHeader(
            icon = Icons.Default.BrokenImage,
            title = "3. Photo Evidence & Uploads",
            subtitle = "Take a real-time photo of the damaged pole or unlit lantern"
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ImageAttachmentRow(
                    photoUris = draft.photoUris,
                    onAddPhoto = { uri -> viewModel.addPhoto(uri) },
                    onRemovePhoto = { uri -> viewModel.removePhoto(uri) }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 4: Citizen Details & Notes
        SectionHeader(
            icon = Icons.Outlined.Info,
            title = "4. Additional Details & Contact",
            subtitle = "Provide context and optional contact for SMS/Email repair updates"
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { viewModel.updateDraftDescription(it) },
                    label = { Text("Citizen Notes / Fault Details") },
                    placeholder = { Text("e.g. Pedestrians cannot see crosswalk at night. Street pole base looks dented.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft.reporterName,
                        onValueChange = { viewModel.updateDraftReporter(it, draft.reporterContact) },
                        label = { Text("Your Name (Optional)") },
                        placeholder = { Text("Jane Doe") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reporter_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = draft.reporterContact,
                        onValueChange = { viewModel.updateDraftReporter(draft.reporterName, it) },
                        label = { Text("Phone / Email (For Status)") },
                        placeholder = { Text("555-0199") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reporter_contact_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SUBMIT BUTTON
        Button(
            onClick = {
                viewModel.submitReport { ticketId ->
                    showReceiptDialog = ticketId
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_report_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Submit Streetlight Report",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Submission Confirmation Receipt Dialog
    showReceiptDialog?.let { ticketId ->
        Dialog(onDismissRequest = {
            showReceiptDialog = null
            onNavigateToFeed()
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFD1FAE5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Report Dispatched!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Public Works Electrical Maintenance Queue",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "OFFICIAL TICKET NUMBER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "#$ticketId",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estimated crew inspection within 24-48 hours",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            showReceiptDialog = null
                            onNavigateToFeed()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("track_ticket_button")
                    ) {
                        Text("View in Active Outages Feed", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getFaultIcon(type: FaultType): ImageVector {
    return when (type) {
        FaultType.TOTAL_OUTAGE -> Icons.Default.Lightbulb
        FaultType.FLICKERING -> Icons.Default.FlashOn
        FaultType.DAY_BURNING -> Icons.Default.WbSunny
        FaultType.DAMAGED_POLE -> Icons.Default.Warning
        FaultType.EXPOSED_WIRES -> Icons.Default.ElectricBolt
        FaultType.BROKEN_FIXTURE -> Icons.Default.BrokenImage
        FaultType.DIM_ILLUMINATION -> Icons.Default.BrightnessLow
    }
}
