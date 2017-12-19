package clwater.metaphysics

import clwater.metaphysics.utils.ChineseCalendar
import clwater.metaphysics.utils.GanZhiCalendar
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.host.*   // for embededServer
import org.jetbrains.ktor.netty.*  // for Netty
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.*
import freemarker.cache.* // template loaders live in this package
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.util.runSync
import java.net.URL
import java.sql.Time
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class ServerMain{
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            startServer()
//            test()
        }
    }
}




fun Application.module() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(ServerMain::class.java.classLoader, "/templates") as TemplateLoader?
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/test") {
            val user = mapOf("title" to "这是title", "name" to "user name", "email" to "user@example.com")
            call.respond(FreeMarkerContent("index.ftl", user, "e"))
        }
        get("/") {
            val map = WebRouting.into("/" ,call)
            call.respond(FreeMarkerContent("index.ftl", map, "e"))
        }
    }
}

fun startServer(){
    embeddedServer(Netty, 9001, watchPaths = listOf("ServerMain"), module = Application::module).start()
}

