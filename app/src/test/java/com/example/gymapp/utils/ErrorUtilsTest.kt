package com.example.gymapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ErrorUtilsTest {

    @Test
    fun `parseErrorMessage handles IOException`() {
        val ioException = IOException("Network down")
        val result = ErrorUtils.parseErrorMessage(ioException)
        assertTrue(result.contains("Erro de conexão"))
        assertTrue(result.contains("Verifique sua internet"))
    }

    @Test
    fun `parseErrorMessage handles generic Exception with message`() {
        val exception = Exception("Something went wrong")
        val result = ErrorUtils.parseErrorMessage(exception)
        assertEquals("Something went wrong", result)
    }

    @Test
    fun `parseErrorMessage handles generic Exception without message`() {
        val exception = Exception()
        val result = ErrorUtils.parseErrorMessage(exception)
        assertTrue(result.contains("Ocorreu um erro inesperado"))
    }

    @Test
    fun `parseErrorMessage uses fallback for null message exception`() {
        val exception = RuntimeException(null as String?)
        val result = ErrorUtils.parseErrorMessage(exception, "Custom fallback")
        assertTrue(result.contains("Custom fallback"))
    }
}
