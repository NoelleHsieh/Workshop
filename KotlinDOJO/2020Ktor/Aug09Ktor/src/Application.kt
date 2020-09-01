package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import kotlinx.html.*
import kotlinx.css.*
import freemarker.cache.*
import io.ktor.freemarker.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    val client = HttpClient(Apache) {
    }


    routing {
        get("/") {
            val user = environment.config.property("ktor.db.usr").getString()
            println(user)
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-dsl") {
            htmlDSL()
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

data class IndexData(val items: List<Int>)

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
