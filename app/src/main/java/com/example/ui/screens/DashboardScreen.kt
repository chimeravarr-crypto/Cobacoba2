package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Account
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorSuccess
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onShowQuickAdd: (String) -> Unit // "Income" or "Expense" dialog trigger
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val balances by viewModel.accountBalances.collectAsState()
    val stats by viewModel.statsThisMonth.collectAsState()

    val targetHarian by viewModel.targetHarian.collectAsState()
    val todayGojekIncome by viewModel.todayGojekIncome.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp) // Spacing for floating action buttons or indicators
        ) {
            // Header Card - Total Balance & Monthly Overview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Total Saldo Gabungan",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(totalBalance),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "▲",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pemasukan",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = formatRupiah(stats.income),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "▼",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pengeluaran",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = formatRupiah(stats.expense),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Laba Bersih Bulan Ini:",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = formatRupiah(stats.net),
                            color = if (stats.net >= 0) Color.White else Color(0xFFFFCDD2),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Gojek Daily Target Indicator Widget
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Target Gojek Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val pct = if (targetHarian > 0) (todayGojekIncome / targetHarian * 100).coerceIn(0.0, 100.0) else 0.0
                        Text(
                            text = "${pct.toInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (pct >= 100) ColorSuccess else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val rawPct = if (targetHarian > 0) (todayGojekIncome / targetHarian).toFloat() else 0f
                    val progressVal = rawPct.coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progressVal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (progressVal >= 1f) ColorSuccess else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Pendapatan Hari Ini", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupiah(todayGojekIncome),
                                fontWeight = FontWeight.Bold,
                                color = ColorSuccess,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Target", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupiah(targetHarian),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Sisa Target", fontSize = 11.sp, color = Color.Gray)
                            val remaining = (targetHarian - todayGojekIncome).coerceAtLeast(0.0)
                            Text(
                                text = formatRupiah(remaining),
                                fontWeight = FontWeight.Bold,
                                color = if (remaining > 0) ColorExpense else ColorSuccess,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Quick Actions "Pencatatan Cepat" Panel
            Text(
                text = "⚡ Transaksi Cepat (<3 Klik)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // BBM Cepat button
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable { viewModel.quickBBMExpense() },
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⛽",
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BBM 20rb",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Cash",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Gojek Quick Income Button
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable { viewModel.quickGojekIncome() },
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛵",
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ojek 150rb",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "GoPay",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial Accounts Segment
            Text(
                text = "💰 Rincian Saldo Akun",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Dynamic listed accounts
            accounts.forEach { account ->
                val bal = balances[account.id] ?: 0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (account.name.lowercase()) {
                                            "cash" -> ColorExpense.copy(alpha = 0.1f)
                                            "gopay" -> ColorSuccess.copy(alpha = 0.1f)
                                            "bca" -> ColorIncome.copy(alpha = 0.1f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (account.name.lowercase()) {
                                        "cash" -> "💵"
                                        "gopay" -> "📱"
                                        "bca" -> "💳"
                                        else -> "💰"
                                    },
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = account.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = formatRupiah(bal),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (bal >= 0) MaterialTheme.colorScheme.onSurface else ColorExpense
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Trend Chart Widget (Custom Draw)
            Text(
                text = "📊 Sketsa Alur Kas (Bulan Ini)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Pemasukan", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupiah(stats.income),
                                fontWeight = FontWeight.Bold,
                                color = ColorSuccess
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Pengeluaran", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupiah(stats.expense),
                                fontWeight = FontWeight.Bold,
                                color = ColorExpense
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw nice representation comparison bars
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val maxVal = maxOf(stats.income, stats.expense, 1000.0)
                        val totalWidth = size.width
                        val barHeight = size.height

                        val incomeRatio = (stats.income / maxVal).toFloat()
                        val expenseRatio = (stats.expense / maxVal).toFloat()

                        val columnWidth = totalWidth * 0.35f
                        val gap = totalWidth * 0.1f

                        // Draw background guidelines
                        for (i in 1..3) {
                            val y = barHeight * (i / 4f)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(totalWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw Income bar (left side)
                        val incBarHeightActual = barHeight * incomeRatio
                        drawRoundRect(
                            color = ColorSuccess,
                            topLeft = Offset(totalWidth * 0.15f, barHeight - incBarHeightActual),
                            size = Size(columnWidth, incBarHeightActual),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Draw Expense bar (right side)
                        val expBarHeightActual = barHeight * expenseRatio
                        drawRoundRect(
                            color = ColorExpense,
                            topLeft = Offset(totalWidth * 0.15f + columnWidth + gap, barHeight - expBarHeightActual),
                            size = Size(columnWidth, expBarHeightActual),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Pemasukan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorSuccess,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Pengeluaran",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorExpense,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Floating Action Button with quick picker menu (Income vs Expense)
        var expandedFab by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(visible = expandedFab) {
                Column(horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        onClick = {
                            expandedFab = false
                            onShowQuickAdd("Income")
                        },
                        containerColor = ColorIncome,
                        contentColor = Color.White,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .testTag("fab_add_income")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("+ Pemasukan ")
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            expandedFab = false
                            onShowQuickAdd("Expense")
                        },
                        containerColor = ColorExpense,
                        contentColor = Color.White,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .testTag("fab_add_expense")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("- Pengeluaran ")
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { expandedFab = !expandedFab },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_expand_trigger")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction Options"
                )
            }
        }
    }
}
