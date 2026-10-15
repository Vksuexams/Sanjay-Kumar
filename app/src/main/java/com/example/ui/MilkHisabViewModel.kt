package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.MilkEntryEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.repository.MilkRepository
import com.example.data.repository.MonthlyBillSummary
import com.example.ui.localization.AppLanguageStrings
import com.example.ui.localization.getAppStrings
import com.example.util.BackupRestoreHelper
import com.example.util.PdfGenerator
import com.example.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MilkHisabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MilkRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MilkRepository(database)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    // Selected Date for Daily Entries
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Selected Month for Billing
    private val _selectedBillingMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedBillingMonth: StateFlow<String> = _selectedBillingMonth.asStateFlow()

    // Language state
    private val _currentLanguage = MutableStateFlow("hi")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    val strings: StateFlow<AppLanguageStrings> = combine(_currentLanguage) { langArray ->
        getAppStrings(langArray[0])
    }.stateIn(viewModelScope, SharingStarted.Eagerly, getAppStrings("hi"))

    // Business Profile
    val businessProfile: StateFlow<BusinessProfileEntity> = repository.getBusinessProfile()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BusinessProfileEntity(
                dairyName = "Shree Krishna Dairy / श्री कृष्णा डेरी",
                appLanguage = "hi"
            )
        )

    // Customers
    val allCustomers: StateFlow<List<CustomerEntity>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCustomers: StateFlow<List<CustomerEntity>> = repository.getActiveCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Entries & Payments
    val allEntries: StateFlow<List<MilkEntryEntity>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily entries for current selected date
    val entriesForSelectedDate: StateFlow<List<MilkEntryEntity>> = combine(_selectedDate, allEntries) { date, entries ->
        entries.filter { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Customer for Bill / Detail
    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    // Monthly Bill Summary for selected customer and month
    private val _monthlyBillSummary = MutableStateFlow<MonthlyBillSummary?>(null)
    val monthlyBillSummary: StateFlow<MonthlyBillSummary?> = _monthlyBillSummary.asStateFlow()

    // UI Message / Toast Feedback
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun shiftDate(days: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = dateFormat.parse(_selectedDate.value)
            if (parsed != null) cal.time = parsed
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelectedBillingMonth(month: String) {
        _selectedBillingMonth.value = month
        _selectedCustomerId.value?.let { loadMonthlyBill(it, month) }
    }

    fun shiftMonth(months: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = monthFormat.parse(_selectedBillingMonth.value)
            if (parsed != null) cal.time = parsed
            cal.add(Calendar.MONTH, months)
            val newMonth = monthFormat.format(cal.time)
            _selectedBillingMonth.value = newMonth
            _selectedCustomerId.value?.let { loadMonthlyBill(it, newMonth) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleLanguage() {
        val nextLang = if (_currentLanguage.value == "hi") "en" else "hi"
        _currentLanguage.value = nextLang
        viewModelScope.launch {
            val current = businessProfile.value
            repository.saveBusinessProfile(current.copy(appLanguage = nextLang))
        }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            val current = businessProfile.value
            repository.saveBusinessProfile(current.copy(appLanguage = lang))
        }
    }

    fun selectCustomer(customerId: Long?) {
        _selectedCustomerId.value = customerId
        if (customerId != null) {
            loadMonthlyBill(customerId, _selectedBillingMonth.value)
        } else {
            _monthlyBillSummary.value = null
        }
    }

    fun loadMonthlyBill(customerId: Long, month: String = _selectedBillingMonth.value) {
        viewModelScope.launch {
            val summary = repository.calculateMonthlyBill(customerId, month)
            _monthlyBillSummary.value = summary
        }
    }

    // Customer operations
    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _snackbarMessage.value = strings.value.successSaved
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            _snackbarMessage.value = strings.value.successSaved
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            if (_selectedCustomerId.value == customer.id) {
                _selectedCustomerId.value = null
                _monthlyBillSummary.value = null
            }
            _snackbarMessage.value = "Customer deleted / ग्राहक हटा दिया गया"
        }
    }

    // Entry operations
    fun saveMilkEntry(
        customerId: Long,
        date: String,
        morningLitres: Double,
        morningRate: Double,
        eveningLitres: Double,
        eveningRate: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entry = MilkEntryEntity(
                customerId = customerId,
                date = date,
                morningLitres = morningLitres,
                morningRate = morningRate,
                eveningLitres = eveningLitres,
                eveningRate = eveningRate,
                notes = notes
            )
            repository.saveMilkEntry(entry)
            _snackbarMessage.value = strings.value.successSaved
            // Reload bill if current
            if (_selectedCustomerId.value == customerId && date.startsWith(_selectedBillingMonth.value)) {
                loadMonthlyBill(customerId, _selectedBillingMonth.value)
            }
        }
    }

    fun deleteMilkEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteMilkEntry(id)
            _snackbarMessage.value = "Entry deleted / एंट्री हटाई गई"
            _selectedCustomerId.value?.let { loadMonthlyBill(it, _selectedBillingMonth.value) }
        }
    }

    // Payment operations
    fun savePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.savePayment(payment)
            _snackbarMessage.value = strings.value.successSaved
            if (_selectedCustomerId.value == payment.customerId && payment.date.startsWith(_selectedBillingMonth.value)) {
                loadMonthlyBill(payment.customerId, _selectedBillingMonth.value)
            }
        }
    }

    fun deletePayment(id: Long) {
        viewModelScope.launch {
            repository.deletePayment(id)
            _snackbarMessage.value = "Payment deleted / भुगतान हटाया गया"
            _selectedCustomerId.value?.let { loadMonthlyBill(it, _selectedBillingMonth.value) }
        }
    }

    // Business Profile
    fun saveBusinessProfile(profile: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.saveBusinessProfile(profile)
            _snackbarMessage.value = strings.value.successSaved
        }
    }

    // PDF Bill Generation & Share
    fun generateAndSharePdfBill() {
        val summary = _monthlyBillSummary.value
        if (summary == null) {
            _snackbarMessage.value = "No bill data to generate PDF / बिल डेटा उपलब्ध नहीं है"
            return
        }

        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val pdfFile: File = withContext(Dispatchers.IO) {
                    PdfGenerator.generateMonthlyBillPdf(
                        context = context,
                        billSummary = summary,
                        profile = businessProfile.value
                    )
                }
                ShareHelper.sharePdfBill(
                    context = context,
                    pdfFile = pdfFile,
                    summary = summary,
                    dairyName = businessProfile.value.dairyName
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _snackbarMessage.value = "Error generating PDF: ${e.localizedMessage}"
            }
        }
    }

    fun viewPdfBill() {
        val summary = _monthlyBillSummary.value
        if (summary == null) {
            _snackbarMessage.value = "No bill data / बिल डेटा उपलब्ध नहीं है"
            return
        }

        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val pdfFile: File = withContext(Dispatchers.IO) {
                    PdfGenerator.generateMonthlyBillPdf(
                        context = context,
                        billSummary = summary,
                        profile = businessProfile.value
                    )
                }
                ShareHelper.openPdfViewer(context, pdfFile)
            } catch (e: Exception) {
                e.printStackTrace()
                _snackbarMessage.value = "Error viewing PDF: ${e.localizedMessage}"
            }
        }
    }

    // Backup & Restore
    fun exportBackup(onFileCreated: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = repository.getBackupData()
                val context = getApplication<Application>().applicationContext
                val file = withContext(Dispatchers.IO) {
                    BackupRestoreHelper.createBackupFile(context, backupData)
                }
                onFileCreated(file)
                _snackbarMessage.value = strings.value.backupSuccess
            } catch (e: Exception) {
                e.printStackTrace()
                _snackbarMessage.value = "Backup failed: ${e.localizedMessage}"
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val backupData = withContext(Dispatchers.IO) {
                    BackupRestoreHelper.readBackupFromUri(context, uri)
                }
                repository.restoreBackupData(backupData)
                _snackbarMessage.value = strings.value.restoreSuccess
            } catch (e: Exception) {
                e.printStackTrace()
                _snackbarMessage.value = "Restore failed: ${e.localizedMessage}"
            }
        }
    }
}
