package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.Account
import com.example.data.Category
import com.example.data.OperationalCost
import com.example.receiver.NotificationHelper
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorSuccess
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val operationalCosts by viewModel.operationalCosts.collectAsState()
    val targetHarian by viewModel.targetHarian.collectAsState()

    // Notification states
    val notifEnabled by viewModel.notifEnabled.collectAsState()
    val notifTime1 by viewModel.notifTime1.collectAsState()
    val notifTime2 by viewModel.notifTime2.collectAsState()

    // Backup states
    val isBackupEnabled by viewModel.isBackupEnabled.collectAsState()
    val isGoogleLoggedIn by viewModel.isGoogleLoggedIn.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()

    val scrollState = rememberScrollState()

    // Dialog triggering states
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddOpCostDialog by remember { mutableStateOf(false) }
    var showEditTargetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 32.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "⚙️ Pengaturan Seluruh Aplikasi",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Account Target Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎯 Pengaturan Target Gojek",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Target Harian Gojek", fontSize = 12.sp, color = Color.Gray)
                        Text(formatRupiah(targetHarian), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Button(onClick = { showEditTargetDialog = true }) {
                        Text("Ubah")
                    }
                }
            }
        }

        // 2. Manage Accounts Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏦 Kelola Akun Keuangan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showAddAccountDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Account", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(acc.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        // Prevent deleting default accounts for safety, but users can delete other added accounts
                        val lowercaseName = acc.name.lowercase()
                        if (lowercaseName != "cash" && lowercaseName != "gopay" && lowercaseName != "bca") {
                            IconButton(onClick = { viewModel.deleteAccount(acc) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorExpense)
                            }
                        } else {
                            Text("Default", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(end = 12.dp))
                        }
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }

        // 3. Manage Categories card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏷️ Kelola Kategori Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Category", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Show only user customized or lists of custom categories
                Text("Daftar Kategori Tersedia:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))

                categories.take(15).forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.name, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (cat.type == "Income") "Pemasukan" else "Pengeluaran",
                                fontSize = 10.sp,
                                color = if (cat.type == "Income") ColorSuccess else ColorExpense,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            val defaultList = listOf(
                                "Pendapatan Gojek", "Bonus Gojek", "Penjualan", "Transfer Masuk", "Gaji", "Lainnya",
                                "BBM", "Servis Motor", "Ganti Oli", "Ban", "Makan", "Belanja", "Tagihan", "Pulsa",
                                "Internet", "Pendidikan", "Kesehatan"
                            )
                            if (!defaultList.contains(cat.name)) {
                                IconButton(onClick = { viewModel.deleteCategory(cat) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorExpense, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Operational Fixed costs card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛵 Biaya Tetap Operasional Bulanan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showAddOpCostDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Cost", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (operationalCosts.isEmpty()) {
                    Text("Belum ada biaya tetap operasional dikonfigurasi.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    operationalCosts.forEach { cost ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(cost.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Beban Bulan", fontSize = 11.sp, color = Color.Gray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatRupiah(cost.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                IconButton(onClick = { viewModel.deleteOperationalCost(cost) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorExpense)
                                }
                            }
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // 5. Daily Notification Hour Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔 Pengingat Harian Notifikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Switch(
                        checked = notifEnabled,
                        onCheckedChange = {
                            viewModel.toggleNotifications(it)
                            if (it) {
                                NotificationHelper.scheduleDailyAlarms(context, notifTime1, notifTime2)
                                Toast.makeText(context, "Alarm pengingat diaktifkan!", Toast.LENGTH_SHORT).show()
                            } else {
                                NotificationHelper.cancelAlarms(context)
                                Toast.makeText(context, "Alarm pengingat dinonaktifkan!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (notifEnabled) {
                    // Hour reminder 1 Settings
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Mengingatkan Pencatatan (Default 21:00)", fontSize = 11.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var textVal1 by remember { mutableStateOf(notifTime1) }
                            OutlinedTextField(
                                value = textVal1,
                                onValueChange = {
                                    textVal1 = it
                                    if (it.length == 5 && it.contains(":")) {
                                        viewModel.updateNotificationTime1(it)
                                        NotificationHelper.scheduleDailyAlarms(context, it, notifTime2)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("21:00") },
                                singleLine = true
                            )
                            Text("WIB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hour reminder 2 Settings
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Memeriksa Target Gojek (Default 23:00)", fontSize = 11.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var textVal2 by remember { mutableStateOf(notifTime2) }
                            OutlinedTextField(
                                value = textVal2,
                                onValueChange = {
                                    textVal2 = it
                                    if (it.length == 5 && it.contains(":")) {
                                        viewModel.updateNotificationTime2(it)
                                        NotificationHelper.scheduleDailyAlarms(context, notifTime1, it)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("23:00") },
                                singleLine = true
                            )
                            Text("WIB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 6. Backup Controls Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "☁️ Pencadangan Google Drive (Offline-First)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Mock login indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGoogleLoggedIn) "Terhubung: Akun Google Aktif" else "Belum terhubung Google Drive",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoogleLoggedIn) ColorSuccess else ColorExpense
                    )
                    Button(
                        onClick = { viewModel.loginGoogleDrive(!isGoogleLoggedIn) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGoogleLoggedIn) ColorExpense else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isGoogleLoggedIn) "Putuskan" else "Hubungkan")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isGoogleLoggedIn) {
                    Text("Pencadangan Manual SQLite", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.backupDatabaseNow()
                                Toast.makeText(context, "Selesai Mencadangkan Database!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Text("Cadangkan", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.restoreDatabaseNow()
                                Toast.makeText(context, "Selesai Memulihkan Database!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Pulihkan", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pencadangan Harian Otomatis", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Mencadangkan database setiap tengah malam", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isBackupEnabled,
                            onCheckedChange = { viewModel.toggleAutoBackup(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: $backupStatus",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ================= ALERTS DIALOGS FOR MODALS =================

        // 1. Target Dialog
        if (showEditTargetDialog) {
            var inputVal by remember { mutableStateOf(targetHarian.toString()) }
            AlertDialog(
                onDismissRequest = { showEditTargetDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val amt = inputVal.toDoubleOrNull() ?: 250000.0
                        viewModel.updateTargetHarian(amt)
                        showEditTargetDialog = false
                    }) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditTargetDialog = false }) {
                        Text("Batal")
                    }
                },
                title = { Text("Detail Target Harian Gojek") },
                text = {
                    Column {
                        Text("Simpan target pendapatan Gojek per-hari (Rupiah)", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputVal,
                            onValueChange = { inputVal = it },
                            placeholder = { Text("Contoh: 250000") },
                            singleLine = true
                        )
                    }
                }
            )
        }

        // 2. Add Account Dialog
        if (showAddAccountDialog) {
            var accNameInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddAccountDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (accNameInput.isNotBlank()) {
                            viewModel.insertAccount(accNameInput)
                        }
                        showAddAccountDialog = false
                    }) {
                        Text("Tambah")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAccountDialog = false }) {
                        Text("Batal")
                    }
                },
                title = { Text("Tambah Akun Keuangan Baru") },
                text = {
                    Column {
                        Text("Contoh: Dana, OVO, Bank Mandiri", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = accNameInput,
                            onValueChange = { accNameInput = it },
                            placeholder = { Text("Nama Akun") },
                            singleLine = true
                        )
                    }
                }
            )
        }

        // 3. Add Category Dialog
        if (showAddCategoryDialog) {
            var catNameInput by remember { mutableStateOf("") }
            var selectedType by remember { mutableStateOf("Income") } // "Income" or "Expense"
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (catNameInput.isNotBlank()) {
                            viewModel.insertCategory(catNameInput, selectedType)
                        }
                        showAddCategoryDialog = false
                    }) {
                        Text("Tambah")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Batal")
                    }
                },
                title = { Text("Tambah Kategori Transaksi") },
                text = {
                    Column {
                        Text("Pilih Tipe Kategori:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedType == "Income", onClick = { selectedType = "Income" })
                            Text("Pemasukan (Income)")
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = selectedType == "Expense", onClick = { selectedType = "Expense" })
                            Text("Pengeluaran (Expense)")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = catNameInput,
                            onValueChange = { catNameInput = it },
                            placeholder = { Text("Nama Kategori Baru") },
                            singleLine = true
                        )
                    }
                }
            )
        }

        // 4. Add Operational cost dialogue
        if (showAddOpCostDialog) {
            var nameInput by remember { mutableStateOf("") }
            var amountInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddOpCostDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (nameInput.isNotBlank() && amt > 0) {
                            viewModel.insertOperationalCost(nameInput, amt, true)
                        }
                        showAddOpCostDialog = false
                    }) {
                        Text("Tambah")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddOpCostDialog = false }) {
                        Text("Batal")
                    }
                },
                title = { Text("Tambah Biaya Tetap Bulanan") },
                text = {
                    Column {
                        Text("Contoh: Cicilan Motor, BPJS, Paket Data Kantor.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Nama Pengeluaran (e.g. Cicilan Motor)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            placeholder = { Text("Nominal Bulanan (e.g. 700000)") },
                            singleLine = true
                        )
                    }
                }
            )
        }
    }
}
