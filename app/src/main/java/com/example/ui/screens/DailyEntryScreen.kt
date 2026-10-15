package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CustomerEntity
import com.example.ui.MilkHisabViewModel
import com.example.ui.theme.DairyBluePrimary
import com.example.ui.theme.DairyGold
import com.example.ui.theme.MilkGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyEntryScreen(
    viewModel: MilkHisabViewModel,
    onNavigateBack: () -> Unit
) {
    val strings by viewModel.strings.collectAsState()
    val customers by viewModel.activeCustomers.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayEntries by viewModel.entriesForSelectedDate.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Single Entry Form, 1: Quick Roster List

    // Single Form States
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var morningLitresText by remember { mutableStateOf("") }
    var morningRateText by remember { mutableStateOf("") }
    var eveningLitresText by remember { mutableStateOf("") }
    var eveningRateText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    // When customer or date changes, check if existing entry exists
    LaunchedEffect(selectedCustomer, selectedDate, todayEntries) {
        val cust = selectedCustomer
        if (cust != null) {
            val existing = todayEntries.find { it.customerId == cust.id }
            if (existing != null) {
                morningLitresText = if (existing.morningLitres > 0) existing.morningLitres.toString() else ""
                morningRateText = existing.morningRate.toString()
                eveningLitresText = if (existing.eveningLitres > 0) existing.eveningLitres.toString() else ""
                eveningRateText = existing.eveningRate.toString()
                notesText = existing.notes
            } else {
                morningLitresText = ""
                morningRateText = cust.defaultMorningRate.toString()
                eveningLitresText = ""
                eveningRateText = cust.defaultEveningRate.toString()
                notesText = ""
            }
        } else if (customers.isNotEmpty()) {
            selectedCustomer = customers.first()
        }
    }

    val mLitres = morningLitresText.toDoubleOrNull() ?: 0.0
    val mRate = morningRateText.toDoubleOrNull() ?: (selectedCustomer?.defaultMorningRate ?: 55.0)
    val eLitres = eveningLitresText.toDoubleOrNull() ?: 0.0
    val eRate = eveningRateText.toDoubleOrNull() ?: (selectedCustomer?.defaultEveningRate ?: 55.0)

    val previewTotalLitres = Math.round((mLitres + eLitres) * 100.0) / 100.0
    val previewTotalAmount = Math.round(((mLitres * mRate) + (eLitres * eRate)) * 100.0) / 100.0

    val formattedDateDisplay = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(parsed ?: Date())
        } catch (e: Exception) {
            selectedDate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Date selector strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.shiftDate(-1) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Date")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = DairyBluePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = formattedDateDisplay, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { viewModel.shiftDate(1) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Date")
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text(strings.quickAddEntry, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_single_entry")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Daily Register / रजिस्टर", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_roster_entry")
            )
        }

        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.noCustomersYet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (selectedTabIndex == 0) {
            // Single Customer Entry Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Selector Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { customerDropdownExpanded = true }
                            .testTag("customer_dropdown_selector"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DairyBluePrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.customerName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedCustomer?.name ?: "Select Customer",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (selectedCustomer != null) {
                                        Text(
                                            text = "${selectedCustomer?.milkType} Milk | Def: ₹${selectedCustomer?.defaultMorningRate}/L",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(cust.name, fontWeight = FontWeight.SemiBold)
                                            Text("${cust.milkType} • ${cust.phone}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedCustomer = cust
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Morning Shift Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌅 ${strings.morningMilk}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = morningLitresText,
                                    onValueChange = { morningLitresText = it },
                                    label = { Text(strings.morningLitres) },
                                    placeholder = { Text("0.0") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("morning_litres_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = morningRateText,
                                    onValueChange = { morningRateText = it },
                                    label = { Text(strings.morningRate) },
                                    placeholder = { Text("55.0") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("morning_rate_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Evening Shift Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌇 ${strings.eveningMilk}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = eveningLitresText,
                                    onValueChange = { eveningLitresText = it },
                                    label = { Text(strings.eveningLitres) },
                                    placeholder = { Text("0.0") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("evening_litres_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = eveningRateText,
                                    onValueChange = { eveningRateText = it },
                                    label = { Text(strings.eveningRate) },
                                    placeholder = { Text("55.0") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("evening_rate_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Summary Live Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.totalLitres, style = MaterialTheme.typography.bodySmall)
                                Text("$previewTotalLitres L", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DairyBluePrimary))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.totalAmount, style = MaterialTheme.typography.bodySmall)
                                Text("₹$previewTotalAmount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MilkGreen))
                            }
                        }
                    }
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(strings.notes) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input"),
                        singleLine = true
                    )
                }

                // Save Button
                item {
                    Button(
                        onClick = {
                            val cust = selectedCustomer ?: return@Button
                            viewModel.saveMilkEntry(
                                customerId = cust.id,
                                date = selectedDate,
                                morningLitres = mLitres,
                                morningRate = mRate,
                                eveningLitres = eLitres,
                                eveningRate = eRate,
                                notes = notesText
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_milk_entry_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedCustomer != null && (mLitres > 0 || eLitres > 0)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.save} (${strings.dailyEntry})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            // Rapid Roster Register Mode
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Quick Roster Entry for $formattedDateDisplay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(customers, key = { it.id }) { cust ->
                    val existing = todayEntries.find { it.customerId == cust.id }
                    RosterItemCard(
                        customer = cust,
                        existingEntry = existing,
                        onSave = { mornL, eveL ->
                            viewModel.saveMilkEntry(
                                customerId = cust.id,
                                date = selectedDate,
                                morningLitres = mornL,
                                morningRate = cust.defaultMorningRate,
                                eveningLitres = eveL,
                                eveningRate = cust.defaultEveningRate
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterItemCard(
    customer: CustomerEntity,
    existingEntry: com.example.data.local.entity.MilkEntryEntity?,
    onSave: (Double, Double) -> Unit
) {
    var morningText by remember(existingEntry) {
        mutableStateOf(if (existingEntry?.morningLitres ?: 0.0 > 0) existingEntry?.morningLitres.toString() else "")
    }
    var eveningText by remember(existingEntry) {
        mutableStateOf(if (existingEntry?.eveningLitres ?: 0.0 > 0) existingEntry?.eveningLitres.toString() else "")
    }

    val isSaved = existingEntry != null && (existingEntry.morningLitres > 0 || existingEntry.eveningLitres > 0)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSaved) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "${customer.milkType} • ₹${customer.defaultMorningRate}/L", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isSaved) {
                    Text(
                        text = "Saved: ₹${existingEntry.totalAmount}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MilkGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = morningText,
                    onValueChange = { morningText = it },
                    label = { Text("Morn L") },
                    placeholder = { Text("0") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = eveningText,
                    onValueChange = { eveningText = it },
                    label = { Text("Eve L") },
                    placeholder = { Text("0") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        val mVal = morningText.toDoubleOrNull() ?: 0.0
                        val eVal = eveningText.toDoubleOrNull() ?: 0.0
                        onSave(mVal, eVal)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(DairyBluePrimary, RoundedCornerShape(8.dp))
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                }
            }
        }
    }
}
