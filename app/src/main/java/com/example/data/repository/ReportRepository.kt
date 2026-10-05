package com.example.data.repository

import com.example.data.local.ReportDao
import com.example.data.local.ReportEntity
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()

    fun getReportById(id: Long): Flow<ReportEntity?> = reportDao.getReportById(id)

    fun getReportsByStatus(status: String): Flow<List<ReportEntity>> = reportDao.getReportsByStatus(status)

    suspend fun insertReport(report: ReportEntity): Long = reportDao.insertReport(report)

    suspend fun updateReport(report: ReportEntity) = reportDao.updateReport(report)

    suspend fun upvoteReport(id: Long) = reportDao.incrementUpvote(id)

    suspend fun updateTechnicianStatus(
        id: Long,
        status: String,
        notes: String,
        techName: String,
        resolvedTime: Long?
    ) = reportDao.updateTechnicianStatus(id, status, notes, techName, resolvedTime)

    suspend fun deleteReport(id: Long) = reportDao.deleteReportById(id)
}
