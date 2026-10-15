package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val milkType: String = "Cow", // Cow, Buffalo, Mixed
    val defaultMorningRate: Double = 55.0,
    val defaultEveningRate: Double = 55.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
