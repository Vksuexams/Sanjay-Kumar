package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.repository.MonthlyBillSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateMonthlyBillPdf(
        context: Context,
        billSummary: MonthlyBillSummary,
        profile: BusinessProfileEntity
    ): File {
        val pdfDir = File(context.cacheDir, "bills")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }

        val sanitizedCustomerName = billSummary.customer.name.replace("\\s+".toRegex(), "_")
        val fileName = "MilkBill_${sanitizedCustomerName}_${billSummary.month}.pdf"
        val pdfFile = File(pdfDir, fileName)

        val document = PdfDocument()

        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points

        // Setup paints
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val primaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(13, 71, 161) // #0D47A1 Deep Dairy Blue
            style = Paint.Style.FILL
        }

        val lightBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 244, 248)
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(218, 224, 233)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val entries = billSummary.entries.sortedBy { it.date }
        val maxEntriesPerPage = 22
        val totalPages = Math.max(1, ((entries.size + 4) / maxEntriesPerPage) + 1)

        var entryIndex = 0

        for (pageNum in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 30f

            if (pageNum == 1) {
                // Header Banner
                val headerRect = RectF(24f, y, (pageWidth - 24).toFloat(), y + 80f)
                canvas.drawRoundRect(headerRect, 8f, 8f, primaryPaint)

                // Dairy Name
                boldPaint.color = Color.WHITE
                boldPaint.textSize = 18f
                canvas.drawText(profile.dairyName, 40f, y + 28f, boldPaint)

                // Subtitle / Contact
                textPaint.color = Color.rgb(227, 242, 253)
                textPaint.textSize = 10f
                val contactInfo = buildString {
                    if (profile.ownerName.isNotBlank()) append("Owner: ${profile.ownerName} | ")
                    if (profile.phone.isNotBlank()) append("Mob: ${profile.phone}")
                    if (profile.address.isNotBlank()) append(" | ${profile.address}")
                }
                canvas.drawText(contactInfo, 40f, y + 46f, textPaint)

                // Bill title
                boldPaint.color = Color.rgb(255, 213, 79) // Gold accent
                boldPaint.textSize = 12f
                canvas.drawText("MILK BILL / दूध का बिल", 40f, y + 66f, boldPaint)

                val invoiceNo = "${profile.invoicePrefix}-${billSummary.month.replace("-", "")}-${billSummary.customer.id}"
                textPaint.color = Color.WHITE
                val invText = "Inv: $invoiceNo"
                val invWidth = textPaint.measureText(invText)
                canvas.drawText(invText, pageWidth - 40f - invWidth, y + 66f, textPaint)

                y += 95f

                // Bill & Customer details box
                val infoBox = RectF(24f, y, (pageWidth - 24).toFloat(), y + 68f)
                canvas.drawRoundRect(infoBox, 6f, 6f, lightBgPaint)
                canvas.drawRoundRect(infoBox, 6f, 6f, strokePaint)

                boldPaint.color = Color.rgb(13, 71, 161)
                boldPaint.textSize = 11f
                canvas.drawText("CUSTOMER INFO / ग्राहक जानकारी", 34f, y + 18f, boldPaint)

                textPaint.color = Color.BLACK
                textPaint.textSize = 10f
                canvas.drawText("Name: ${billSummary.customer.name}", 34f, y + 36f, textPaint)
                canvas.drawText("Phone: ${billSummary.customer.phone.ifBlank { "N/A" }}", 34f, y + 52f, textPaint)
                canvas.drawText("Milk Type: ${billSummary.customer.milkType}", 34f, y + 62f, textPaint)

                val rightColX = 320f
                boldPaint.color = Color.rgb(13, 71, 161)
                canvas.drawText("BILLING PERIOD / बिल अवधि", rightColX, y + 18f, boldPaint)
                textPaint.color = Color.BLACK
                canvas.drawText("Month: ${billSummary.month}", rightColX, y + 36f, textPaint)
                val issueDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
                canvas.drawText("Date: $issueDate", rightColX, y + 52f, textPaint)

                y += 80f

                // Summary Cards Row
                val cardWidth = (pageWidth - 48f - 18f) / 4f
                val cardHeight = 44f

                // 1. Total Litres
                drawSummaryPill(canvas, 24f, y, cardWidth, cardHeight, "Total Litres (लीटर)", "${billSummary.totalLitres} L", Color.rgb(33, 150, 243))
                // 2. Milk Amount
                drawSummaryPill(canvas, 24f + cardWidth + 6f, y, cardWidth, cardHeight, "Milk Total (रुपये)", "₹${billSummary.currentMonthMilkAmount}", Color.rgb(76, 175, 80))
                // 3. Paid
                drawSummaryPill(canvas, 24f + (cardWidth + 6f) * 2, y, cardWidth, cardHeight, "Paid (जमा)", "₹${billSummary.totalPaymentsInMonth}", Color.rgb(0, 150, 136))
                // 4. Net Due
                val dueColor = if (billSummary.netBalanceDue > 0) Color.rgb(211, 47, 47) else Color.rgb(56, 142, 60)
                drawSummaryPill(canvas, 24f + (cardWidth + 6f) * 3, y, cardWidth, cardHeight, "Net Due (बकाया)", "₹${billSummary.netBalanceDue}", dueColor)

                y += 56f
            } else {
                // Continuation Header for Subsequent Pages
                boldPaint.color = Color.rgb(13, 71, 161)
                boldPaint.textSize = 12f
                canvas.drawText("${profile.dairyName} - Milk Bill (${billSummary.month}) - Page $pageNum of $totalPages", 24f, y + 10f, boldPaint)
                y += 24f
            }

            // Table Header
            val tableLeft = 24f
            val tableRight = (pageWidth - 24).toFloat()
            val headerH = 22f

            val thRect = RectF(tableLeft, y, tableRight, y + headerH)
            primaryPaint.color = Color.rgb(21, 101, 192)
            canvas.drawRect(thRect, primaryPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 8.5f

            val colX = floatArrayOf(
                tableLeft + 6f,       // Date
                tableLeft + 70f,      // Morning Litres
                tableLeft + 140f,     // Morning Rate
                tableLeft + 210f,     // Evening Litres
                tableLeft + 280f,     // Evening Rate
                tableLeft + 360f,     // Day Litres
                tableLeft + 440f      // Amount
            )

            canvas.drawText("Date (तारीख)", colX[0], y + 15f, boldPaint)
            canvas.drawText("Morn L (सुबह)", colX[1], y + 15f, boldPaint)
            canvas.drawText("Morn Rate", colX[2], y + 15f, boldPaint)
            canvas.drawText("Eve L (शाम)", colX[3], y + 15f, boldPaint)
            canvas.drawText("Eve Rate", colX[4], y + 15f, boldPaint)
            canvas.drawText("Day Total L", colX[5], y + 15f, boldPaint)
            canvas.drawText("Amount (₹)", colX[6], y + 15f, boldPaint)

            y += headerH

            // Table Rows
            val rowH = 17f
            var rowCountOnThisPage = 0

            val altRowPaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }

            while (entryIndex < entries.size && rowCountOnThisPage < maxEntriesPerPage && y + rowH < pageHeight - 80f) {
                val entry = entries[entryIndex]

                if (rowCountOnThisPage % 2 == 1) {
                    canvas.drawRect(tableLeft, y, tableRight, y + rowH, altRowPaint)
                }
                canvas.drawLine(tableLeft, y + rowH, tableRight, y + rowH, strokePaint)

                textPaint.color = Color.BLACK
                textPaint.textSize = 8.5f

                // Format date to show dd-MMM (e.g. 15-Oct)
                val displayDate = try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.date)
                    SimpleDateFormat("dd-MMM", Locale.getDefault()).format(parsed ?: Date())
                } catch (e: Exception) {
                    entry.date
                }

                canvas.drawText(displayDate, colX[0], y + 12f, textPaint)
                canvas.drawText(if (entry.morningLitres > 0) "${entry.morningLitres}" else "-", colX[1], y + 12f, textPaint)
                canvas.drawText(if (entry.morningLitres > 0) "₹${entry.morningRate}" else "-", colX[2], y + 12f, textPaint)
                canvas.drawText(if (entry.eveningLitres > 0) "${entry.eveningLitres}" else "-", colX[3], y + 12f, textPaint)
                canvas.drawText(if (entry.eveningLitres > 0) "₹${entry.eveningRate}" else "-", colX[4], y + 12f, textPaint)
                canvas.drawText("${entry.totalLitres} L", colX[5], y + 12f, textPaint)
                canvas.drawText("₹${entry.totalAmount}", colX[6], y + 12f, textPaint)

                y += rowH
                entryIndex++
                rowCountOnThisPage++
            }

            // Outline around table
            canvas.drawRect(tableLeft, y - (rowCountOnThisPage * rowH) - headerH, tableRight, y, strokePaint)

            // If last page, print totals and footer
            if (pageNum == totalPages || entryIndex >= entries.size) {
                y += 12f

                // Calculation Ledger Box
                val ledgerBox = RectF(280f, y, tableRight, y + 90f)
                canvas.drawRoundRect(ledgerBox, 4f, 4f, lightBgPaint)
                canvas.drawRoundRect(ledgerBox, 4f, 4f, strokePaint)

                var ly = y + 18f
                drawLedgerLine(canvas, textPaint, boldPaint, "Current Month Milk Total:", "₹${billSummary.currentMonthMilkAmount}", 290f, tableRight - 10f, ly)
                ly += 16f
                drawLedgerLine(canvas, textPaint, boldPaint, "Previous Balance (पुराना बकाया):", "₹${billSummary.previousBalance}", 290f, tableRight - 10f, ly)
                ly += 16f
                drawLedgerLine(canvas, textPaint, boldPaint, "Payments Received (जमा किया):", "- ₹${billSummary.totalPaymentsInMonth}", 290f, tableRight - 10f, ly)
                ly += 18f
                canvas.drawLine(290f, ly - 6f, tableRight - 10f, ly - 6f, strokePaint)
                drawLedgerLine(canvas, boldPaint, boldPaint, "NET AMOUNT DUE (कुल बकाया):", "₹${billSummary.netBalanceDue}", 290f, tableRight - 10f, ly, Color.rgb(198, 40, 40))

                // UPI / Payment Note on left
                if (profile.upiId.isNotBlank()) {
                    val upiBox = RectF(tableLeft, y, 260f, y + 54f)
                    canvas.drawRoundRect(upiBox, 4f, 4f, lightBgPaint)
                    canvas.drawRoundRect(upiBox, 4f, 4f, strokePaint)
                    boldPaint.color = Color.rgb(13, 71, 161)
                    boldPaint.textSize = 9f
                    canvas.drawText("UPI Payment / ऑनलाइन भुगतान", tableLeft + 10f, y + 18f, boldPaint)
                    textPaint.color = Color.BLACK
                    textPaint.textSize = 9f
                    canvas.drawText("UPI ID: ${profile.upiId}", tableLeft + 10f, y + 36f, textPaint)
                }

                // Signatures & Footer Note
                val footerY = pageHeight - 45f
                textPaint.color = Color.rgb(117, 117, 117)
                textPaint.textSize = 8f
                canvas.drawText("* Thank you for choosing ${profile.dairyName}. Please pay promptly.", 24f, footerY, textPaint)
                canvas.drawText("Authorized Signatory / अधिकृत हस्ताक्षर: ___________________", tableRight - 220f, footerY, textPaint)
            }

            document.finishPage(page)
        }

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun drawSummaryPill(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        accentColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(220, 226, 235)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val pillRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(pillRect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(pillRect, 6f, 6f, borderPaint)

        // Accent indicator line on left
        val accentPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(x, y + 4f, x + 3.5f, y + height - 4f), 2f, 2f, accentPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText(label, x + 8f, y + 16f, labelPaint)
        canvas.drawText(value, x + 8f, y + 33f, valuePaint)
    }

    private fun drawLedgerLine(
        canvas: Canvas,
        labelPaint: Paint,
        valuePaint: Paint,
        label: String,
        value: String,
        leftX: Float,
        rightX: Float,
        y: Float,
        valueColor: Int = Color.BLACK
    ) {
        labelPaint.color = Color.rgb(51, 65, 85)
        labelPaint.textSize = 9f
        canvas.drawText(label, leftX, y, labelPaint)

        val vPaint = Paint(valuePaint).apply {
            color = valueColor
            textSize = 9.5f
        }
        val vWidth = vPaint.measureText(value)
        canvas.drawText(value, rightX - vWidth, y, vPaint)
    }
}
