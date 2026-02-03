package com.uansari.stockwise.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateTimeUtils {

    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private val dateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    /**
     * Get start of today in milliseconds.
     */
    fun getStartOfToday(): Long {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Get start of a specific day in milliseconds.
     */
    fun getStartOfDay(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Get end of a specific day in milliseconds.
     */
    fun getEndOfDay(date: LocalDate): Long {
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Convert milliseconds to LocalDateTime.
     */
    fun Long.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this), ZoneId.systemDefault()
        )
    }

    /**
     * Convert milliseconds to LocalDate.
     */
    fun Long.toLocalDate(): LocalDate {
        return this.toLocalDateTime().toLocalDate()
    }

    /**
     * Format timestamp to readable date string.
     */
    fun Long.formatAsDate(): String {
        return this.toLocalDate().format(dateFormatter)
    }

    /**
     * Format timestamp to readable time string.
     */
    fun Long.formatAsTime(): String {
        return this.toLocalDateTime().format(timeFormatter)
    }

    /**
     * Format timestamp to readable date-time string.
     */
    fun Long.formatAsDateTime(): String {
        return this.toLocalDateTime().format(dateTimeFormatter)
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday").
     */
    fun Long.toRelativeTimeString(): String {
        val now = System.currentTimeMillis()
        val diff = now - this

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> this.formatAsDate()
        }
    }
}
