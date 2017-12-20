package clwater.metaphysics.Routing

import clwater.metaphysics.listGua
import clwater.metaphysics.utils.ChineseCalendar
import clwater.metaphysics.utils.GanZhiCalendar
import clwater.metaphysics.utils.dayIndexGanZhiCalendar
import clwater.metaphysics.utils.monthIndexGanZhiCalendar
import org.jetbrains.ktor.application.ApplicationCall
import java.text.SimpleDateFormat
import java.util.*

object IndexServer{
    lateinit var map : Map<String , String>
    private val data = Date()
    private var year  = String()
    private var month = String()
    private var day = String()
    private var week = String()
    private var lunarTiangan = String()
    private var lunarDizhi = String()
    private var lunarMonth = String()
    private var lunarDay = String()
    private var lunarYear = String()
    private var zhYear = String()
    private var zhMonth = String()
    private var zhDay = String()
    private var lastFestival = String()
    private var netxFestival = String()
    private var zodiac = String()
    private var dateWithFestival = String()
    private var ganZhiMonth = String()
    private var ganZhiDay = String()
    private var dayIndex = 0

    fun initDate(){
        println("check date")
        year = SimpleDateFormat("yyyy").format(data)
        month = SimpleDateFormat("MM").format(data)
        day = SimpleDateFormat("dd").format(data)

        println(year + month + day + week)

//        val c = ChineseCalendar(1895 , month.toInt() , 10)
        val c = ChineseCalendar(year.toInt() , month.toInt() , day.toInt())
        dateWithFestival = c.getChinese(ChineseCalendar.CHINESE_TERM_OR_DATE)
        lunarTiangan = c.getChinese(ChineseCalendar.CHINESE_HEAVENLY_STEM)
        lunarDizhi = c.getChinese(ChineseCalendar.CHINESE_EARTHLY_BRANCH)
        zodiac = c.getChinese(ChineseCalendar.CHINESE_ZODIAC)
        lunarYear = c.getChinese(ChineseCalendar.CHINESE_YEAR)
        lunarMonth = c.getChinese(ChineseCalendar.CHINESE_MONTH)
        lunarDay = c.getChinese(ChineseCalendar.CHINESE_DATE)
        lastFestival = c.getChinese(ChineseCalendar.CHINESE_SECTIONAL_TERM)
        netxFestival = c.getChinese(ChineseCalendar.CHINESE_PRINCIPLE_TERM)

        zhYear = getChineseNumber(year)
        zhMonth = lunarMonth
        zhDay = lunarDay

        week = getWeekToday()
        ganZhiMonth = GanZhiCalendar.getGanZhiMonth(year.toInt() , c.getChinese(ChineseCalendar.CHINESE_MONTH_NUMBER).toInt() - 1)
        ganZhiDay =  GanZhiCalendar.ganZhiDay(year.toInt() , month.toInt() , day.toInt())

        dayIndex = dayIndexGanZhiCalendar * monthIndexGanZhiCalendar % 64

        val gua = listGua[dayIndex]
        println(gua)
    }

    fun getWeekToday(): String {
        val chineseWeekNames = arrayOf( "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = IndexServer.data
        var index: Int = calendar.get(Calendar.DAY_OF_WEEK) - 1
        if (index < 0) {
            index = 0
        }
        return chineseWeekNames[index]
    }



    fun getChineseNumber(text: String): String{
        val texts = text.toCharArray()
        var chineseNumber = String()
        for (baseChar in texts){
            when (baseChar){
                '0' -> chineseNumber += "〇"
                '1' -> chineseNumber += "一"
                '2' -> chineseNumber += "二"
                '3' -> chineseNumber += "三"
                '4' -> chineseNumber += "四"
                '5' -> chineseNumber += "五"
                '6' -> chineseNumber += "六"
                '7' -> chineseNumber += "七"
                '8' -> chineseNumber += "八"
                '9' -> chineseNumber += "九"
            }
        }
        return chineseNumber
    }


    fun initView(call: ApplicationCall): Map<Any, Any> {
        initDate()

        return initMao(call)
    }


    private fun initMao(call: ApplicationCall): Map<Any, Any> {
        return mapOf("year" to year ,
                "month" to month ,
                "day" to day ,
                "week" to week ,
                "zhYear" to zhYear  ,
                "zhMonth" to zhMonth ,
                "zhDay" to zhDay ,
                "dateWithFestival" to dateWithFestival,
                "lunarTiangan" to lunarTiangan,
                "lunarDizhi" to lunarDizhi,
                "zodiac" to zodiac  ,
                "lunarYear" to lunarYear,
                "lunarMonth" to lunarMonth,
                "lunarDay" to lunarDay,
                "lastFestival" to lastFestival,
                "netxFestival" to netxFestival,
                "ganZhiMonth" to ganZhiMonth ,
                "ganZhiDay" to ganZhiDay
        )
    }
}