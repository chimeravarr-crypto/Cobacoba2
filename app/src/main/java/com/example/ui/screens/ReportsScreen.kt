package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.Account
import com.example.data.Transaction
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorSuccess
import com.example.ui.viewmodel.FinanceViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val transactions by viewModel.filteredTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var activeReportType by remember { mutableStateOf("Bulan Ini") } // "Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini"

    // Calculate aggregated parameters
    val filteredForReport = remember(transactions, activeReportType) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        val todayStr = sdf.format(today)

        val cal = Calendar.getInstance()
        cal.time = today

        when (activeReportType) {
            "Hari Ini" -> transactions.filter { it.transaction_date == todayStr }
            "Minggu Ini" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val oneWeekAgo = sdf.format(cal.time)
                transactions.filter { it.transaction_date >= oneWeekAgo && it.transaction_date <= todayStr }
            }
            "Bulan Ini" -> {
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(today)
                transactions.filter { it.transaction_date.startsWith(currentMonth) }
            }
            "Tahun Ini" -> {
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(today)
                transactions.filter { it.transaction_date.startsWith(currentYear) }
            }
            else -> transactions
        }
    }

    val totalIncome = remember(filteredForReport) {
        filteredForReport.filter { it.type == "Income" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredForReport) {
        filteredForReport.filter { it.type == "Expense" }.sumOf { it.amount }
    }
    val netProfit = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // Selection Toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini").forEach { type ->
                    val isSelected = activeReportType == type
                    Button(
                        onClick = { activeReportType = type },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ringkasan Laporan ($activeReportType)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Pemasukan", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = formatRupiah(totalIncome),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorIncome
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Pengeluaran", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = formatRupiah(totalExpense),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorExpense
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Laba Bersih / Keuntungan:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        text = formatRupiah(netProfit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netProfit >= 0) ColorSuccess else ColorExpense
                    )
                }
            }
        }

        // Action export buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    exportToExcelCSV(context, activeReportType, filteredForReport, accounts, totalIncome, totalExpense, netProfit)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
                    .testTag("export_excel_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Excel", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor Excel", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    exportToPDF(context, activeReportType, filteredForReport, accounts, totalIncome, totalExpense, netProfit)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
                    .testTag("export_pdf_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExpense)
            ) {
                Icon(Icons.Default.Share, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor PDF", fontSize = 12.sp)
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text("Laporan Berkas Transaksi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }

        // Report Items List
        if (filteredForReport.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada transaksi pada periode $activeReportType",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("report_items_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredForReport) { tx ->
                    val acc = accounts.find { it.id == tx.account_id }?.name ?: "Unknown"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tx.transaction_date,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = tx.type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "Income") ColorIncome else ColorExpense,
                                    modifier = Modifier
                                        .background(
                                            (if (tx.type == "Income") ColorIncome else ColorExpense).copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tx.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Akun: $acc",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    if (!tx.note.isNullOrBlank()) {
                                        Text(
                                            text = "Catatan: ${tx.note}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    text = formatRupiah(tx.amount),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (tx.type == "Income") ColorIncome else ColorExpense
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Generates real EXCEL-compatible CSV format sheet
private fun exportToExcelCSV(
    context: Context,
    title: String,
    list: List<Transaction>,
    accounts: List<Account>,
    totalIncome: Double,
    totalExpense: Double,
    netProfit: Double
) {
    try {
        val fileName = "Laporan_Keuangan_${title.replace(" ", "_")}.csv"
        val file = File(context.cacheDir, fileName)
        val out = FileOutputStream(file)

        val csvBuilder = StringBuilder()
        csvBuilder.append("LAPORAN KAS JASA DRIVER - PERIODE ${title.uppercase()}\n")
        csvBuilder.append("Tanggal Ekspor,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

        // Summary metrics
        csvBuilder.append("RINGKASAN\n")
        csvBuilder.append("Total Pemasukan,${totalIncome}\n")
        csvBuilder.append("Total Pengeluaran,${totalExpense}\n")
        csvBuilder.append("Laba Bersih / Margin,${netProfit}\n\n")

        // Headers
        csvBuilder.append("Tanggal,Kategori,Jenis,Akun,Nominal,Catatan\n")

        // Rows
        for (tx in list) {
            val acc = accounts.find { it.id == tx.account_id }?.name ?: "Unknown"
            val cleanNote = (tx.note ?: "").replace(",", ";")
            csvBuilder.append("${tx.transaction_date},${tx.category},${tx.type},$acc,${tx.amount},$cleanNote\n")
        }

        out.write(csvBuilder.toString().toByteArray())
        out.close()

        shareFile(context, file, "text/csv", "Ekspor CSV Keuangan")
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mengekpor Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Generates PDF formatted document and triggers sharing
private fun exportToPDF(
    context: Context,
    period: String,
    list: List<Transaction>,
    accounts: List<Account>,
    totalIncome: Double,
    totalExpense: Double,
    netProfit: Double
) {
    try {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Dimensions in points
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        var yPos = 40f

        // Title Header
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Laporan Catatan Keuangan", 40f, yPos, paint)
        yPos += 22f

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Periode: $period  •  Tanggal Ekspor: $formattedDate", 40f, yPos, paint)
        yPos += 30f

        // Draw Summary Card Block
        paint.isFakeBoldText = true
        canvas.drawText("RINGKASAN KEUANGAN:", 40f, yPos, paint)
        yPos += 18f

        paint.isFakeBoldText = false
        canvas.drawText("Total Pemasukan: Rp ${String.format("%,.0f", totalIncome)}", 50f, yPos, paint)
        yPos += 16f
        canvas.drawText("Total Pengeluaran: Rp ${String.format("%,.0f", totalExpense)}", 50f, yPos, paint)
        yPos += 16f
        paint.isFakeBoldText = true
        canvas.drawText("Keuntungan Bersih: Rp ${String.format("%,.0f", netProfit)}", 50f, yPos, paint)
        yPos += 30f

        // Table Headers
        canvas.drawText("DAFTAR RIWAYAT TRANSAKSI (" + list.size + " data)", 40f, yPos, paint)
        yPos += 18f

        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Tanggal", 40f, yPos, paint)
        canvas.drawText("Kategori", 110f, yPos, paint)
        canvas.drawText("Jenis", 230f, yPos, paint)
        canvas.drawText("Akun", 290f, yPos, paint)
        canvas.drawText("Nominal", 360f, yPos, paint)
        canvas.drawText("Catatan", 450f, yPos, paint)

        // Divider
        yPos += 5f
        canvas.drawLine(40f, yPos, 550f, yPos, paint)
        yPos += 15f

        paint.isFakeBoldText = false
        for (tx in list) {
            // Safe bounds check to avoid paging overflows on sample mockup exports
            if (yPos > 800f) {
                break
            }
            val acc = accounts.find { it.id == tx.account_id }?.name ?: "Unknown"

            canvas.drawText(tx.transaction_date, 40f, yPos, paint)

            val categoryTruncated = if (tx.category.length > 18) tx.category.substring(0, 15) + ".." else tx.category
            canvas.drawText(categoryTruncated, 110f, yPos, paint)

            canvas.drawText(tx.type, 230f, yPos, paint)
            canvas.drawText(acc, 290f, yPos, paint)

            val amtText = "Rp " + String.format("%,.0f", tx.amount)
            canvas.drawText(amtText, 360f, yPos, paint)

            val noteTruncated = if ((tx.note ?: "").length > 16) (tx.note ?: "").substring(0, 13) + ".." else (tx.note ?: "-")
            canvas.drawText(noteTruncated, 450f, yPos, paint)

            yPos += 16f
        }

        pdfDoc.finishPage(page)

        val fileName = "Laporan_Keuangan_${period.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)
        val out = FileOutputStream(file)
        pdfDoc.writeTo(out)
        out.close()
        pdfDoc.close()

        shareFile(context, file, "application/pdf", "Ekspor PDF Keuangan")
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mengekpor PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun shareFile(context: Context, file: File, mimeType: String, docTitle: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, docTitle)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan berkas laporan"))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan laporan: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
