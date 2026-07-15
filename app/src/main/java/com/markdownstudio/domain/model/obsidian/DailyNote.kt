package com.markdownstudio.domain.model.obsidian

data class DailyNote(
    val date: String,
    val uri: String,
    val title: String,
    val exists: Boolean = false,
    val content: String = ""
) {
    companion object {
        fun today(): String {
            val now = java.time.LocalDate.now()
            return String.format(
                "%04d-%02d-%02d",
                now.year, now.monthValue, now.dayOfMonth
            )
        }

        fun fromDate(date: java.time.LocalDate): String {
            return String.format(
                "%04d-%02d-%02d",
                date.year, date.monthValue, date.dayOfMonth
            )
        }
    }
}
