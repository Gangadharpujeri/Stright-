package com.example.data.model

enum class FaultType(
    val title: String,
    val description: String,
    val defaultSeverity: Severity,
    val iconName: String
) {
    TOTAL_OUTAGE(
        title = "Completely Dark / Out",
        description = "Light is completely off at night, leaving the road or sidewalk in darkness.",
        defaultSeverity = Severity.HIGH,
        iconName = "lightbulb_outline"
    ),
    FLICKERING(
        title = "Flickering / Intermittent",
        description = "Light constantly flashes, blinks, or cycles on and off repeatedly.",
        defaultSeverity = Severity.MEDIUM,
        iconName = "flash_on"
    ),
    DAY_BURNING(
        title = "Day-Burning (Always On)",
        description = "Light remains illuminated in broad daylight, wasting city electrical energy.",
        defaultSeverity = Severity.LOW,
        iconName = "wb_sunny"
    ),
    DAMAGED_POLE(
        title = "Damaged / Leaning Pole",
        description = "Pole struck by vehicle, tilting dangerously, or cracked structural base.",
        defaultSeverity = Severity.CRITICAL,
        iconName = "warning"
    ),
    EXPOSED_WIRES(
        title = "Exposed Wires / Electrical Hazard",
        description = "Open inspection cover or dangling electrical wires posing electrocution risk.",
        defaultSeverity = Severity.CRITICAL,
        iconName = "electric_bolt"
    ),
    BROKEN_FIXTURE(
        title = "Broken Glass / Lens Cover",
        description = "Cracked lens, missing shade, or hanging glass fixture at risk of falling.",
        defaultSeverity = Severity.HIGH,
        iconName = "broken_image"
    ),
    DIM_ILLUMINATION(
        title = "Dim Light / Low Voltage",
        description = "Lamp is extremely faint or glowing dull yellow/orange, insufficient visibility.",
        defaultSeverity = Severity.LOW,
        iconName = "brightness_low"
    );

    companion object {
        fun fromString(value: String): FaultType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: TOTAL_OUTAGE
        }
    }
}

enum class Severity(val title: String, val level: Int) {
    LOW("Low Priority", 1),
    MEDIUM("Medium Priority", 2),
    HIGH("High Priority", 3),
    CRITICAL("Immediate Hazard", 4);

    companion object {
        fun fromString(value: String): Severity {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class ReportStatus(val title: String, val stepIndex: Int) {
    SUBMITTED("Submitted", 0),
    ACKNOWLEDGED("Dispatched", 1),
    IN_PROGRESS("In Repair", 2),
    RESOLVED("Fixed & Verified", 3),
    CLOSED("Closed", 4);

    companion object {
        fun fromString(value: String): ReportStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SUBMITTED
        }
    }
}
