package com.depsoftware.notifhistory.data.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringListConverterTest {

    private val converter = StringListConverter()

    @Test
    fun fromPaths_emptyList_returnsEmptyStringNotNull() {
        assertEquals("", converter.fromPaths(emptyList()))
        assertEquals("", converter.fromPaths(null))
    }

    @Test
    fun roundTrip_preservesPaths() {
        val paths = listOf("/a/1.jpg", "/a/2.jpg")
        val stored = converter.fromPaths(paths)
        assertTrue(stored.isNotEmpty())
        assertEquals(paths, converter.toPaths(stored))
    }

    @Test
    fun toPaths_blankOrNull_returnsEmptyList() {
        assertEquals(emptyList<String>(), converter.toPaths(null))
        assertEquals(emptyList<String>(), converter.toPaths(""))
        assertEquals(emptyList<String>(), converter.toPaths("   "))
    }
}
