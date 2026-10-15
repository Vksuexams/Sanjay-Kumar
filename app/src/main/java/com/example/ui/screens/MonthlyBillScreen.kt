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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CustomerEntity
import com.example.ui.MilkHisabViewModel
import com.example.ui.theme.DairyBluePrimary
import com.example.ui.theme.DairyGold
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyBillScreen(
    viewModel: MilkHisabViewModel
) {
    val strings by viewModel.strings.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsState()
    val selectedMonth by viewModel.selectedBillingMonth.collectAsState()
    val billSummary by viewModel.monthlyBillSummary.collectAsState()
    val businessProfile by viewModel.businessProfile.collectAsState()

    var customerDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-select first customer if none selected
    LaunchedEffect(customers, selectedCustomerId) {
        if (selectedCustomerId == null && customers.isNotEmpty()) {
            viewModel.selectCustomer(customers.first().id)
        }
    }

    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    val formattedMonthDisplay = remember(selectedMonth) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(parsed ?: Date())
        } catch (e: Exception) {
            selectedMonth
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Customer & Month Selector Cards
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { customerDropdownExpanded = true }
                    .testTag("bill_customer_picker"),
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
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DairyBluePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = strings.customerName, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = selectedCustomer?.name ?: "Select Customer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                                viewModel.selectCustomer(c.id)
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Month Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    IconButton(onClick = { viewModel.shiftMonth(-1) }, modifier = Modifier.testTag("bill_prev_month")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = DairyBluePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = formattedMonthDisplay, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { viewModel.shiftMonth(1) }, modifier = Modifier.testTag("bill_next_month")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        if (billSummary == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Select a customer to view their monthly bill.")
                    }
                }
            }
        } else {
            val summary = billSummary!!

            // Big Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_summary_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${summary.customer.name} - ${summary.month}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Invoice: ${businessProfile.invoicePrefix}-${summary.month.replace("-", "")}-${summary.customer.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = DairyBluePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Summary grid rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BillMetric(label = strings.morningMilk, value = "${summary.totalMorningLitres} L")
                            BillMetric(label = strings.eveningMilk, value = "${summary.totalEveningLitres} L")
                            BillMetric(label = strings.totalLitres, value = "${summary.totalLitres} L", isBold = true)
                        }

                        // Financial Ledger Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LedgerRow(label = "${strings.month} Milk Amount / दूध राशि:", value = "₹${summary.currentMonthMilkAmount}")
                                if (summary.previousBalance != 0.0) {
                                    LedgerRow(label = "${strings.previousBalance} / पिछला बकाया:", value = "₹${summary.previousBalance}")
                                }
                                if (summary.totalPaymentsInMonth > 0) {
                                    LedgerRow(label = "${strings.paymentsReceived} / जमा राशि:", value = "- ₹${summary.totalPaymentsInMonth}", color = MilkGreen)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                )
                                LedgerRow(
                                    label = "${strings.netBalanceDue} / कुल बकाया:",
                                    value = "₹${summary.netBalanceDue}",
                                    isBold = true,
                                    color = if (summary.netBalanceDue > 0) MilkRed else MilkGreen
                                )
                            }
                        }

                        // Share & View Buttons (Requirement 9, 10, 11, 12)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Native Share Button (WhatsApp / Chooser)
                            Button(
                                onClick = { viewModel.generateAndSharePdfBill() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("share_pdf_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share PDF / शेयर",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // View PDF Button
                            OutlinedButton(
                                onClick = { viewModel.viewPdfBill() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("view_pdf_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View PDF / देखें",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Date-wise entries table header
            item {
                Text(
                    text = "Date-wise Breakdown / तारीख अनुसार विवरण (${summary.entries.size} days)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (summary.entries.isEmpty()) {
                item {
                    Text(
                        text = "No milk entries recorded in $formattedMonthDisplay.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Table Columns Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                            Text("Morn L", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("Eve L", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("Total L", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("Amount", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        }
                    }
                }

                items(summary.entries.sortedBy { it.date }, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(entry.date.takeLast(5), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                            Text(if (entry.morningLitres > 0) "${entry.morningLitres}" else "-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(if (entry.eveningLitres > 0) "${entry.eveningLitres}" else "-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("${entry.totalLitres} L", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                            Text("₹${entry.totalAmount}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
                        }
                    }
                }
            }

            // Month's Payments
            if (summary.payments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Payments Received This Month (${summary.payments.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(summary.payments, key = { it.id }) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${p.date} • ${p.paymentMode}")
                            Text("₹${p.amount}", fontWeight = FontWeight.Bold, color = MilkGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillMetric(label: String, value: String, isBold: Boolean = false) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium))
    }
}

@Composable
private fun LedgerRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, color = color))
    }
}
