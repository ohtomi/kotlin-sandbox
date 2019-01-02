package com.github.ohtomi.kotlin.sandbox.dsl

import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class DomTest {

    @Test
    fun example() {
        val dom = html {
            head {
                title("foo bar baz")
            }
            body {
                div {
                    h1("hoge")
                    a("example.com") {
                        "href" - "https://www.example.com"
                        "target" - "_blank"
                    }
                    div {
                        -"blaa"
                        -"blaa"
                        -"blaa"
                    }
                }
            }
        }
        val writer = StringWriter()
        dom.render(writer)

        val want = """<html><head><title>foo bar baz</title></head><body><div><h1>hoge</h1><a href="https://www.example.com" target="_blank">example.com</a><div>blaablaablaa</div></div></body></html>"""
        val got = writer.toString()
        assertEquals(want, got)
    }
}
