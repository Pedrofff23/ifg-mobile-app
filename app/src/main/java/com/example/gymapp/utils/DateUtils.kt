package com.example.gymapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val isoFormatFull = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val isoFormatShort = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Returns current date/time in ISO 8601 format (UTC)
     */
    fun getNowIso(): String {
        return isoFormat.format(Date()) + "Z"
    }

    fun formatIsoDate(isoString: String?, includeTime: Boolean = false): String {
        if (isoString.isNullOrBlank()) return ""
        
        return try {
            // Clean up the string to handle various ISO formats
            // We want to extract YYYY-MM-DD and optionally HH:mm:ss
            val dateRegex = """(\d{4}-\d{2}-\d{2})(?:[T\s](\d{2}:\d{2}:\d{2}))?""".toRegex()
            val match = dateRegex.find(isoString)
            
            if (match != null) {
                val datePart = match.groupValues[1]
                val timePart = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
                
                val date = if (timePart != null) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse("$datePart $timePart")
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart)
                }
                
                if (includeTime && timePart != null) {
                    displayDateTimeFormat.format(date!!)
                } else {
                    displayFormat.format(date!!)
                }
            } else {
                // Fallback for non-standard formats
                isoString
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
