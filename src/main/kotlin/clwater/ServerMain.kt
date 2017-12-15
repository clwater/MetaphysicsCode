import com.sun.corba.se.impl.activation.ServerMain
import kotlinx.html.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.host.*   // for embededServer
import org.jetbrains.ktor.netty.*  // for Netty
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.html.*
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.response.*
import freemarker.cache.*; // template loaders live in this package


fun Application.module() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(ServerMain::class.java.classLoader, "/templates")
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/test") {

            val user = mapOf("title" to "Welcome guy", "name" to "user name", "email" to "user@example.com")
            call.respond(FreeMarkerContent("index.ftl", user, "e"))
        }
        get("/") {
            call.respondText("Hello, world!", ContentType.Text.Html)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 9000, watchPaths = listOf("ServerMain"), module = Application::module).start()
}