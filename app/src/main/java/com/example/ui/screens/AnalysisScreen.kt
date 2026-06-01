package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorSuccess
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalysisScreen(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val operationalCostsTotal by viewModel.totalMonthlyOperationalCost.collectAsState()
    val dailyBurden by viewModel.dailyOperationalBurden.collectAsState()

    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }

    // Computations
    val thisMonthTx = remember(transactions) {
        transactions.filter { it.transaction_date.startsWith(currentMonth) }
    }

    val monthlyIncome = remember(thisMonthTx) {
        thisMonthTx.filter { it.type == "Income" }.sumOf { it.amount }
    }

    val monthlyExpense = remember(thisMonthTx) {
        thisMonthTx.filter { it.type == "Expense" }.sumOf { it.amount }
    }

    // Monthly Profit = Income - Expense - Operational Costs
    val netDriverProfit = monthlyIncome - monthlyExpense - operationalCostsTotal

    // Largest Expense Category
    val expenseCategoryTotals = remember(thisMonthTx) {
        thisMonthTx.filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val largestExpenseCategory = remember(expenseCategoryTotals) {
        expenseCategoryTotals.maxByOrNull { it.value }
    }

    // Largest Income Category
    val incomeCategoryTotals = remember(thisMonthTx) {
        thisMonthTx.filter { it.type == "Income" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
            .testTag("analysis_screen")
    ) {
        // Hero Card - Net Driver Margin
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "💸 KEUNTUNGAN BERSIH DRIVER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRupiah(netDriverProfit),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (netDriverProfit >= 0) ColorSuccess else ColorExpense
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dihitung setelah dikurangi Pengeluaran Laisan & Beban Tetap Operasional.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Operational Metrics Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pemasukan", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = formatRupiah(monthlyIncome),
                        fontWeight = FontWeight.Bold,
                        color = ColorIncome,
                        fontSize = 14.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pengeluaran Kas", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = formatRupiah(monthlyExpense),
                        fontWeight = FontWeight.Bold,
                        color = ColorExpense,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Beban Bulanan", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = formatRupiah(operationalCostsTotal),
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Beban Tetap Harian", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = formatRupiah(dailyBurden),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Largest Expense Highlight
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorExpense.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Kategori Pengeluaran Terbesar", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = largestExpenseCategory?.key ?: "Belum Ada",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    largestExpenseCategory?.let {
                        Text(
                            text = "Menyerap ${formatRupiah(it.value)} bulan ini",
                            fontSize = 12.sp,
                            color = ColorExpense,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Visualizers (Dynamic Canvas)
        Text(
            text = "📊 Distribusi Pengeluaran",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (expenseCategoryTotals.isEmpty()) {
                    Text(
                        text = "Belum ada transaksi pengeluaran bulan ini untuk dibuat grafik.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                } else {
                    val colors = listOf(
                        Color(0xFFE53935), Color(0xFFFFB74D), Color(0xFF81C784),
                        Color(0xFF64B5F6), Color(0xFFBA68C4), Color(0xFFFF8A65),
                        Color(0xFFAED581), Color(0xFF90A4AE), Color(0xFFD4E157)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Pie Chart canvas representation
                        Canvas(
                            modifier = Modifier
                                .size(120.dp)
                                .weight(1f)
                        ) {
                            val totalExp = expenseCategoryTotals.values.sum()
                            if (totalExp > 0) {
                                var currentAngle = 0f
                                expenseCategoryTotals.toList().forEachIndexed { index, pair ->
                                    val sweep = ((pair.second / totalExp) * 360f).toFloat()
                                    drawArc(
                                        color = colors[index % colors.size],
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        size = Size(size.width, size.height)
                                    )
                                    currentAngle += sweep
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Category Labels Legend list
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            expenseCategoryTotals.toList().take(5).forEachIndexed { index, pair ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(colors[index % colors.size], shape = RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = pair.first,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatRupiah(pair.second),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Income Distribution Chart
        Text(
            text = "📈 Hubungan Rincian Pendapatan",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (incomeCategoryTotals.isEmpty()) {
                    Text(
                        text = "Belum ada transaksi pendapatan bulan ini untuk dibuat grafik.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                } else {
                    val colors = listOf(
                        Color(0xFF1E88E5), Color(0xFF26A69A), Color(0xFFAB47BC),
                        Color(0xFF2E7D32), Color(0xFFFFA726), Color(0xFFD4E157)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(120.dp)
                                .weight(1f)
                        ) {
                            val totalInc = incomeCategoryTotals.values.sum()
                            if (totalInc > 0) {
                                var currentAngle = 0f
                                incomeCategoryTotals.toList().forEachIndexed { index, pair ->
                                    val sweep = ((pair.second / totalInc) * 360f).toFloat()
                                    drawArc(
                                        color = colors[index % colors.size],
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        size = Size(size.width, size.height)
                                    )
                                    currentAngle += sweep
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            incomeCategoryTotals.toList().take(5).forEachIndexed { index, pair ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(colors[index % colors.size], shape = RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = pair.first,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatRupiah(pair.second),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
