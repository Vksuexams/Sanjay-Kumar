package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId AND date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getPaymentsForCustomerInMonth(customerId: Long, monthPrefix: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId AND date LIKE :monthPrefix || '%' ORDER BY date ASC")
    suspend fun getPaymentsForCustomerInMonthDirect(customerId: Long, monthPrefix: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE customerId = :customerId AND date < :datePrefix ORDER BY date ASC")
    suspend fun getPaymentsBeforeDate(customerId: Long, datePrefix: String): List<PaymentEntity>

    @Query("SELECT SUM(amount) FROM payments WHERE customerId = :customerId")
    fun getTotalPaidByCustomer(customerId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("DELETE FROM payments WHERE customerId = :customerId")
    suspend fun deletePaymentsByCustomer(customerId: Long)

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsDirect(): List<PaymentEntity>
}
