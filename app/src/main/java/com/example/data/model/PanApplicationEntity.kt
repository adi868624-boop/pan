package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pan_applications")
data class PanApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applicationRef: String,
    val retailerId: Long,
    val retailerName: String,
    val retailerShopName: String,
    val retailerMobile: String,
    
    // Customer Details
    val customerName: String,
    val fatherName: String,
    val motherName: String,
    val mobile: String,
    val email: String,
    val dob: String,
    val applicationType: String = "NEW_49A", // NEW_49A, CORRECTION, MINOR
    val physicalCardRequired: Boolean = true,
    
    // Document Image File Paths (stored locally in internal storage)
    val aadharFrontPath: String,
    val aadharBackPath: String,
    val voterFrontPath: String = "",
    val voterBackPath: String = "",
    val panPhotoPath: String = "",
    val signaturePath: String,
    val passportPhotoPath: String,
    
    // Processing status & Admin controls
    val status: String = "PENDING", // PENDING, IN_REVIEW, ACK_GENERATED, COMPLETED, REJECTED
    val ackNumber: String = "",
    val panNumber: String = "",
    val adminRemarks: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
