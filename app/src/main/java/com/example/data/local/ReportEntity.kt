package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streetlight_reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketId: String,
    val faultType: String,
    val severity: String,
    val status: String,
    val poleNumber: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val landmark: String = "",
    val description: String = "",
    val photoUris: String = "", // Comma-separated paths or content URIs
    val reporterName: String = "",
    val reporterContact: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 1,
    val technicianName: String = "",
    val technicianNotes: String = "",
    val resolvedTimestamp: Long? = null
)
