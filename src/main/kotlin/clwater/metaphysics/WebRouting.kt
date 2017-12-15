package clwater.metaphysics

import clwater.metaphysics.Routing.IndexServer
import clwater.metaphysics.utils.ChineseCalendar
import org.jetbrains.ktor.application.ApplicationCall
import java.text.SimpleDateFormat
import java.util.*

object WebRouting {
    fun into(type : String , call: ApplicationCall ): Map<Any, Any> {
        when (type){
            "/" -> return intoIndex(call)
            else -> return mapOf()
        }
    }

    fun intoIndex(call: ApplicationCall): Map<Any, Any>{
        return IndexServer.initView(call)
    }
}