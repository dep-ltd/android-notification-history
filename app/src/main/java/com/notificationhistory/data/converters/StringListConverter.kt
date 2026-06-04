package com.notificationhistory.data.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromPaths(paths: List<String>?): String? {
        if (paths.isNullOrEmpty()) return null
        return paths.joinToString(PATH_SEPARATOR)
    }

    @TypeConverter
    fun toPaths(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(PATH_SEPARATOR).filter { it.isNotEmpty() }
    }

    companion object {
        private const val PATH_SEPARATOR = "\u001F"
    }
}
