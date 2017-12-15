package clwater.metaphysics.Routing

import clwater.metaphysics.utils.ChineseCalendar
import org.jetbrains.ktor.application.ApplicationCall
import java.text.SimpleDateFormat
import java.util.*

object IndexServer{
    lateinit var map : Map<String , String>
    var year = String()
    var month = String()
    var day = String()

    fun test(){
        println("check date")
        val data = Date()
        year = SimpleDateFormat("yyyy").format(data)
        month = SimpleDateFormat("MM").format(data)
        day = SimpleDateFormat("dd").format(data)
        println(year + month + day )

//
//
////    val c = ChineseCalendar()
//        val c = ChineseCalendar(2017 , 12 ,15)
//        println(c.getChinese(801))
//        println(c.getChinese(802))
//        println(c.getChinese(803))
//        println(c.getChinese(804))
//        println(c.getChinese(805))
//        println(c.getChinese(806))
//        println(c.getChinese(807))
//        println(c.getChinese(808))
//        println(c.getChinese(888))

    }

    fun initView(call: ApplicationCall): Map<Any, Any> {
        test()

        return initMao(call)
    }

    private fun initMao(call: ApplicationCall): Map<Any, Any> {
        return mapOf("year" to year ,
                "month" to month ,
                "day" to day)
    }

}