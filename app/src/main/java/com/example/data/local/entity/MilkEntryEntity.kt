package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milk_entries",
    indices = [
        Index(value = ["customerId", "date"], unique = true)
    ]
)
data class MilkEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val date: String, // format: "yyyy-MM-dd"
    val morningLitres: Double = 0.0,
    val morningRate: Double = 55.0,
    val eveningLitres: Double = 0.0,
    val eveningRate: Double = 55.0,
    val totalLitres: Double = 0.0,
    val totalAmount: Double = 0.0,
    val notes: String = ""
)
