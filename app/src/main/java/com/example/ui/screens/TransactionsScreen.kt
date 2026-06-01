package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Account
import com.example.data.Category
import com.example.data.Transaction
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorSuccess
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    onEditTriggered: (Transaction) -> Unit // Handles transaction edit popup
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Filter bindings from VM
    val selectedFilterType by viewModel.selectedFilterType.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedAccountFilter by viewModel.selectedAccountFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

    var filterSectionExpanded by remember { mutableStateOf(false) }

    // Intermediary details/delete dialog states
    var showDetailFor by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteConfirmFor by remember { mutableStateOf<Transaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        // Search bar & configuration row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari kategori / deskripsi...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("transaction_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Action controls (Filter expanded, Sorter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { filterSectionExpanded = !filterSectionExpanded },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filterSectionExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    contentColor = if (filterSectionExpanded) Color.White else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (filterSectionExpanded) "✖" else "🔍",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kategori & Akun", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Sorting Trigger
            var showSortOptionMenu by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { showSortOptionMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⇅",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(sortType, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showSortOptionMenu,
                    onDismissRequest = { showSortOptionMenu = false }
                ) {
                    val sortTypes = listOf("Terbaru", "Terlama", "Nominal Terbesar")
                    sortTypes.forEach { st ->
                        DropdownMenuItem(
                            text = { Text(st) },
                            onClick = {
                                viewModel.setSortType(st)
                                showSortOptionMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Animated Extended Filters Section (Category & Account filtering)
        AnimatedVisibility(visible = filterSectionExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Time filters pill selectors
                    Text("Jangka Waktu:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val timeRanges = listOf("All" to "Semua", "Day" to "Hari Ini", "Week" to "7 Hari", "Month" to "Bulan Ini", "Year" to "Tahun Ini")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        timeRanges.forEach { range ->
                            val active = selectedFilterType == range.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.setTimeFilter(range.first) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = range.second,
                                    fontSize = 11.sp,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Filter horizontal row scroll
                    Text("Sumber Kas Akun:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            val active = selectedAccountFilter == "All"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.setAccountFilter("All") }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Semua Akun", fontSize = 11.sp, color = if (active) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }

                        items(accounts) { acc ->
                            val active = selectedAccountFilter == acc.id.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.setAccountFilter(acc.id.toString()) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(acc.name, fontSize = 11.sp, color = if (active) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category scroll
                    Text("Pilih Kategori:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            val active = selectedCategoryFilter == "All"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.setCategoryFilter("All") }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Semua Kategori", fontSize = 11.sp, color = if (active) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }

                        items(categories) { cat ->
                            val active = selectedCategoryFilter == cat.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.setCategoryFilter(cat.name) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(cat.name, fontSize = 11.sp, color = if (active) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Transactions History Data List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada catatan keuangan yang sesuai.",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(filteredTransactions) { tx ->
                    val acc = accounts.find { it.id == tx.account_id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clickable { showDetailFor = tx },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular icon depending on type
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.type == "Income") ColorIncome.copy(alpha = 0.1f) else ColorExpense.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tx.type == "Income") "▲" else "▼",
                                    color = if (tx.type == "Income") ColorIncome else ColorExpense,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.category,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tx.note ?: "Tanpa keterangan",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${tx.transaction_date}  |  ${acc?.name ?: "Akun"}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (tx.type == "Income") "+" else "-") + formatRupiah(tx.amount),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (tx.type == "Income") ColorIncome else ColorExpense,
                                    fontSize = 14.sp
                                )
                                // Indicator if receipt uploaded
                                if (!tx.receipt_image.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text("🧾", fontSize = 12.sp, modifier = Modifier.padding(end = 2.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Struk", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= ALERTS DIALOGS FOR CLICKS =================

        // 1. Details Popup Dialogue
        showDetailFor?.let { tx ->
            val acc = accounts.find { it.id == tx.account_id }
            AlertDialog(
                onDismissRequest = { showDetailFor = null },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = {
                                onEditTriggered(tx)
                                showDetailFor = null
                            }
                        ) {
                            Text("Edit", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(
                            onClick = {
                                showDeleteConfirmFor = tx
                                showDetailFor = null
                            }
                        ) {
                            Text("Hapus", color = ColorExpense)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDetailFor = null }) {
                        Text("Tutup")
                    }
                },
                title = { Text("Detail Catatan Transaksi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Divider(modifier = Modifier.padding(bottom = 12.dp))

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Tipe Kas: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                            Text(
                                text = if (tx.type == "Income") "Pemasukan (+)" else "Pengeluaran (-)",
                                color = if (tx.type == "Income") ColorIncome else ColorExpense,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Kategori: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                            Text(tx.category, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Jumlah: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                            Text(
                                text = formatRupiah(tx.amount),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (tx.type == "Income") ColorIncome else ColorExpense
                            )
                        }

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Akun: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                            Text(acc?.name ?: "Kas Akun")
                        }

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Tanggal: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                            Text("${tx.transaction_date} pukul ${tx.created_at}")
                        }

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Catatan: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                            Text(
                                text = tx.note ?: "-",
                                fontSize = 13.sp
                             )
                        }

                        if (!tx.receipt_image.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Foto Struk / Bukti:", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            // Draw nice receipt stub
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🧾", fontSize = 36.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tx.receipt_image,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // 2. Delete Confirmation Dialogue
        showDeleteConfirmFor?.let { tx ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmFor = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            showDeleteConfirmFor = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExpense)
                    ) {
                        Text("Ya, Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmFor = null }) {
                        Text("Batal")
                    }
                },
                title = { Text("Konfirmasi Hapus") },
                text = { Text("Apakah Anda yakin ingin menghapus catatan transaksi ${tx.category} senilai ${formatRupiah(tx.amount)} ini permanen?") }
            )
        }
    }
}
