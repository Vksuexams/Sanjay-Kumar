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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun CustomersScreen(
    viewModel: MilkHisabViewModel,
    onNavigateToBills: () -> Unit
) {
    val strings by viewModel.strings.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var viewingCustomerDetail by remember { mutableStateOf<CustomerEntity?>(null) }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("customer_search_input"),
                placeholder = { Text(strings.searchCustomer) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No customers match your search." else strings.noCustomersYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        val customerEntries = allEntries.filter { it.customerId == customer.id }
                        val customerPayments = allPayments.filter { it.customerId == customer.id }
                        val totalMilkAmt = customerEntries.sumOf { it.totalAmount }
                        val totalPaidAmt = customerPayments.sumOf { it.amount }
                        val netBalance = Math.round((totalMilkAmt - totalPaidAmt) * 100.0) / 100.0

                        CustomerCard(
                            customer = customer,
                            balanceDue = netBalance,
                            onCardClick = { viewingCustomerDetail = customer },
                            onEditClick = { editingCustomer = customer },
                            onDeleteClick = { customerToDelete = customer },
                            onBillClick = {
                                viewModel.selectCustomer(customer.id)
                                onNavigateToBills()
                            }
                        )
                    }
                }
            }
        }

        // Add Customer FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_customer_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
        }
    }

    // Add / Edit Customer Dialog
    if (showAddDialog || editingCustomer != null) {
        val target = editingCustomer
        CustomerFormDialog(
            customer = target,
            onDismiss = {
                showAddDialog = false
                editingCustomer = null
            },
            onSave = { savedCustomer ->
                if (target == null) {
                    viewModel.saveCustomer(savedCustomer)
                } else {
                    viewModel.updateCustomer(savedCustomer)
                }
                showAddDialog = false
                editingCustomer = null
            }
        )
    }

    // Delete Confirmation
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(text = strings.confirmDelete) },
            text = { Text("Are you sure you want to delete ${customerToDelete?.name}? All their milk entries and payments will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        customerToDelete?.let { viewModel.deleteCustomer(it) }
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Customer Detailed Ledger Sheet Dialog
    if (viewingCustomerDetail != null) {
        val cust = viewingCustomerDetail!!
        val customerEntries = allEntries.filter { it.customerId == cust.id }.sortedByDescending { it.date }
        val customerPayments = allPayments.filter { it.customerId == cust.id }.sortedByDescending { it.date }
        val totalMilkAmt = customerEntries.sumOf { it.totalAmount }
        val totalPaidAmt = customerPayments.sumOf { it.amount }
        val netBalance = Math.round((totalMilkAmt - totalPaidAmt) * 100.0) / 100.0

        CustomerDetailDialog(
            customer = cust,
            totalMilkAmount = totalMilkAmt,
            totalPaidAmount = totalPaidAmt,
            balanceDue = netBalance,
            entries = customerEntries,
            payments = customerPayments,
            onDismiss = { viewingCustomerDetail = null },
            onGenerateBill = {
                viewingCustomerDetail = null
                viewModel.selectCustomer(cust.id)
                onNavigateToBills()
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    balanceDue: Double,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBillClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("customer_item_${customer.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DairyBluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DairyBluePrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (customer.phone.isNotBlank()) {
                                Text(
                                    text = customer.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(" • ", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "${customer.milkType} Milk",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = DairyBluePrimary)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance: ₹$balanceDue",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (balanceDue > 0) Color(0xFFC62828) else MilkGreen
                        )
                    )
                    Text(
                        text = "Rate: ₹${customer.defaultMorningRate}/L",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBillClick) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bill (बिल)")
                }
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerFormDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var milkType by remember { mutableStateOf(customer?.milkType ?: "Cow") }
    var morningRate by remember { mutableStateOf(customer?.defaultMorningRate?.toString() ?: "55.0") }
    var eveningRate by remember { mutableStateOf(customer?.defaultEveningRate?.toString() ?: "55.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (customer == null) "Add Customer / नया ग्राहक" else "Edit Customer / ग्राहक संपादित करें")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (नाम) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_customer_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (मोबाइल नंबर)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Village (पता)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Milk Type Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = milkType == "Cow",
                        onClick = { milkType = "Cow" },
                        label = { Text("Cow (गाय)") }
                    )
                    FilterChip(
                        selected = milkType == "Buffalo",
                        onClick = { milkType = "Buffalo" },
                        label = { Text("Buffalo (भैंस)") }
                    )
                    FilterChip(
                        selected = milkType == "Mixed",
                        onClick = { milkType = "Mixed" },
                        label = { Text("Mixed (मिश्रित)") }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = morningRate,
                        onValueChange = { morningRate = it },
                        label = { Text("Morn Rate (₹)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = eveningRate,
                        onValueChange = { eveningRate = it },
                        label = { Text("Eve Rate (₹)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val mRate = morningRate.toDoubleOrNull() ?: 55.0
                        val eRate = eveningRate.toDoubleOrNull() ?: 55.0
                        val entity = customer?.copy(
                            name = name,
                            phone = phone,
                            address = address,
                            milkType = milkType,
                            defaultMorningRate = mRate,
                            defaultEveningRate = eRate
                        ) ?: CustomerEntity(
                            name = name,
                            phone = phone,
                            address = address,
                            milkType = milkType,
                            defaultMorningRate = mRate,
                            defaultEveningRate = eRate
                        )
                        onSave(entity)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("dialog_customer_save_btn")
            ) {
                Text("Save / सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / रद्द करें")
            }
        }
    )
}

@Composable
private fun CustomerDetailDialog(
    customer: CustomerEntity,
    totalMilkAmount: Double,
    totalPaidAmount: Double,
    balanceDue: Double,
    entries: List<com.example.data.local.entity.MilkEntryEntity>,
    payments: List<com.example.data.local.entity.PaymentEntity>,
    onDismiss: () -> Unit,
    onGenerateBill: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = customer.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                if (customer.phone.isNotBlank()) {
                    Text(text = "Mob: ${customer.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Milk / कुल दूध:")
                            Text("₹${Math.round(totalMilkAmount * 100.0) / 100.0}", fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid / कुल जमा:")
                            Text("₹${Math.round(totalPaidAmount * 100.0) / 100.0}", fontWeight = FontWeight.SemiBold, color = MilkGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance Due / कुल बकाया:", fontWeight = FontWeight.Bold)
                            Text("₹$balanceDue", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        }
                    }
                }

                Text(
                    text = "Recent Activity (${entries.size} entries, ${payments.size} payments)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )

                // Quick preview of last 3 entries
                entries.take(3).forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(entry.date, style = MaterialTheme.typography.bodySmall)
                        Text("${entry.totalLitres} L", style = MaterialTheme.typography.bodySmall)
                        Text("₹${entry.totalAmount}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onGenerateBill) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate Monthly Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
