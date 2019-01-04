package com.github.ohtomi.kotlin.sandbox.dsl

import java.io.Writer

interface Element {

    fun render(w: Writer)
}

abstract class Node : Element

class Text(val value: String) : Node() {

    override fun render(w: Writer) {
        w.write(value)
    }
}

abstract class Tag(val name: String) : Element {

    val attributes: MutableList<Pair<String, String>> = mutableListOf()
    val children: MutableList<Element> = mutableListOf()

    override fun render(w: Writer) {
        w.write("<$name${joinAttributes()}>")
        children.forEach {
            it.render(w)
        }
        w.write("</$name>")
    }

    private fun joinAttributes(): String = attributes.map { """ ${it.first}="${it.second}"""" }.joinToString("")
}

fun html(build: Html.() -> Unit): Html {
    val dom = Html()
    dom.build()
    return dom
}

class Html : Tag("html") {

    fun head(build: Head.() -> Unit): Head {
        val dom = Head()
        dom.build()
        children.add(dom)
        return dom
    }

    fun body(build: Body.() -> Unit): Body {
        val dom = Body()
        dom.build()
        children.add(dom)
        return dom
    }
}

class Head : Tag("head") {

    fun title(text: String): Title {
        val dom = Title(text)
        children.add(dom)
        return dom
    }
}

class Title(text: String) : Tag("title") {

    init {
        children.add(Text(text))
    }
}

abstract class BlockElement(name: String) : Tag(name) {

    fun div(build: Div.() -> Unit): Div {
        val dom = Div()
        dom.build()
        children.add(dom)
        return dom
    }

    fun h1(text: String): H1 {
        val dom = H1(text)
        children.add(dom)
        return dom
    }

    fun a(text: String, build: A.() -> Unit): A {
        val dom = A(text)
        dom.build()
        children.add(dom)
        return dom
    }

    operator fun String.unaryMinus() {
        children.add(Text(this))
    }
}

class Body : BlockElement("body")

class Div : BlockElement("div")

abstract class InlineElement(name: String, text: String) : Tag(name) {

    init {
        children.add(Text(text))
    }
}

class H1(text: String) : InlineElement("h1", text)

class A(text: String) : InlineElement("a", text) {

    operator fun String.minus(value: String) {
        attributes.add(this to value)
    }
}
