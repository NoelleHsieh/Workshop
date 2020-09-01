package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val client = HttpClient(Apache) {
    }

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(Cities, Users)
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/CreateCity/{city_create}"){

            call.parameters["city_create"]?.let {
                transaction {
                    City.new { name = it }
                }
            }
        }

        get("/cities") {

            val cities = transaction {
                City.all().joinToString { it.name }
            }

            call.respondText(cities)
        }

        get("/CreateUser{name}{city}{age}"){

            call.request?.let {
                val name = checkVaildName(it.queryParameters["name"])
                if(name.equals(NULL_PARAMETER)){

                    call.respondText(name,
                            contentType = ContentType.Text.Plain)
                    return@get
                }

                val cityInput = checkVaildName(it.queryParameters["city"])

                var usrCities: MutableList<City> = mutableListOf()
                if(cityInput.equals(NULL_PARAMETER)){

                    call.respondText(cityInput,
                            contentType = ContentType.Text.Plain)
                    return@get

                }else{
                    transaction {
                        usrCities = City.find{
                            Cities.name eq cityInput}.toMutableList()
                        if (usrCities.isEmpty()){
                            usrCities.add(
                                    City.new {this.name = cityInput}
                            )}
                    }
                }

                val age = checkVaildName(it.queryParameters["age"])
                if(age.equals(NULL_PARAMETER)){

                    call.respondText(name,
                            contentType = ContentType.Text.Plain)
                    return@get
                }

                transaction {
                    User.new {
                        this.name = name
                        this.city = usrCities[0]
                        this.age = age.toInt()
                    }
                }

            }

        }

        get("/users") {
            val users = transaction {
                User.all().joinToString {
                    "user:${it.name} city:${it.city.name} age:${it.age}"
                }
            }

            call.respondText(users)
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

private const val NULL_PARAMETER = "Please provide the vaild parameters"

fun checkVaildName(str:String?): String{


    val arg =  str?.let {

        it.removePrefix("\"").removeSuffix("\"").trim()

    }

    return if(arg.isNullOrBlank()) NULL_PARAMETER else arg

}

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
