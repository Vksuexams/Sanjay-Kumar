package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.MilkEntryEntity
import com.example.data.local.entity.PaymentEntity
import com.example.ui.MilkHisabViewModel
import com.example.ui.theme.DairyBluePrimary
import com.example.ui.theme.DairyBlueSecondary
import com.example.ui.theme.DairyGold
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyAggregation(
    val dayNumber: Int,
    val dateStr: String,
    val morningLitres: Double,
    val eveningLitres: Double,
    val totalLitres: Double,
    val totalAmount: Double
)

data class WeeklyCashflow(
    val label: String,
    val billedAmount: Double,
    val collectedAmount: Double
)

@Composable
fun MonthlyAnalyticsScreen(
    viewModel: MilkHisabViewModel
) {
    val strings by viewModel.strings.collectAsState()
    val selectedMonth by viewModel.selectedBillingMonth.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()

    // Filter data for the selected billing month (e.g. "2026-10")
    val monthEntries = remember(allEntries, selectedMonth) {
        allEntries.filter { it.date.startsWith(selectedMonth) }
    }
    val monthPayments = remember(allPayments, selectedMonth) {
        allPayments.filter { it.date.startsWith(selectedMonth) }
    }

    // Days in this month
    val daysInMonth = remember(selectedMonth) {
        try {
            val parts = selectedMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            30
        }
    }

    // Aggregate daily consumption trends
    val dailyData = remember(monthEntries, selectedMonth, daysInMonth) {
        (1..daysInMonth).map { day ->
            val dayStr = String.format(Locale.US, "%s-%02d", selectedMonth, day)
            val dayEntries = monthEntries.filter { it.date == dayStr }
            val morn = dayEntries.sumOf { it.morningLitres }
            val eve = dayEntries.sumOf { it.eveningLitres }
            val totalL = morn + eve
            val totalAmt = dayEntries.sumOf { it.totalAmount }
            DailyAggregation(
                dayNumber = day,
                dateStr = dayStr,
                morningLitres = Math.round(morn * 100.0) / 100.0,
                eveningLitres = Math.round(eve * 100.0) / 100.0,
                totalLitres = Math.round(totalL * 100.0) / 100.0,
                totalAmount = Math.round(totalAmt * 100.0) / 100.0
            )
        }
    }

    // Weekly Cashflow Blocks (Billing vs Collection)
    val weeklyCashflow = remember(monthEntries, monthPayments, selectedMonth) {
        listOf(
            WeeklyCashflow(
                label = "W1 (1-7)",
                billedAmount = monthEntries.filter { getDayFromDate(it.date) in 1..7 }.sumOf { it.totalAmount },
                collectedAmount = monthPayments.filter { getDayFromDate(it.date) in 1..7 }.sumOf { it.amount }
            ),
            WeeklyCashflow(
                label = "W2 (8-14)",
                billedAmount = monthEntries.filter { getDayFromDate(it.date) in 8..14 }.sumOf { it.totalAmount },
                collectedAmount = monthPayments.filter { getDayFromDate(it.date) in 8..14 }.sumOf { it.amount }
            ),
            WeeklyCashflow(
                label = "W3 (15-21)",
                billedAmount = monthEntries.filter { getDayFromDate(it.date) in 15..21 }.sumOf { it.totalAmount },
                collectedAmount = monthPayments.filter { getDayFromDate(it.date) in 15..21 }.sumOf { it.amount }
            ),
            WeeklyCashflow(
                label = "W4 (22+)",
                billedAmount = monthEntries.filter { getDayFromDate(it.date) >= 22 }.sumOf { it.totalAmount },
                collectedAmount = monthPayments.filter { getDayFromDate(it.date) >= 22 }.sumOf { it.amount }
            )
        )
    }

    // Milk Type Distribution (Cow vs Buffalo vs Mixed)
    val milkTypeVolume = remember(monthEntries, allCustomers) {
        val cowL = monthEntries.filter { entry ->
            val c = allCustomers.find { it.id == entry.customerId }
            c?.milkType == "Cow"
        }.sumOf { it.totalLitres }

        val bufL = monthEntries.filter { entry ->
            val c = allCustomers.find { it.id == entry.customerId }
            c?.milkType == "Buffalo"
        }.sumOf { it.totalLitres }

        val mixL = monthEntries.filter { entry ->
            val c = allCustomers.find { it.id == entry.customerId }
            c?.milkType != "Cow" && c?.milkType != "Buffalo"
        }.sumOf { it.totalLitres }

        Triple(
            Math.round(cowL * 10.0) / 10.0,
            Math.round(bufL * 10.0) / 10.0,
            Math.round(mixL * 10.0) / 10.0
        )
    }

    // Top Customers by Volume
    val topCustomers = remember(monthEntries, allCustomers) {
        monthEntries.groupBy { it.customerId }.mapNotNull { (custId, entries) ->
            val cust = allCustomers.find { it.id == custId } ?: return@mapNotNull null
            val litres = entries.sumOf { it.totalLitres }
            val amount = entries.sumOf { it.totalAmount }
            Triple(cust, Math.round(litres * 100.0) / 100.0, Math.round(amount * 100.0) / 100.0)
        }.sortedByDescending { it.second }.take(5)
    }

    // High level KPIs
    val totalVolume = Math.round(dailyData.sumOf { it.totalLitres } * 100.0) / 100.0
    val totalBilled = Math.round(dailyData.sumOf { it.totalAmount } * 100.0) / 100.0
    val totalCollected = Math.round(monthPayments.sumOf { it.amount } * 100.0) / 100.0
    val dailyAvg = if (daysInMonth > 0) Math.round((totalVolume / daysInMonth) * 10.0) / 10.0 else 0.0
    val recoveryRate = if (totalBilled > 0) Math.min(100.0, Math.round((totalCollected / totalBilled) * 1000.0) / 10.0) else 0.0

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_month_selector"),
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
                    IconButton(
                        onClick = { viewModel.shiftMonth(-1) },
                        modifier = Modifier.testTag("analytics_prev_month")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = DairyBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedMonthDisplay,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.shiftMonth(1) },
                        modifier = Modifier.testTag("analytics_next_month")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Summary KPI Metrics Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = strings.totalLitres,
                        value = "$totalVolume L",
                        subtitle = "Avg: $dailyAvg L/day",
                        accentColor = DairyBluePrimary,
                        icon = Icons.Default.LocalDrink
                    )
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Billed Total",
                        value = "₹$totalBilled",
                        subtitle = "${monthEntries.size} entries",
                        accentColor = DairyGold,
                        icon = Icons.Default.TrendingUp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Collected (जमा)",
                        value = "₹$totalCollected",
                        subtitle = "${monthPayments.size} receipts",
                        accentColor = MilkGreen,
                        icon = Icons.Default.Payments
                    )
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = strings.collectionRate,
                        value = "$recoveryRate%",
                        subtitle = if (totalBilled > totalCollected) "Due: ₹${Math.round((totalBilled - totalCollected)*10.0)/10.0}" else "Settled",
                        accentColor = if (recoveryRate >= 75) MilkGreen else MilkRed,
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }

        // Visual Chart 1: Daily Milk Consumption Trend (D3 Linear Scale & Smooth Area Curve)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consumption_trend_chart_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.consumptionTrends,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Daily volume curve across $formattedMonthDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = DairyBluePrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    D3MilkTrendChart(
                        dailyData = dailyData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // Visual Chart 2: Cashflow & Payment Summary (Recharts-style Grouped Bar Chart)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cashflow_bar_chart_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.paymentSummaries,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = strings.cashflowOverview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = MilkGreen)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(DairyBluePrimary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Milk Bill (₹)", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(20.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(MilkGreen, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Payments Collected (₹)", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    RechartsCashflowBarChart(
                        weeklyCashflow = weeklyCashflow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )
                }
            }
        }

        // Visual Chart 3: Milk Type Distribution (Donut Pie Chart)
        item {
            val totalMilkTypeVol = milkTypeVolume.first + milkTypeVolume.second + milkTypeVolume.third
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("milk_type_donut_chart_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.milkDistribution,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Cow vs Buffalo vs Mixed volume share",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = DairyGold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Donut Canvas
                        RechartsDonutChart(
                            cowLitres = milkTypeVolume.first,
                            buffaloLitres = milkTypeVolume.second,
                            mixedLitres = milkTypeVolume.third,
                            modifier = Modifier.size(130.dp)
                        )

                        // Legend & percentages
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DonutLegendItem(
                                label = "Cow Milk (गाय)",
                                litres = milkTypeVolume.first,
                                total = totalMilkTypeVol,
                                color = DairyBluePrimary
                            )
                            DonutLegendItem(
                                label = "Buffalo Milk (भैंस)",
                                litres = milkTypeVolume.second,
                                total = totalMilkTypeVol,
                                color = Color(0xFF00897B)
                            )
                            DonutLegendItem(
                                label = "Mixed Milk (मिश्रित)",
                                litres = milkTypeVolume.third,
                                total = totalMilkTypeVol,
                                color = DairyGold
                            )
                        }
                    }
                }
            }
        }

        // Top Customers Leaderboard
        if (topCustomers.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("top_customers_leaderboard"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.topCustomers,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        val maxTopVol = topCustomers.maxOfOrNull { it.second } ?: 1.0

                        topCustomers.forEachIndexed { index, (customer, litres, amount) ->
                            val progress = (litres / maxTopVol).toFloat().coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. ${customer.name}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$litres L (₹$amount)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DairyBluePrimary
                                        )
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = DairyBluePrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * D3-style Smooth Cubic Bezier Trend Chart
 * Uses linear scaling logic (domain to range) with interactive scrubbing inspection.
 */
@Composable
private fun D3MilkTrendChart(
    dailyData: List<DailyAggregation>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val maxVolume = remember(dailyData) {
        val peak = dailyData.maxOfOrNull { it.totalLitres } ?: 10.0
        if (peak <= 0.0) 10.0 else Math.ceil(peak * 1.15)
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dailyData) {
                    detectTapGestures(
                        onPress = { offset ->
                            val leftPad = 32.dp.toPx()
                            val rightPad = 16.dp.toPx()
                            val plotWidth = size.width - leftPad - rightPad
                            if (plotWidth > 0 && dailyData.isNotEmpty()) {
                                val touchX = (offset.x - leftPad).coerceIn(0f, plotWidth)
                                val idx = ((touchX / plotWidth) * (dailyData.size - 1)).toInt()
                                selectedIndex = idx.coerceIn(0, dailyData.lastIndex)
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val padLeft = 32.dp.toPx()
            val padBottom = 24.dp.toPx()
            val padTop = 16.dp.toPx()
            val padRight = 16.dp.toPx()

            val chartWidth = width - padLeft - padRight
            val chartHeight = height - padTop - padBottom

            if (chartWidth <= 0 || chartHeight <= 0 || dailyData.isEmpty()) return@Canvas

            // Horizontal Grid Lines & Y-ticks
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = padTop + chartHeight - (chartHeight / gridCount * i)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(padLeft, y),
                    end = Offset(width - padRight, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // D3 Linear Scale function: map value from [0, maxVolume] to [padTop + chartHeight, padTop]
            fun scaleY(vol: Double): Float {
                val norm = (vol / maxVolume).coerceIn(0.0, 1.0)
                return (padTop + chartHeight - (norm * chartHeight)).toFloat()
            }

            fun scaleX(index: Int): Float {
                val step = chartWidth / (dailyData.size - 1).coerceAtLeast(1)
                return padLeft + (index * step)
            }

            // Compute curve points
            val points = dailyData.mapIndexed { idx, item ->
                Offset(scaleX(idx), scaleY(item.totalLitres))
            }

            // Construct Smooth Bezier Curve Path & Area Path
            val strokePath = Path()
            val areaPath = Path()

            if (points.isNotEmpty()) {
                strokePath.moveTo(points.first().x, points.first().y)
                areaPath.moveTo(points.first().x, padTop + chartHeight)
                areaPath.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    areaPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }

                areaPath.lineTo(points.last().x, padTop + chartHeight)
                areaPath.close()

                // Draw Gradient Fill (D3 area chart style)
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1976D2).copy(alpha = 0.35f),
                            Color(0xFF1976D2).copy(alpha = 0.02f)
                        ),
                        startY = padTop,
                        endY = padTop + chartHeight
                    )
                )

                // Draw Smooth Stroke
                drawPath(
                    path = strokePath,
                    color = Color(0xFF0D47A1),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Selected point highlight
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        // Vertical guideline
                        drawLine(
                            color = Color(0xFF0D47A1).copy(alpha = 0.4f),
                            start = Offset(pt.x, padTop),
                            end = Offset(pt.x, padTop + chartHeight),
                            strokeWidth = 1.2.dp.toPx()
                        )
                        // Outer halo & center dot
                        drawCircle(color = Color(0xFF1976D2).copy(alpha = 0.25f), radius = 7.dp.toPx(), center = pt)
                        drawCircle(color = Color(0xFF0D47A1), radius = 4.dp.toPx(), center = pt)
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
                    }
                }
            }
        }

        // Tooltip Overlay when a point is tapped
        selectedIndex?.let { idx ->
            if (idx in dailyData.indices) {
                val item = dailyData[idx]
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Day ${item.dayNumber}: ${item.totalLitres} L (M: ${item.morningLitres}L, E: ${item.eveningLitres}L • ₹${item.totalAmount})",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Recharts-style Grouped Bar Chart comparing Billing vs Payments
 */
@Composable
private fun RechartsCashflowBarChart(
    weeklyCashflow: List<WeeklyCashflow>,
    modifier: Modifier = Modifier
) {
    val maxVal = remember(weeklyCashflow) {
        val peak = weeklyCashflow.maxOfOrNull { Math.max(it.billedAmount, it.collectedAmount) } ?: 500.0
        if (peak <= 0.0) 500.0 else Math.ceil(peak * 1.2)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val padLeft = 24.dp.toPx()
        val padRight = 16.dp.toPx()
        val padTop = 16.dp.toPx()
        val padBottom = 28.dp.toPx()

        val chartWidth = width - padLeft - padRight
        val chartHeight = height - padTop - padBottom

        if (chartWidth <= 0 || chartHeight <= 0 || weeklyCashflow.isEmpty()) return@Canvas

        // Base line
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(padLeft, padTop + chartHeight),
            end = Offset(width - padRight, padTop + chartHeight),
            strokeWidth = 1.dp.toPx()
        )

        val groupWidth = chartWidth / weeklyCashflow.size
        val barWidth = 14.dp.toPx()
        val barGap = 4.dp.toPx()

        weeklyCashflow.forEachIndexed { idx, flow ->
            val groupCenterX = padLeft + (idx * groupWidth) + (groupWidth / 2f)

            // Billed Bar (Blue)
            val billedH = ((flow.billedAmount / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
            val billedX = groupCenterX - barWidth - (barGap / 2f)
            val billedY = padTop + chartHeight - billedH

            drawRoundRect(
                color = Color(0xFF0D47A1),
                topLeft = Offset(billedX, billedY),
                size = Size(barWidth, billedH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Collected Bar (Green)
            val collH = ((flow.collectedAmount / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
            val collX = groupCenterX + (barGap / 2f)
            val collY = padTop + chartHeight - collH

            drawRoundRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(collX, collY),
                size = Size(barWidth, collH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

/**
 * Recharts-style Donut Chart
 */
@Composable
private fun RechartsDonutChart(
    cowLitres: Double,
    buffaloLitres: Double,
    mixedLitres: Double,
    modifier: Modifier = Modifier
) {
    val total = cowLitres + buffaloLitres + mixedLitres

    Canvas(modifier = modifier) {
        val diameter = Math.min(size.width, size.height)
        val strokeW = 16.dp.toPx()
        val arcSize = Size(diameter - strokeW, diameter - strokeW)
        val topLeft = Offset(strokeW / 2f, strokeW / 2f)

        if (total <= 0.0) {
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            return@Canvas
        }

        val cowSweep = ((cowLitres / total) * 360f).toFloat()
        val bufSweep = ((buffaloLitres / total) * 360f).toFloat()
        val mixSweep = (360f - cowSweep - bufSweep).coerceAtLeast(0f)

        var currentAngle = -90f

        if (cowSweep > 0) {
            drawArc(
                color = Color(0xFF0D47A1),
                startAngle = currentAngle,
                sweepAngle = cowSweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            currentAngle += cowSweep
        }

        if (bufSweep > 0) {
            drawArc(
                color = Color(0xFF00897B),
                startAngle = currentAngle,
                sweepAngle = bufSweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            currentAngle += bufSweep
        }

        if (mixSweep > 0) {
            drawArc(
                color = Color(0xFFFFB300),
                startAngle = currentAngle,
                sweepAngle = mixSweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun DonutLegendItem(
    label: String,
    litres: Double,
    total: Double,
    color: Color
) {
    val pct = if (total > 0) Math.round((litres / total) * 1000.0) / 10.0 else 0.0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            Text(
                text = "$litres L ($pct%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalyticsKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getDayFromDate(dateStr: String): Int {
    return try {
        dateStr.substringAfterLast("-").toInt()
    } catch (e: Exception) {
        1
    }
}
