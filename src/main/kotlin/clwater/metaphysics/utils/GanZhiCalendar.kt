package clwater.metaphysics.utils

object GanZhiCalendar{
    val zhiDataMonth = arrayOf("寅","卯","辰","巳","午","未","申","酉","戌","亥", "子","丑")
    val zhiDataDay = arrayOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")
    val ganData = arrayOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    val baseMonth = arrayOf(0,31,-1,30,0,31,1,32,3,33,4,34)
    var correctionIndex = 0


    fun getGanZhiMonth(year: Int , _month: Int): String{
        val month = _month - 1
        var ganZhiMonth = String()
        var gan = ( (year % 5) - 2) * 2 - 2
        if (gan < 0 ) {
            gan += 10
        }
        when(gan){
            0,5-> correctionIndex=-8
            1,6-> correctionIndex=-6
            2,7-> correctionIndex=-4
            3,8-> correctionIndex=-2
            4,9-> correctionIndex=0
        }

        var ganIndex = month +correctionIndex
        if (ganIndex < 0){
            ganIndex += 10
        }
        ganZhiMonth = ganData[ganIndex] + zhiDataMonth[month]

        return ganZhiMonth
    }


    fun ganZhiDay(year: Int , month: Int , day: Int): String{
        var ganZhiDay = String()
        val c = year / 100
        val y = year % 100
        var m = month
        val d = day
        var dayIndex = 0;
        var G = 0
        G = 4 * c + ( c / 4 ).toInt() + 5 * y + ( y / 4 ).toInt() + (3 * ( m + 1) / 5).toInt() + d + 3

        var Z = 0
        if (month < 3){
            m += 12
        }
        if (month % 2 == 0){
            dayIndex = 6
        }
        Z = 8 * c + ( c / 4).toInt() + 5 * y + ( y / 4 ).toInt() + (3 * ( m + 1) / 5).toInt() + d + 7 + dayIndex
        println(G % 10)
        println(Z % 12)

        ganZhiDay = ganData[G % 10 - 1] + zhiDataDay[Z % 12 - 1]

        return ganZhiDay

    }
}