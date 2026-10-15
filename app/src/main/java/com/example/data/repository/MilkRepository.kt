package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.MilkEntryEntity
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class MonthlyBillSummary(
    val customer: CustomerEntity,
    val month: String, // "yyyy-MM"
    val totalMorningLitres: Double,
    val totalEveningLitres: Double,
    val totalLitres: Double,
    val currentMonthMilkAmount: Double,
    val previousBalance: Double,
    val totalPaymentsInMonth: Double,
    val netBalanceDue: Double,
    val entries: List<MilkEntryEntity>,
    val payments: List<PaymentEntity>
)

data class BackupData(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val businessProfile: BusinessProfileEntity?,
    val customers: List<CustomerEntity>,
    val milkEntries: List<MilkEntryEntity>,
    val payments: List<PaymentEntity>
)

class MilkRepository(private val database: AppDatabase) {
    private val customerDao = database.customerDao()
    private val milkEntryDao = database.milkEntryDao()
    private val paymentDao = database.paymentDao()
    private val profileDao = database.businessProfileDao()

    // Customers
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    fun getActiveCustomers(): Flow<List<CustomerEntity>> = customerDao.getActiveCustomers()
    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerByIdFlow(id)
    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    suspend fun saveCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) {
        milkEntryDao.deleteEntriesByCustomer(customer.id)
        paymentDao.deletePaymentsByCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }

    // Milk Entries
    fun getAllEntries(): Flow<List<MilkEntryEntity>> = milkEntryDao.getAllEntries()
    fun getEntriesByDate(date: String): Flow<List<MilkEntryEntity>> = milkEntryDao.getEntriesByDate(date)
    fun getEntriesByCustomer(customerId: Long): Flow<List<MilkEntryEntity>> = milkEntryDao.getEntriesByCustomer(customerId)
    fun getEntriesForCustomerInMonth(customerId: Long, monthPrefix: String): Flow<List<MilkEntryEntity>> =
        milkEntryDao.getEntriesForCustomerInMonth(customerId, monthPrefix)

    suspend fun getEntryByCustomerAndDate(customerId: Long, date: String): MilkEntryEntity? =
        milkEntryDao.getEntryByCustomerAndDate(customerId, date)

    suspend fun saveMilkEntry(entry: MilkEntryEntity): Long {
        val calculatedTotalLitres = entry.morningLitres + entry.eveningLitres
        val calculatedTotalAmount = (entry.morningLitres * entry.morningRate) + (entry.eveningLitres * entry.eveningRate)
        val finalized = entry.copy(
            totalLitres = Math.round(calculatedTotalLitres * 100.0) / 100.0,
            totalAmount = Math.round(calculatedTotalAmount * 100.0) / 100.0
        )
        return milkEntryDao.insertOrUpdateEntry(finalized)
    }

    suspend fun deleteMilkEntry(id: Long) = milkEntryDao.deleteEntryById(id)

    // Payments
    fun getAllPayments(): Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentEntity>> = paymentDao.getPaymentsByCustomer(customerId)
    fun getPaymentsForCustomerInMonth(customerId: Long, monthPrefix: String): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForCustomerInMonth(customerId, monthPrefix)

    suspend fun savePayment(payment: PaymentEntity): Long = paymentDao.insertPayment(payment)
    suspend fun deletePayment(id: Long) = paymentDao.deletePaymentById(id)

    // Profile
    fun getBusinessProfile(): Flow<BusinessProfileEntity> = profileDao.getProfileFlow().map {
        it ?: BusinessProfileEntity(
            dairyName = "Shree Krishna Dairy / कृष्णा डेरी",
            ownerName = "Dairy Owner",
            phone = "9876543210",
            address = "Village Main Road",
            upiId = "dairy@upi",
            appLanguage = "hi"
        )
    }

    suspend fun saveBusinessProfile(profile: BusinessProfileEntity) = profileDao.insertOrUpdateProfile(profile)

    // Monthly Bill Calculation
    suspend fun calculateMonthlyBill(customerId: Long, monthPrefix: String): MonthlyBillSummary? {
        val customer = customerDao.getCustomerById(customerId) ?: return null

        val monthEntries = milkEntryDao.getEntriesForCustomerInMonthDirect(customerId, monthPrefix)
        val monthPayments = paymentDao.getPaymentsForCustomerInMonthDirect(customerId, monthPrefix)

        // Previous calculations (before month-01)
        val firstDayOfMonth = "$monthPrefix-01"
        val priorEntries = milkEntryDao.getEntriesBeforeDate(customerId, firstDayOfMonth)
        val priorPayments = paymentDao.getPaymentsBeforeDate(customerId, firstDayOfMonth)

        val priorMilkTotal = priorEntries.sumOf { it.totalAmount }
        val priorPaidTotal = priorPayments.sumOf { it.amount }
        val previousBalance = priorMilkTotal - priorPaidTotal

        val currentMonthMilkAmount = monthEntries.sumOf { it.totalAmount }
        val currentMonthMorningLitres = monthEntries.sumOf { it.morningLitres }
        val currentMonthEveningLitres = monthEntries.sumOf { it.eveningLitres }
        val currentMonthTotalLitres = monthEntries.sumOf { it.totalLitres }
        val currentMonthPaymentsTotal = monthPayments.sumOf { it.amount }

        val netBalance = previousBalance + currentMonthMilkAmount - currentMonthPaymentsTotal

        return MonthlyBillSummary(
            customer = customer,
            month = monthPrefix,
            totalMorningLitres = Math.round(currentMonthMorningLitres * 100.0) / 100.0,
            totalEveningLitres = Math.round(currentMonthEveningLitres * 100.0) / 100.0,
            totalLitres = Math.round(currentMonthTotalLitres * 100.0) / 100.0,
            currentMonthMilkAmount = Math.round(currentMonthMilkAmount * 100.0) / 100.0,
            previousBalance = Math.round(previousBalance * 100.0) / 100.0,
            totalPaymentsInMonth = Math.round(currentMonthPaymentsTotal * 100.0) / 100.0,
            netBalanceDue = Math.round(netBalance * 100.0) / 100.0,
            entries = monthEntries,
            payments = monthPayments
        )
    }

    // Customer Overall Summary
    suspend fun getCustomerTotalBalance(customerId: Long): Double {
        val allEntries = milkEntryDao.getAllEntriesDirect().filter { it.customerId == customerId }
        val allPayments = paymentDao.getAllPaymentsDirect().filter { it.customerId == customerId }
        val milkTotal = allEntries.sumOf { it.totalAmount }
        val paidTotal = allPayments.sumOf { it.amount }
        return Math.round((milkTotal - paidTotal) * 100.0) / 100.0
    }

    // Backup & Restore
    suspend fun getBackupData(): BackupData {
        val profile = profileDao.getProfile()
        val customers = customerDao.getAllCustomersDirect()
        val entries = milkEntryDao.getAllEntriesDirect()
        val payments = paymentDao.getAllPaymentsDirect()
        return BackupData(
            businessProfile = profile,
            customers = customers,
            milkEntries = entries,
            payments = payments
        )
    }

    suspend fun restoreBackupData(backupData: BackupData) {
        backupData.businessProfile?.let {
            profileDao.insertOrUpdateProfile(it)
        }
        if (backupData.customers.isNotEmpty()) {
            customerDao.insertCustomers(backupData.customers)
        }
        if (backupData.milkEntries.isNotEmpty()) {
            milkEntryDao.insertEntries(backupData.milkEntries)
        }
        if (backupData.payments.isNotEmpty()) {
            paymentDao.insertPayments(backupData.payments)
        }
    }
}
