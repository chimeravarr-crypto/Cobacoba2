package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Account
import com.example.data.Category
import com.example.data.Transaction
import com.example.receiver.NotificationHelper
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorSuccess
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the default notification channels on launch
        NotificationHelper.setupNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                val viewModel: FinanceViewModel = viewModel()
                var activeTab by remember { mutableStateOf("Dashboard") }

                var showAddTxType by remember { mutableStateOf<String?>(null) } // "Income" or "Expense"
                var showEditTxFor by remember { mutableStateOf<Transaction?>(null) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_main_scaffold"),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            val items = listOf("Dashboard", "Transaksi", "Laporan", "Analisis", "Pengaturan")
                            items.forEach { item ->
                                val isSelected = activeTab == item
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { activeTab = item },
                                    label = { Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    icon = {
                                        Icon(
                                            imageVector = when (item) {
                                                "Dashboard" -> Icons.Default.Home
                                                "Transaksi" -> Icons.Default.Search
                                                "Laporan" -> Icons.Default.Share
                                                "Analisis" -> Icons.Default.PlayArrow
                                                else -> Icons.Default.Settings
                                            },
                                            contentDescription = item
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Multi-view tab content switcher
                        when (activeTab) {
                            "Dashboard" -> DashboardScreen(
                                viewModel = viewModel,
                                onShowQuickAdd = { transactionType -> showAddTxType = transactionType }
                            )
                            "Transaksi" -> TransactionsScreen(
                                viewModel = viewModel,
                                onEditTriggered = { editTarget -> showEditTxFor = editTarget }
                            )
                            "Laporan" -> ReportsScreen(viewModel = viewModel)
                            "Analisis" -> AnalysisScreen(viewModel = viewModel)
                            "Pengaturan" -> SettingsScreen(viewModel = viewModel)
                        }

                        // ================= MODAL SHEETS OVERLAYS =================

                        // 1. ADD TRANSACTION OVERLAY DIALOGUE
                        showAddTxType?.let { type ->
                            AddTransactionDialog(
                                type = type,
                                viewModel = viewModel,
                                onDismiss = { showAddTxType = null }
                            )
                        }

                        // 2. EDIT TRANSACTION OVERLAY DIALOGUE
                        showEditTxFor?.let { tx ->
                            EditTransactionDialog(
                                transaction = tx,
                                viewModel = viewModel,
                                onDismiss = { showEditTxFor = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    type: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }

    // Dropdown filters lists
    val filteredCats = categories.filter { it.type == type }
    var selectedCategory by remember { mutableStateOf(filteredCats.firstOrNull()?.name ?: "Lainnya") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull() ?: Account(id = 1, name = "Cash")) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    // Mock receipt attachment state
    var receiptImageStub by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        Toast.makeText(context, "Jumlah nominal harus valid!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.addTransaction(
                        type = type,
                        category = selectedCategory,
                        amount = amount,
                        accountId = selectedAccount.id,
                        note = noteStr,
                        receiptImage = receiptImageStub
                    )
                    Toast.makeText(context, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_add_transaction_btn")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        title = {
            Text(
                text = if (type == "Income") "Tambah Pemasukan (+)" else "Catat Pengeluaran (-)",
                color = if (type == "Income") ColorIncome else ColorExpense,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Jumlah Nominal (Rupiah)") },
                    placeholder = { Text("e.g. 50000") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown Selection
                Text("Kategori:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { categoryDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategory, color = Color.DarkGray)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        filteredCats.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection
                Text("Sumber Akun:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { accountDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedAccount.name, color = Color.DarkGray)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }

                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note description
                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Pengisian BBM di SPBU Dago") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt attachment stub Simulator
                if (receiptImageStub == null) {
                    Button(
                        onClick = {
                            val code = (1000..9999).random()
                            receiptImageStub = "struk_grup_${code}.jpg"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷  Ambil / Upload Struk Pembayaran", fontSize = 12.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorSuccess.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = ColorSuccess)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(receiptImageStub!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSuccess)
                            }
                            IconButton(onClick = { receiptImageStub = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = ColorExpense)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var amountStr by remember { mutableStateOf(transaction.amount.toString()) }
    var noteStr by remember { mutableStateOf(transaction.note ?: "") }
    var dateStr by remember { mutableStateOf(transaction.transaction_date) }

    val filteredCats = categories.filter { it.type == transaction.type }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedAccount by remember { mutableStateOf(accounts.find { it.id == transaction.account_id } ?: (accounts.firstOrNull() ?: Account(id = 1, name = "Cash"))) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    var receiptImageStub by remember { mutableStateOf(transaction.receipt_image) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        Toast.makeText(context, "Jumlah nominal harus valid!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val updated = transaction.copy(
                        category = selectedCategory,
                        amount = amount,
                        account_id = selectedAccount.id,
                        note = if (noteStr.isBlank()) null else noteStr,
                        transaction_date = dateStr,
                        receipt_image = receiptImageStub
                    )
                    viewModel.updateTransaction(updated)
                    Toast.makeText(context, "Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        title = {
            Text("Edit Pencatatan Transaksi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Date field
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Tanggal Kas (YYYY-MM-DD)") },
                    placeholder = { Text("2026-06-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Jumlah Nominal (Rupiah)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selection dropdown
                Text("Kategori:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { categoryDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategory, color = Color.DarkGray)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        filteredCats.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection dropdown
                Text("Sumber Akun:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { accountDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedAccount.name, color = Color.DarkGray)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }

                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text("Catatan / Keterangan tambahan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt photo Stub
                if (receiptImageStub == null) {
                    Button(
                        onClick = {
                            val code = (1000..9999).random()
                            receiptImageStub = "struk_edit_${code}.jpg"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷  Ganti / Upload Struk Baru", fontSize = 12.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorSuccess.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = ColorSuccess)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(receiptImageStub!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSuccess)
                            }
                            IconButton(onClick = { receiptImageStub = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = ColorExpense)
                            }
                        }
                    }
                }
            }
        }
    )
}
