package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PanApplicationEntity
import com.example.data.model.RetailerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RetailerEntity::class, PanApplicationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun retailerDao(): RetailerDao
    abstract fun panApplicationDao(): PanApplicationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pan_mitra_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.retailerDao())
                    }
                }
            }

            suspend fun populateInitialData(retailerDao: RetailerDao) {
                if (retailerDao.getRetailersCount() == 0) {
                    retailerDao.insertRetailer(
                        RetailerEntity(
                            name = "Sharma Online Seva",
                            shopName = "Sharma CSC & Cyber Point",
                            mobile = "9876543210",
                            pin = "1234",
                            address = "Main Market Road, Patna",
                            isActive = true
                        )
                    )
                    retailerDao.insertRetailer(
                        RetailerEntity(
                            name = "Gupta Digital Center",
                            shopName = "Gupta Jan Seva Kendra",
                            mobile = "9812345678",
                            pin = "4321",
                            address = "Station Road, Lucknow",
                            isActive = true
                        )
                    )
                }
            }
        }
    }
}
