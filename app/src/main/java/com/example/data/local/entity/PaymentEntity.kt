package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val date: String, // format: "yyyy-MM-dd"
    val amount: Double,
    val paymentMode: String = "Cash", // Cash, UPI, PhonePe, GPay, Bank
    val notes: String = "",
    val receiptNo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
