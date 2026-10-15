package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.MilkEntryEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.repository.BackupData
import com.example.util.BackupRestoreHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Milk Hisab AI", appName)
    }

    @Test
    fun `test milk entry amount calculation`() {
        val morningLitres = 2.5
        val morningRate = 55.0
        val eveningLitres = 1.5
        val eveningRate = 55.0

        val totalLitres = morningLitres + eveningLitres
        val totalAmount = (morningLitres * morningRate) + (eveningLitres * eveningRate)

        assertEquals(4.0, totalLitres, 0.001)
        assertEquals(220.0, totalAmount, 0.001)
    }

    @Test
    fun `test net balance calculation`() {
        val previousBalance = 500.0
        val currentMonthMilk = 2200.0
        val paymentReceived = 1500.0

        val netBalanceDue = previousBalance + currentMonthMilk - paymentReceived
        assertEquals(1200.0, netBalanceDue, 0.001)
    }

    @Test
    fun `test backup json serialization and deserialization`() {
        val customer = CustomerEntity(
            id = 101L,
            name = "Ramesh Kumar",
            phone = "9876543210",
            milkType = "Cow",
            defaultMorningRate = 55.0,
            defaultEveningRate = 55.0
        )
        val entry = MilkEntryEntity(
            id = 1L,
            customerId = 101L,
            date = "2026-10-15",
            morningLitres = 2.0,
            morningRate = 55.0,
            eveningLitres = 2.0,
            eveningRate = 55.0,
            totalLitres = 4.0,
            totalAmount = 220.0
        )
        val payment = PaymentEntity(
            id = 1L,
            customerId = 101L,
            date = "2026-10-15",
            amount = 200.0,
            paymentMode = "UPI"
        )
        val profile = BusinessProfileEntity(
            id = 1,
            dairyName = "Shree Krishna Dairy"
        )

        val backupData = BackupData(
            businessProfile = profile,
            customers = listOf(customer),
            milkEntries = listOf(entry),
            payments = listOf(payment)
        )

        val jsonString = BackupRestoreHelper.exportBackupToJsonString(backupData)
        assertNotNull(jsonString)

        val restored = BackupRestoreHelper.parseBackupJson(jsonString)
        assertEquals(1, restored.customers.size)
        assertEquals("Ramesh Kumar", restored.customers[0].name)
        assertEquals(1, restored.milkEntries.size)
        assertEquals(220.0, restored.milkEntries[0].totalAmount, 0.001)
        assertEquals(1, restored.payments.size)
        assertEquals(200.0, restored.payments[0].amount, 0.001)
    }

    @Test
    fun `test monthly analytics aggregation logic`() {
        val entries = listOf(
            MilkEntryEntity(id = 1L, customerId = 1L, date = "2026-10-01", morningLitres = 5.0, morningRate = 50.0, eveningLitres = 5.0, eveningRate = 50.0, totalLitres = 10.0, totalAmount = 500.0),
            MilkEntryEntity(id = 2L, customerId = 1L, date = "2026-10-02", morningLitres = 6.0, morningRate = 50.0, eveningLitres = 4.0, eveningRate = 50.0, totalLitres = 10.0, totalAmount = 500.0),
            MilkEntryEntity(id = 3L, customerId = 2L, date = "2026-10-02", morningLitres = 4.0, morningRate = 60.0, eveningLitres = 4.0, eveningRate = 60.0, totalLitres = 8.0, totalAmount = 480.0)
        )

        val totalLitres = entries.sumOf { it.totalLitres }
        val totalBilled = entries.sumOf { it.totalAmount }
        val day2Litres = entries.filter { it.date == "2026-10-02" }.sumOf { it.totalLitres }

        assertEquals(28.0, totalLitres, 0.001)
        assertEquals(1480.0, totalBilled, 0.001)
        assertEquals(18.0, day2Litres, 0.001)
    }
}
