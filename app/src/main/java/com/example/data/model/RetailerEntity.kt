package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "retailers")
data class RetailerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shopName: String,
    val mobile: String,
    val pin: String,
    val address: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
