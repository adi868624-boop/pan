package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PanApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PanApplicationDao {
    @Query("SELECT * FROM pan_applications ORDER BY createdAt DESC")
    fun getAllApplications(): Flow<List<PanApplicationEntity>>

    @Query("SELECT * FROM pan_applications WHERE retailerId = :retailerId ORDER BY createdAt DESC")
    fun getApplicationsByRetailer(retailerId: Long): Flow<List<PanApplicationEntity>>

    @Query("SELECT * FROM pan_applications WHERE id = :id LIMIT 1")
    suspend fun getApplicationById(id: Long): PanApplicationEntity?

    @Query("SELECT * FROM pan_applications WHERE applicationRef = :ref LIMIT 1")
    suspend fun getApplicationByRef(ref: String): PanApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: PanApplicationEntity): Long

    @Update
    suspend fun updateApplication(application: PanApplicationEntity)

    @Delete
    suspend fun deleteApplication(application: PanApplicationEntity)

    @Query("UPDATE pan_applications SET status = :status, ackNumber = :ackNumber, panNumber = :panNumber, adminRemarks = :remarks, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, ackNumber: String, panNumber: String, remarks: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM pan_applications")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM pan_applications WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM pan_applications WHERE status = 'COMPLETED' OR status = 'ACK_GENERATED'")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM pan_applications WHERE status = 'REJECTED'")
    suspend fun getRejectedCount(): Int
}
