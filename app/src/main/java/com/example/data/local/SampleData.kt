package com.example.data.local

import com.example.data.model.FaultType
import com.example.data.model.ReportStatus
import com.example.data.model.Severity

object SampleData {
    val initialReports = listOf(
        ReportEntity(
            ticketId = "SL-9402",
            faultType = FaultType.EXPOSED_WIRES.name,
            severity = Severity.CRITICAL.name,
            status = ReportStatus.IN_PROGRESS.name,
            poleNumber = "SL-041",
            address = "Corner of 5th Ave & Market St",
            latitude = 37.7833,
            longitude = -122.4064,
            landmark = "Outside Metro Station Exit B",
            description = "Lower maintenance hatch is detached with high-voltage insulated wiring hanging near sidewalk pedestrians.",
            photoUris = "sample://wires",
            reporterName = "Marcus Vance",
            reporterContact = "marcus.v@example.com",
            timestamp = System.currentTimeMillis() - 7200000L, // 2 hrs ago
            upvotes = 7,
            technicianName = "Crew #4 - Alvarez",
            technicianNotes = "Perimeter cones deployed. Power shut down at transformer junction. Awaiting terminal splice parts."
        ),
        ReportEntity(
            ticketId = "SL-8831",
            faultType = FaultType.TOTAL_OUTAGE.name,
            severity = Severity.HIGH.name,
            status = ReportStatus.ACKNOWLEDGED.name,
            poleNumber = "SL-118",
            address = "742 Evergreen Terrace, Oak District",
            latitude = 37.7795,
            longitude = -122.4180,
            landmark = "Across from Community Center Playground",
            description = "Entire LED luminaire head dark since Monday storm. Very dark sidewalk for school kids walking home.",
            photoUris = "sample://dark",
            reporterName = "Elena Rostova",
            reporterContact = "(555) 321-9844",
            timestamp = System.currentTimeMillis() - 18000000L, // 5 hrs ago
            upvotes = 4,
            technicianName = "Dispatch Queue",
            technicianNotes = "Ticket dispatched to Sector 2 Electrical Team for night-shift inspection."
        ),
        ReportEntity(
            ticketId = "SL-8120",
            faultType = FaultType.DAMAGED_POLE.name,
            severity = Severity.CRITICAL.name,
            status = ReportStatus.SUBMITTED.name,
            poleNumber = "SL-309",
            address = "1200 Industrial Pkwy near Pier 32",
            latitude = 37.7702,
            longitude = -122.3920,
            landmark = "Opposite Freight Warehouse Gate 3",
            description = "Pole hit by turning delivery truck. Steel base cracked and pole leaning at 15-degree angle toward roadway.",
            photoUris = "sample://leaning",
            reporterName = "Devon Cooper",
            reporterContact = "d.cooper@logistics.org",
            timestamp = System.currentTimeMillis() - 3600000L, // 1 hr ago
            upvotes = 9,
            technicianName = "",
            technicianNotes = ""
        ),
        ReportEntity(
            ticketId = "SL-7649",
            faultType = FaultType.FLICKERING.name,
            severity = Severity.MEDIUM.name,
            status = ReportStatus.SUBMITTED.name,
            poleNumber = "SL-088",
            address = "388 Pine Street, Financial Corridor",
            latitude = 37.7915,
            longitude = -122.4012,
            landmark = "In front of Bluefin Bakery",
            description = "Light strobe-flashes every 4 seconds continuously. Distracting for nighttime drivers and pedestrians.",
            photoUris = "sample://flicker",
            reporterName = "Aisha Patel",
            reporterContact = "aisha.patel@citylife.io",
            timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
            upvotes = 3,
            technicianName = "",
            technicianNotes = ""
        ),
        ReportEntity(
            ticketId = "SL-6920",
            faultType = FaultType.DAY_BURNING.name,
            severity = Severity.LOW.name,
            status = ReportStatus.RESOLVED.name,
            poleNumber = "SL-204",
            address = "155 Lincoln Way, Sunset Parkside",
            latitude = 37.7650,
            longitude = -122.4650,
            landmark = "North Park perimeter trail",
            description = "Photocell sensor defective, high-pressure sodium lamp running full blast at noon on sunny days.",
            photoUris = "sample://dayburn",
            reporterName = "Howard Schultz",
            reporterContact = "howard.s@baynet.net",
            timestamp = System.currentTimeMillis() - 172800000L, // 2 days ago
            upvotes = 2,
            technicianName = "Senior Tech Jenkins",
            technicianNotes = "Replaced NEMA twist-lock photocontrol sensor and tested dusk-to-dawn relay. Luminaire functioning normally.",
            resolvedTimestamp = System.currentTimeMillis() - 43200000L
        ),
        ReportEntity(
            ticketId = "SL-6215",
            faultType = FaultType.BROKEN_FIXTURE.name,
            severity = Severity.HIGH.name,
            status = ReportStatus.RESOLVED.name,
            poleNumber = "SL-072",
            address = "850 Mission Blvd, SoMa District",
            latitude = 37.7818,
            longitude = -122.4055,
            landmark = "Near Public Library steps",
            description = "Glass refract shield broken by storm debris. Dangling acrylic lens hazard.",
            photoUris = "sample://broken",
            reporterName = "Chloe Zhao",
            reporterContact = "(555) 789-2041",
            timestamp = System.currentTimeMillis() - 259200000L, // 3 days ago
            upvotes = 5,
            technicianName = "Tech Garcia",
            technicianNotes = "Installed new polycarbonate vandal-resistant lens shield. Fixture re-torqued and tested.",
            resolvedTimestamp = System.currentTimeMillis() - 86400000L
        )
    )
}
