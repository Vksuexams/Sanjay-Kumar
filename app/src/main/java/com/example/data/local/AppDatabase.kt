package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BusinessProfileDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.MilkEntryDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.MilkEntryEntity
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        MilkEntryEntity::class,
        PaymentEntity::class,
        BusinessProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun milkEntryDao(): MilkEntryDao
    abstract fun paymentDao(): PaymentDao
    abstract fun businessProfileDao(): BusinessProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milk_hisab_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default business profile and initial sample data in background
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.businessProfileDao().insertOrUpdateProfile(
                                    BusinessProfileEntity(
                                        id = 1,
                                        dairyName = "Shree Krishna Dairy / कृष्णा डेरी",
                                        ownerName = "Dairy Farm / डेरी फार्म",
                                        phone = "9876543210",
                                        address = "Village Road, Dairy Nagar",
                                        upiId = "dairy@upi",
                                        defaultCowRate = 55.0,
                                        defaultBuffaloRate = 65.0,
                                        appLanguage = "hi",
                                        invoicePrefix = "MH"
                                    )
                                )
                                // Seed friendly starter customers for initial demonstration
                                val c1 = database.customerDao().insertCustomer(
                                    CustomerEntity(
                                        name = "Ramesh Kumar (रमेश कुमार)",
                                        phone = "9876543211",
                                        address = "House 12, Ward 3",
                                        milkType = "Cow",
                                        defaultMorningRate = 55.0,
                                        defaultEveningRate = 55.0,
                                        isActive = true
                                    )
                                )
                                val c2 = database.customerDao().insertCustomer(
                                    CustomerEntity(
                                        name = "Suresh Sharma (सुरेश शर्मा)",
                                        phone = "9876543212",
                                        address = "Main Bazar",
                                        milkType = "Buffalo",
                                        defaultMorningRate = 65.0,
                                        defaultEveningRate = 65.0,
                                        isActive = true
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
