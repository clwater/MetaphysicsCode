package clwater.metaphysics

import clwater.metaphysics.model.Gua
import clwater.metaphysics.utils.getListFromFile
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

var listGua :MutableList<Gua> = ArrayList()


class ServerMain{

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            listGua = getListFromFile()
//            println(listGua)
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
            call.respondText(""+ listGua.toString(), ContentType.Text.Html)
        }
        get("/") {
            val map = WebRouting.into("/" ,call)
            call.respond(FreeMarkerContent("index.ftl", map, "index"))
        }
    }
}

fun startServer(){
    embeddedServer(Netty, 9002, watchPaths = listOf("ServerMain"), module = Application::module).start()
}


