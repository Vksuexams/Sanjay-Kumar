package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val dairyName: String = "Shree Krishna Dairy / श्री कृष्णा डेरी",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val upiId: String = "",
    val defaultCowRate: Double = 55.0,
    val defaultBuffaloRate: Double = 65.0,
    val appLanguage: String = "hi", // "hi" or "en"
    val invoicePrefix: String = "MH"
)
