package com.example.data.repository

import com.example.data.local.PanApplicationDao
import com.example.data.local.RetailerDao
import com.example.data.model.PanApplicationEntity
import com.example.data.model.RetailerEntity
import kotlinx.coroutines.flow.Flow

class PanRepository(
    private val retailerDao: RetailerDao,
    private val panApplicationDao: PanApplicationDao
) {
    // Retailer Operations
    val allRetailers: Flow<List<RetailerEntity>> = retailerDao.getAllRetailers()

    suspend fun getRetailerById(id: Long): RetailerEntity? = retailerDao.getRetailerById(id)

    suspend fun getRetailerByMobile(mobile: String): RetailerEntity? = retailerDao.getRetailerByMobile(mobile)

    suspend fun authenticateRetailer(mobile: String, pin: String): RetailerEntity? {
        return retailerDao.authenticate(mobile.trim(), pin.trim())
    }

    suspend fun insertRetailer(retailer: RetailerEntity): Long {
        return retailerDao.insertRetailer(retailer)
    }

    suspend fun updateRetailer(retailer: RetailerEntity) {
        retailerDao.updateRetailer(retailer)
    }

    suspend fun deleteRetailer(retailer: RetailerEntity) {
        retailerDao.deleteRetailer(retailer)
    }

    // Application Operations
    val allApplications: Flow<List<PanApplicationEntity>> = panApplicationDao.getAllApplications()

    fun getApplicationsByRetailer(retailerId: Long): Flow<List<PanApplicationEntity>> {
        return panApplicationDao.getApplicationsByRetailer(retailerId)
    }

    suspend fun getApplicationById(id: Long): PanApplicationEntity? {
        return panApplicationDao.getApplicationById(id)
    }

    suspend fun insertApplication(application: PanApplicationEntity): Long {
        return panApplicationDao.insertApplication(application)
    }

    suspend fun updateApplication(application: PanApplicationEntity) {
        panApplicationDao.updateApplication(application)
    }

    suspend fun deleteApplication(application: PanApplicationEntity) {
        panApplicationDao.deleteApplication(application)
    }

    suspend fun updateApplicationStatus(
        id: Long,
        status: String,
        ackNumber: String,
        panNumber: String,
        remarks: String
    ) {
        panApplicationDao.updateStatus(
            id = id,
            status = status,
            ackNumber = ackNumber,
            panNumber = panNumber,
            remarks = remarks,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun getCounts(): ApplicationCounts {
        val total = panApplicationDao.getTotalCount()
        val pending = panApplicationDao.getPendingCount()
        val completed = panApplicationDao.getCompletedCount()
        val rejected = panApplicationDao.getRejectedCount()
        val retailers = retailerDao.getRetailersCount()
        return ApplicationCounts(total, pending, completed, rejected, retailers)
    }
}

data class ApplicationCounts(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0,
    val rejected: Int = 0,
    val totalRetailers: Int = 0
)
