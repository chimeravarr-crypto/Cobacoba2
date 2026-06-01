package com.example.ui.screens

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {
    return try {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        var formatted = format.format(amount)
        if (formatted.endsWith(",00")) {
            formatted = formatted.substring(0, formatted.length - 3)
        }
        formatted.replace("Rp", "Rp ").replace("Rp-", "-Rp ")
    } catch (e: Exception) {
        "Rp ${String.format("%,.0f", amount)}"
    }
}
