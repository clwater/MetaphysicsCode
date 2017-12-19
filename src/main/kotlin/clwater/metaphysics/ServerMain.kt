package clwater.metaphysics

import clwater.metaphysics.model.Gua
import clwater.metaphysics.utils.getListFromFile
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.host.*   // for embededServer
import org.jetbrains.ktor.netty.*  // for Netty
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.*
import freemarker.cache.* // template loaders live in this package
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.get

var listGua :MutableList<Gua> = ArrayList()


class ServerMain{

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            listGua = getListFromFile()
//            startServer()
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
    embeddedServer(Netty, 9002, watchPaths = listOf("ServerMain"), module = Application::module).start()
}


