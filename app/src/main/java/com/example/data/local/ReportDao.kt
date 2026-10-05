package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM streetlight_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM streetlight_reports WHERE id = :id")
    fun getReportById(id: Long): Flow<ReportEntity?>

    @Query("SELECT * FROM streetlight_reports WHERE status = :status ORDER BY timestamp DESC")
    fun getReportsByStatus(status: String): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM streetlight_reports")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("UPDATE streetlight_reports SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun incrementUpvote(id: Long)

    @Query("UPDATE streetlight_reports SET status = :status, technicianNotes = :notes, technicianName = :techName, resolvedTimestamp = :resolvedTime WHERE id = :id")
    suspend fun updateTechnicianStatus(id: Long, status: String, notes: String, techName: String, resolvedTime: Long?)

    @Query("DELETE FROM streetlight_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)
}
