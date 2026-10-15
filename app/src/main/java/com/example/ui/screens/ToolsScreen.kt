package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.MilkHisabViewModel
import com.example.ui.theme.DairyBluePrimary
import com.example.ui.theme.DairyGold
import com.example.ui.theme.MilkGreen
import java.io.File

@Composable
fun ToolsScreen(
    viewModel: MilkHisabViewModel
) {
    val context = LocalContext.current
    val strings by viewModel.strings.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    // Business Profile Fields
    var dairyName by remember(profile) { mutableStateOf(profile.dairyName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var upiId by remember(profile) { mutableStateOf(profile.upiId) }

    // Fat & SNF Calculator States
    var fatInput by remember { mutableStateOf("6.5") }
    var snfInput by remember { mutableStateOf("8.5") }
    var fatRatePerPoint by remember { mutableStateOf("8.5") }

    val fatVal = fatInput.toDoubleOrNull() ?: 0.0
    val snfVal = snfInput.toDoubleOrNull() ?: 0.0
    val ratePointVal = fatRatePerPoint.toDoubleOrNull() ?: 8.5
    // Typical Indian dairy formula: (Fat * RatePerFat) + optional SNF incentive
    val calculatedRatePerLitre = Math.round((fatVal * ratePointVal) * 100.0) / 100.0

    // SAF File Pickers for Backup & Restore
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreBackup(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Language Toggle Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_selector_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = DairyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.language,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = currentLang == "hi",
                            onClick = { viewModel.setLanguage("hi") },
                            label = { Text("हिन्दी (Hindi)") },
                            modifier = Modifier.testTag("lang_hi_chip")
                        )
                        FilterChip(
                            selected = currentLang == "en",
                            onClick = { viewModel.setLanguage("en") },
                            label = { Text("English") },
                            modifier = Modifier.testTag("lang_en_chip")
                        )
                    }
                }
            }
        }

        // Backup and Restore Card (Requirement 13)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_restore_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = DairyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.backupRestore,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "Your data stays 100% offline on this device. Save a backup copy to your phone or share it to WhatsApp/Google Drive to keep it secure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackup { backupFile ->
                                    shareBackupFile(context, backupFile)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DairyBluePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Backup")
                        }

                        OutlinedButton(
                            onClick = {
                                restoreLauncher.launch("application/json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_backup_button")
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore")
                        }
                    }
                }
            }
        }

        // Fat & SNF Milk Rate Calculator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fat_snf_calculator_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = DairyGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.fatSnfCalculator,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = fatInput,
                            onValueChange = { fatInput = it },
                            label = { Text("Fat %") },
                            placeholder = { Text("6.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = snfInput,
                            onValueChange = { snfInput = it },
                            label = { Text("SNF %") },
                            placeholder = { Text("8.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = fatRatePerPoint,
                        onValueChange = { fatRatePerPoint = it },
                        label = { Text("Rate per 1.0 Fat (₹/Point)") },
                        placeholder = { Text("8.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Output Result Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.calculatedRate,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "₹$calculatedRatePerLitre / Litre",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // Dairy Profile Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dairy_profile_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = DairyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dairy Profile (बिल पर छपने वाली जानकारी)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedTextField(
                        value = dairyName,
                        onValueChange = { dairyName = it },
                        label = { Text(strings.dairyName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(strings.ownerName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(strings.phone) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(strings.address) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID (e.g. dairy@okhdfcbank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveBusinessProfile(
                                profile.copy(
                                    dairyName = dairyName,
                                    ownerName = ownerName,
                                    phone = phone,
                                    address = address,
                                    upiId = upiId
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Profile / सुरक्षित करें")
                    }
                }
            }
        }
    }
}

private fun shareBackupFile(context: Context, backupFile: File) {
    try {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, backupFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Milk Hisab AI Database Backup")
            putExtra(Intent.EXTRA_TEXT, "Here is your Milk Hisab AI offline database backup file.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Save or Share Backup File (बैकअप शेयर करें)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error sharing backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
