package com.example.gymapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val isoFormatShort = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun formatIsoDate(isoString: String?, includeTime: Boolean = false): String {
        if (isoString.isNullOrBlank()) return ""
        
        return try {
            // Clean up the string to handle various ISO formats (with/without milliseconds, with/without Z)
            val cleaned = isoString.split(".")[0].replace("Z", "").replace("T", " ")
            val date = if (cleaned.length > 10) {
                 SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(cleaned)
            } else {
                 SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(cleaned)
            }
            
            if (includeTime) {
                displayDateTimeFormat.format(date!!)
            } else {
                displayFormat.format(date!!)
            }
        } catch (e: Exception) {
            // Fallback: if it's at least YYYY-MM-DD, try to flip it
            if (isoString.length >= 10 && isoString[4] == '-' && isoString[7] == '-') {
                val parts = isoString.take(10).split("-")
                if (parts.size == 3) {
                    return "${parts[2]}/${parts[1]}/${parts[0]}"
                }
            }
            isoString
        }
    }
}
