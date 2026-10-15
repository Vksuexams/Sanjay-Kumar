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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.entity.PaymentEntity
import com.example.ui.MilkHisabViewModel
import com.example.ui.theme.DairyBluePrimary
import com.example.ui.theme.MilkGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsScreen(
    viewModel: MilkHisabViewModel
) {
    val strings by viewModel.strings.collectAsState()
    val customers by viewModel.activeCustomers.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(customers.firstOrNull()) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var paymentDate by remember { mutableStateOf(todayDateStr) }
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var notesText by remember { mutableStateOf("") }

    var paymentToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Record Payment Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("record_payment_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.recordPayment,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Customer Selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { customerDropdownExpanded = true }
                            .testTag("payment_customer_selector"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = strings.customerName, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = selectedCustomer?.name ?: "Select Customer",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedCustomer = c
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date & Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = paymentDate,
                            onValueChange = { paymentDate = it },
                            label = { Text(strings.date) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text(strings.paymentAmount) },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("payment_amount_input")
                        )
                    }

                    // Payment Mode Chips
                    Text(text = strings.paymentMode, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = paymentMode == "Cash",
                            onClick = { paymentMode = "Cash" },
                            label = { Text(strings.cash) }
                        )
                        FilterChip(
                            selected = paymentMode == "UPI",
                            onClick = { paymentMode = "UPI" },
                            label = { Text(strings.upi) }
                        )
                        FilterChip(
                            selected = paymentMode == "Bank",
                            onClick = { paymentMode = "Bank" },
                            label = { Text(strings.bank) }
                        )
                    }

                    // Notes
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(strings.notes) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Save Button
                    Button(
                        onClick = {
                            val cust = selectedCustomer ?: return@Button
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.savePayment(
                                    PaymentEntity(
                                        customerId = cust.id,
                                        date = paymentDate,
                                        amount = Math.round(amt * 100.0) / 100.0,
                                        paymentMode = paymentMode,
                                        notes = notesText
                                    )
                                )
                                amountText = ""
                                notesText = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_payment_button"),
                        enabled = selectedCustomer != null && (amountText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.save)
                    }
                }
            }
        }

        // Recent Payments Header
        item {
            Text(
                text = "${strings.payments} ${strings.history} (${allPayments.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Payments History
        if (allPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No payment records yet. / अभी कोई भुगतान दर्ज नहीं है।")
                    }
                }
            }
        } else {
            items(allPayments, key = { it.id }) { payment ->
                val customer = customers.find { it.id == payment.customerId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_item_${payment.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MilkGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MilkGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = customer?.name ?: "Customer #${payment.customerId}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${payment.date} • ${payment.paymentMode}" + (if (payment.notes.isNotBlank()) " • ${payment.notes}" else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${payment.amount}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MilkGreen
                                )
                            )
                            IconButton(
                                onClick = { paymentToDelete = payment },
                                modifier = Modifier.size(36.dp)
                            ) {
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
        }
    }

    if (paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text(text = strings.confirmDelete) },
            text = { Text("Delete payment of ₹${paymentToDelete?.amount}?") },
            confirmButton = {
                Button(
                    onClick = {
                        paymentToDelete?.id?.let { viewModel.deletePayment(it) }
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
