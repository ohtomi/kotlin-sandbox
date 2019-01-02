package com.github.ohtomi.kotlin.sandbox.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class QueryTest {

    @Test
    fun example() {
        val want = "SELECT * FROM Person WHERE ID = 1 ;"
        val got = Query.select("*").from("Person").where("ID = 1").print()
        assertEquals(want, got)
    }
}
