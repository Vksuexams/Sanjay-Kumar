package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.MilkEntryEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.repository.BackupData
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreHelper {

    fun exportBackupToJsonString(backupData: BackupData): String {
        val root = JSONObject()
        root.put("app", "Milk Hisab AI")
        root.put("version", backupData.appVersion)
        root.put("timestamp", backupData.exportTimestamp)
        root.put("dateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(backupData.exportTimestamp)))

        // Profile
        backupData.businessProfile?.let { profile ->
            val pObj = JSONObject().apply {
                put("id", profile.id)
                put("dairyName", profile.dairyName)
                put("ownerName", profile.ownerName)
                put("phone", profile.phone)
                put("address", profile.address)
                put("upiId", profile.upiId)
                put("defaultCowRate", profile.defaultCowRate)
                put("defaultBuffaloRate", profile.defaultBuffaloRate)
                put("appLanguage", profile.appLanguage)
                put("invoicePrefix", profile.invoicePrefix)
            }
            root.put("businessProfile", pObj)
        }

        // Customers
        val customersArray = JSONArray()
        backupData.customers.forEach { c ->
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("milkType", c.milkType)
                put("defaultMorningRate", c.defaultMorningRate)
                put("defaultEveningRate", c.defaultEveningRate)
                put("isActive", c.isActive)
                put("createdAt", c.createdAt)
            }
            customersArray.put(cObj)
        }
        root.put("customers", customersArray)

        // Milk Entries
        val entriesArray = JSONArray()
        backupData.milkEntries.forEach { e ->
            val eObj = JSONObject().apply {
                put("id", e.id)
                put("customerId", e.customerId)
                put("date", e.date)
                put("morningLitres", e.morningLitres)
                put("morningRate", e.morningRate)
                put("eveningLitres", e.eveningLitres)
                put("eveningRate", e.eveningRate)
                put("totalLitres", e.totalLitres)
                put("totalAmount", e.totalAmount)
                put("notes", e.notes)
            }
            entriesArray.put(eObj)
        }
        root.put("milkEntries", entriesArray)

        // Payments
        val paymentsArray = JSONArray()
        backupData.payments.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("customerId", p.customerId)
                put("date", p.date)
                put("amount", p.amount)
                put("paymentMode", p.paymentMode)
                put("notes", p.notes)
                put("receiptNo", p.receiptNo)
                put("createdAt", p.createdAt)
            }
            paymentsArray.put(pObj)
        }
        root.put("payments", paymentsArray)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val timestamp = root.optLong("timestamp", System.currentTimeMillis())
        val version = root.optString("version", "1.0")

        var profile: BusinessProfileEntity? = null
        if (root.has("businessProfile") && !root.isNull("businessProfile")) {
            val pObj = root.getJSONObject("businessProfile")
            profile = BusinessProfileEntity(
                id = pObj.optInt("id", 1),
                dairyName = pObj.optString("dairyName", "Shree Krishna Dairy"),
                ownerName = pObj.optString("ownerName", ""),
                phone = pObj.optString("phone", ""),
                address = pObj.optString("address", ""),
                upiId = pObj.optString("upiId", ""),
                defaultCowRate = pObj.optDouble("defaultCowRate", 55.0),
                defaultBuffaloRate = pObj.optDouble("defaultBuffaloRate", 65.0),
                appLanguage = pObj.optString("appLanguage", "hi"),
                invoicePrefix = pObj.optString("invoicePrefix", "MH")
            )
        }

        val customersList = mutableListOf<CustomerEntity>()
        if (root.has("customers")) {
            val cArray = root.getJSONArray("customers")
            for (i in 0 until cArray.length()) {
                val cObj = cArray.getJSONObject(i)
                customersList.add(
                    CustomerEntity(
                        id = cObj.optLong("id", 0),
                        name = cObj.optString("name", "Unknown"),
                        phone = cObj.optString("phone", ""),
                        address = cObj.optString("address", ""),
                        milkType = cObj.optString("milkType", "Cow"),
                        defaultMorningRate = cObj.optDouble("defaultMorningRate", 55.0),
                        defaultEveningRate = cObj.optDouble("defaultEveningRate", 55.0),
                        isActive = cObj.optBoolean("isActive", true),
                        createdAt = cObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val entriesList = mutableListOf<MilkEntryEntity>()
        if (root.has("milkEntries")) {
            val eArray = root.getJSONArray("milkEntries")
            for (i in 0 until eArray.length()) {
                val eObj = eArray.getJSONObject(i)
                entriesList.add(
                    MilkEntryEntity(
                        id = eObj.optLong("id", 0),
                        customerId = eObj.optLong("customerId", 0),
                        date = eObj.optString("date", ""),
                        morningLitres = eObj.optDouble("morningLitres", 0.0),
                        morningRate = eObj.optDouble("morningRate", 55.0),
                        eveningLitres = eObj.optDouble("eveningLitres", 0.0),
                        eveningRate = eObj.optDouble("eveningRate", 55.0),
                        totalLitres = eObj.optDouble("totalLitres", 0.0),
                        totalAmount = eObj.optDouble("totalAmount", 0.0),
                        notes = eObj.optString("notes", "")
                    )
                )
            }
        }

        val paymentsList = mutableListOf<PaymentEntity>()
        if (root.has("payments")) {
            val pArray = root.getJSONArray("payments")
            for (i in 0 until pArray.length()) {
                val pObj = pArray.getJSONObject(i)
                paymentsList.add(
                    PaymentEntity(
                        id = pObj.optLong("id", 0),
                        customerId = pObj.optLong("customerId", 0),
                        date = pObj.optString("date", ""),
                        amount = pObj.optDouble("amount", 0.0),
                        paymentMode = pObj.optString("paymentMode", "Cash"),
                        notes = pObj.optString("notes", ""),
                        receiptNo = pObj.optString("receiptNo", ""),
                        createdAt = pObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(
            exportTimestamp = timestamp,
            appVersion = version,
            businessProfile = profile,
            customers = customersList,
            milkEntries = entriesList,
            payments = paymentsList
        )
    }

    fun createBackupFile(context: Context, backupData: BackupData): File {
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(backupDir, "MilkHisab_Backup_$timeStr.json")
        FileOutputStream(file).use { out ->
            out.write(exportBackupToJsonString(backupData).toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun readBackupFromUri(context: Context, uri: Uri): BackupData {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return parseBackupJson(stringBuilder.toString())
    }
}
