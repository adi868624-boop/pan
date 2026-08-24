package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RetailerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RetailerDao {
    @Query("SELECT * FROM retailers ORDER BY createdAt DESC")
    fun getAllRetailers(): Flow<List<RetailerEntity>>

    @Query("SELECT * FROM retailers WHERE id = :id LIMIT 1")
    suspend fun getRetailerById(id: Long): RetailerEntity?

    @Query("SELECT * FROM retailers WHERE mobile = :mobile LIMIT 1")
    suspend fun getRetailerByMobile(mobile: String): RetailerEntity?

    @Query("SELECT * FROM retailers WHERE mobile = :mobile AND pin = :pin LIMIT 1")
    suspend fun authenticate(mobile: String, pin: String): RetailerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetailer(retailer: RetailerEntity): Long

    @Update
    suspend fun updateRetailer(retailer: RetailerEntity)

    @Delete
    suspend fun deleteRetailer(retailer: RetailerEntity)

    @Query("SELECT COUNT(*) FROM retailers")
    suspend fun getRetailersCount(): Int
}
