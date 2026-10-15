package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.repository.MonthlyBillSummary
import java.io.File

object ShareHelper {

    fun sharePdfBill(
        context: Context,
        pdfFile: File,
        summary: MonthlyBillSummary,
        dairyName: String
    ) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareMessage = buildString {
                append("🥛 *Milk Bill / दूध का बिल*\n")
                append("━━━━━━━━━━━━━━━━━━━\n")
                append("🏢 *Dairy:* $dairyName\n")
                append("👤 *Customer:* ${summary.customer.name}\n")
                append("📅 *Month:* ${summary.month}\n")
                append("🥛 *Total Litres:* ${summary.totalLitres} L\n")
                append("💰 *Milk Amount:* ₹${summary.currentMonthMilkAmount}\n")
                if (summary.previousBalance != 0.0) {
                    append("⏳ *Previous Balance:* ₹${summary.previousBalance}\n")
                }
                if (summary.totalPaymentsInMonth > 0) {
                    append("💵 *Paid Amount:* ₹${summary.totalPaymentsInMonth}\n")
                }
                append("🔴 *Total Due Balance:* ₹${summary.netBalanceDue}\n")
                append("━━━━━━━━━━━━━━━━━━━\n")
                append("📄 Detailed PDF invoice is attached. Please check.\nThank you!")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Milk Bill - ${summary.customer.name} (${summary.month})")
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Milk Bill via / बिल भेजें (WhatsApp, etc.)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun openPdfViewer(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Open PDF / पीडीएफ देखें")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No PDF viewer app found: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
