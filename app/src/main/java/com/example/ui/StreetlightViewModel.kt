package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ReportDatabase
import com.example.data.local.ReportEntity
import com.example.data.model.FaultType
import com.example.data.model.ReportStatus
import com.example.data.model.Severity
import com.example.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class NewReportDraft(
    val poleNumber: String = "",
    val faultType: FaultType = FaultType.TOTAL_OUTAGE,
    val severity: Severity = Severity.HIGH,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val address: String = "Tap 'Tag My Real GPS' or enter street",
    val landmark: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(),
    val reporterName: String = "",
    val reporterContact: String = "",
    val isGpsTagged: Boolean = false,
    val isLocating: Boolean = false,
    val locationMessage: String = ""
)

class StreetlightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReportRepository

    init {
        val database = ReportDatabase.getDatabase(application, viewModelScope)
        repository = ReportRepository(database.reportDao())
    }

    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _selectedReport = MutableStateFlow<ReportEntity?>(null)
    val selectedReport: StateFlow<ReportEntity?> = _selectedReport.asStateFlow()

    private val _draft = MutableStateFlow(NewReportDraft())
    val draft: StateFlow<NewReportDraft> = _draft.asStateFlow()

    private val _submissionSuccessTicket = MutableStateFlow<String?>(null)
    val submissionSuccessTicket: StateFlow<String?> = _submissionSuccessTicket.asStateFlow()

    val filteredReports = combine(allReports, _searchQuery, _statusFilter) { reports, query, filter ->
        reports.filter { report ->
            val matchesFilter = if (filter == "ALL") true else report.status.equals(filter, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                report.ticketId.contains(query, ignoreCase = true) ||
                report.address.contains(query, ignoreCase = true) ||
                report.poleNumber.contains(query, ignoreCase = true) ||
                report.landmark.contains(query, ignoreCase = true) ||
                report.faultType.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun selectReport(report: ReportEntity?) {
        _selectedReport.value = report
    }

    fun updateDraftPole(pole: String) {
        _draft.value = _draft.value.copy(poleNumber = pole)
    }

    fun updateDraftFaultType(type: FaultType) {
        _draft.value = _draft.value.copy(
            faultType = type,
            severity = type.defaultSeverity
        )
    }

    fun updateDraftSeverity(severity: Severity) {
        _draft.value = _draft.value.copy(severity = severity)
    }

    fun updateDraftAddress(address: String) {
        _draft.value = _draft.value.copy(address = address)
    }

    fun updateDraftLocation(lat: Double, lng: Double, address: String) {
        _draft.value = _draft.value.copy(
            latitude = lat,
            longitude = lng,
            address = address,
            isGpsTagged = true,
            isLocating = false,
            locationMessage = "GPS accuracy locked (±4m)"
        )
    }

    fun setLocatingState(isLocating: Boolean, message: String = "") {
        _draft.value = _draft.value.copy(
            isLocating = isLocating,
            locationMessage = message
        )
    }

    fun updateDraftLandmark(landmark: String) {
        _draft.value = _draft.value.copy(landmark = landmark)
    }

    fun updateDraftDescription(description: String) {
        _draft.value = _draft.value.copy(description = description)
    }

    fun updateDraftReporter(name: String, contact: String) {
        _draft.value = _draft.value.copy(reporterName = name, reporterContact = contact)
    }

    fun addPhoto(uriString: String) {
        val currentPhotos = _draft.value.photoUris
        if (!currentPhotos.contains(uriString)) {
            _draft.value = _draft.value.copy(photoUris = currentPhotos + uriString)
        }
    }

    fun removePhoto(uriString: String) {
        _draft.value = _draft.value.copy(
            photoUris = _draft.value.photoUris.filter { it != uriString }
        )
    }

    fun submitReport(onSuccess: (ticketId: String) -> Unit) {
        val currentDraft = _draft.value
        val ticketId = "SL-" + Random.nextInt(1000, 9999)
        val poleNumber = if (currentDraft.poleNumber.isBlank()) "SL-${Random.nextInt(100, 999)}" else currentDraft.poleNumber
        val address = if (currentDraft.address.startsWith("Tap 'Tag")) "Civic District #4" else currentDraft.address

        val newEntity = ReportEntity(
            ticketId = ticketId,
            faultType = currentDraft.faultType.name,
            severity = currentDraft.severity.name,
            status = ReportStatus.SUBMITTED.name,
            poleNumber = poleNumber,
            address = address,
            latitude = currentDraft.latitude,
            longitude = currentDraft.longitude,
            landmark = currentDraft.landmark,
            description = currentDraft.description.ifBlank { "Citizen reported ${currentDraft.faultType.title}." },
            photoUris = currentDraft.photoUris.joinToString(","),
            reporterName = currentDraft.reporterName.ifBlank { "Civic Watcher" },
            reporterContact = currentDraft.reporterContact,
            timestamp = System.currentTimeMillis(),
            upvotes = 1,
            technicianName = "",
            technicianNotes = ""
        )

        viewModelScope.launch {
            repository.insertReport(newEntity)
            _submissionSuccessTicket.value = ticketId
            _draft.value = NewReportDraft()
            onSuccess(ticketId)
        }
    }

    fun clearSubmissionNotice() {
        _submissionSuccessTicket.value = null
    }

    fun upvoteReport(id: Long) {
        viewModelScope.launch {
            repository.upvoteReport(id)
        }
    }

    fun updateTechnicianStatus(
        id: Long,
        newStatus: ReportStatus,
        technicianNotes: String,
        technicianName: String
    ) {
        viewModelScope.launch {
            val resolvedTime = if (newStatus == ReportStatus.RESOLVED) System.currentTimeMillis() else null
            repository.updateTechnicianStatus(
                id = id,
                status = newStatus.name,
                notes = technicianNotes,
                techName = technicianName,
                resolvedTime = resolvedTime
            )
            // Refresh currently viewed report if open
            if (_selectedReport.value?.id == id) {
                _selectedReport.value = _selectedReport.value?.copy(
                    status = newStatus.name,
                    technicianNotes = technicianNotes,
                    technicianName = technicianName,
                    resolvedTimestamp = resolvedTime
                )
            }
        }
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
            if (_selectedReport.value?.id == id) {
                _selectedReport.value = null
            }
        }
    }
}
