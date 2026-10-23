package com.example.myapplication.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SeguridadTest {

    @Test
    fun hash_coincideConSha256Conocido() {
        // SHA-256("123456"), verificado con sha256sum
        assertEquals(
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
            Seguridad.hash("123456")
        )
    }

    @Test
    fun hash_esDeterministaYDistingueClaves() {
        assertEquals(Seguridad.hash("clave"), Seguridad.hash("clave"))
        assertNotEquals(Seguridad.hash("clave"), Seguridad.hash("Clave"))
    }
}
