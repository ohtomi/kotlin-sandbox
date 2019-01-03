package com.github.ohtomi.kotlin.sandbox.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatorTest {

    data class InputField(val fieldName: String, val fieldValue: Any)

    @Test
    fun example() {
        val input = InputField("name", "john doe")
        val value = object : Validatable {
            override val name: String by lazy { input.fieldName }
            override val value: String by lazy { input.fieldValue.toString() }
        }
        val result = value.validate {
            mandatory() and (maxLength(100) or minLength(10))
        }

        assertTrue(result.errors.isEmpty(), "errors must be empty.")
        assertTrue(result.warnings.isNotEmpty(), "warnings must have more than 1 item.")
        assertEquals("name must be 10 characters or more.", result.warnings[0])
    }
}
