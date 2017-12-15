package clwater.metaphysics.utils

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * 农历日历。<br></br>
 * 将农历从1901年到2100年之间各年、月的大小以及历年节气保存，然后基于这些数据进行计算。<br></br>
 * <br></br>
 * 新增了几个用于农历的常量属性字段，可以使用get()方法获取日历对应的值；<br></br>
 * 农历年、月、日还可以使用set()/add()/roll()方法设置，其他农历属性自动计算；<br></br>
 * 另外，还提供了getChinese(int field)方法用于获得农历的中文文字（仅适用于农历属性和星期）。<br></br>
 *
 *  * CHINESE_YEAR - 农历年
 *  * CHINESE_MONTH - 农历月
 *  * CHINESE_DATE - 农历日
 *  * CHINESE_SECTIONAL_TERM - 当月的节气
 *  * CHINESE_PRINCIPLE_TERM - 当月的中气
 *  * CHINESE_HEAVENLY_STEM - 农历年的天干
 *  * CHINESE_EARTHLY_BRANCH - 农历年的地支
 *  * CHINESE_ZODIAC - 农历年的属相
 *  * CHINESE_TERM_OR_DATE - 如果当天存在一个节气则指示节气，否则如果当天是初一则指示农历月，否则指示农历日
 *
 * 注意：<br></br>
 * 由于Calendar类的设定，公历月份从0起始。所有方法都遵循了这一约定。<br></br>
 * 但所有的农历属性从1起始。即使是在Calendar提供的方法中，农历月也是从1起始的，并以负数表示闰月。<br></br>
 * clear()方法在某些情况下会导致农历和公历日期不对应或是不能达到预期的重置效果，应尽量避免使用。<br></br>
 * 使用getSimpleDateString()获得公历日期字符串时，公历月已经修正；<br></br>
 * 使用getSimpleChineseDateString()获得农历日期字符串时，农历闰月以*表示。<br></br>
 * <br></br>
 * *农历算法来源于[和荣笔记](http://www.herongyang.com/year_gb/program.html)。*
 *
 * @author [Huxi](http://www.cnblogs.com/huxi/)
 * @version 0.12 2011-9-5 <br></br>
 * <blockquote>修复一个当使用农历正月日期初始化日历时陷入死循环的问题。</blockquote>
 * @version 0.11 2009-12-27 <br></br>
 * <blockquote>修复了获取中文农历时未计算农历日期的问题；<br></br>
 * 加入一个字段CHINESE_TERM_OR_DATE用于模仿台历的显示方式：如果当天有节气则指示节气，如果是初一指示农历月，
 * 否则指示农历日。</blockquote>
 * @version 0.10 2009-12-22
 */
class ChineseCalendar : GregorianCalendar {

    private var chineseYear: Int = 0
    private var chineseMonth: Int = 0 // 1起始，负数表示闰月
    private var chineseDate: Int = 0
    private var sectionalTerm: Int = 0 // 当月节气的公历日
    private var principleTerm: Int = 0 // 当月中气的公历日

    private var areChineseFieldsComputed: Boolean = false // 农历日期是否已经经过计算确认
    private var areSolarTermsComputed: Boolean = false // 节气是否已经经过计算确认
    private var lastSetChinese: Boolean = false // 最后设置的是不是农历属性

    /** 使用当前时间构造一个实例。  */
    constructor() : super() {}

    /** 使用指定时间构造一个实例。  */
    constructor(d: Date) {
        super.setTime(d)
    }

    /** 使用指定时间构造一个实例。  */
    constructor(c: Calendar) : this(c.time) {}

    /** 使用指定公历日期构造一个实例。  */
    constructor(y: Int, m: Int, d: Int) : super(y, m, d) {}

    /**
     * 使用指定日期构造一个实例。
     *
     * @param isChinese
     * 是否为农历日期
     * @param y
     * @param m
     * @param d
     */
    constructor(isChinese: Boolean, y: Int, m: Int, d: Int) {
        if (isChinese) {
            set(CHINESE_YEAR, y)
            set(CHINESE_MONTH, m)
            set(CHINESE_DATE, d)
        } else {
            set(y, m, d)
        }
    }

    override fun set(field: Int, value: Int) {
        computeIfNeed(field)

        if (isChineseField(field)) {
            // 农历属性
            when (field) {
                CHINESE_YEAR -> chineseYear = value
                CHINESE_MONTH -> chineseMonth = value
                CHINESE_DATE -> chineseDate = value
                else -> throw IllegalArgumentException("不支持的field设置：" + field)
            }
            lastSetChinese = true
        } else {
            // 非农历属性
            super.set(field, value)
            lastSetChinese = false
        }
        areFieldsSet = false
        areChineseFieldsComputed = false
        areSolarTermsComputed = false
    }

    override fun get(field: Int): Int {
        computeIfNeed(field)

        if (!isChineseField(field)) {
            return super.get(field)
        }

        when (field) {
            CHINESE_YEAR -> return chineseYear
            CHINESE_MONTH -> return chineseMonth
            CHINESE_DATE -> return chineseDate
            CHINESE_SECTIONAL_TERM -> return sectionalTerm
            CHINESE_PRINCIPLE_TERM -> return principleTerm
            CHINESE_HEAVENLY_STEM -> return (chineseYear - 4) % 10 + 1
            CHINESE_EARTHLY_BRANCH, CHINESE_ZODIAC -> return (chineseYear - 4) % 12 + 1
            CHINESE_TERM_OR_DATE -> {
                val option: Int
                if (get(Calendar.DATE) == get(CHINESE_SECTIONAL_TERM)) {
                    option = CHINESE_SECTIONAL_TERM
                } else if (get(Calendar.DATE) == get(CHINESE_PRINCIPLE_TERM)) {
                    option = CHINESE_PRINCIPLE_TERM
                } else if (get(CHINESE_DATE) == 1) {
                    option = CHINESE_MONTH
                } else {
                    option = CHINESE_DATE
                }
                return option
            }
            else -> throw IllegalArgumentException("不支持的field获取：" + field)
        }
    }

    override fun add(field: Int, amount: Int) {
        computeIfNeed(field)

        if (!isChineseField(field)) {
            super.add(field, amount)
            lastSetChinese = false
            areChineseFieldsComputed = false
            areSolarTermsComputed = false
            return
        }

        when (field) {
            CHINESE_YEAR -> chineseYear += amount
            CHINESE_MONTH -> for (i in 0..amount - 1) {
                chineseMonth = nextChineseMonth(chineseYear, chineseMonth)
                if (chineseMonth == 1) {
                    chineseYear++
                }
            }
            CHINESE_DATE -> {
                var maxDate = daysInChineseMonth(chineseYear, chineseMonth)
                for (i in 0..amount - 1) {
                    chineseDate++
                    if (chineseDate > maxDate) {
                        chineseDate = 1
                        chineseMonth = nextChineseMonth(chineseYear, chineseMonth)
                        if (chineseMonth == 1) {
                            chineseYear++
                        }
                        maxDate = daysInChineseMonth(chineseYear, chineseMonth)
                    }
                }
                throw IllegalArgumentException("不支持的field：" + field)
            }
            else -> throw IllegalArgumentException("不支持的field：" + field)
        }

        lastSetChinese = true
        areFieldsSet = false
        areChineseFieldsComputed = false
        areSolarTermsComputed = false
    }

    override fun roll(field: Int, amount: Int) {
        computeIfNeed(field)

        if (!isChineseField(field)) {
            super.roll(field, amount)
            lastSetChinese = false
            areChineseFieldsComputed = false
            areSolarTermsComputed = false
            return
        }

        when (field) {
            CHINESE_YEAR -> chineseYear += amount
            CHINESE_MONTH -> for (i in 0..amount - 1) {
                chineseMonth = nextChineseMonth(chineseYear, chineseMonth)
            }
            CHINESE_DATE -> {
                val maxDate = daysInChineseMonth(chineseYear, chineseMonth)
                for (i in 0..amount - 1) {
                    chineseDate++
                    if (chineseDate > maxDate) {
                        chineseDate = 1
                    }
                }
                throw IllegalArgumentException("不支持的field：" + field)
            }
            else -> throw IllegalArgumentException("不支持的field：" + field)
        }

        lastSetChinese = true
        areFieldsSet = false
        areChineseFieldsComputed = false
        areSolarTermsComputed = false
    }

    /**
     * 获得属性的中文，可以使用的属性字段为DAY_OF_WEEK以及所有农历属性字段。
     *
     * @param field
     * @return
     */
    fun getChinese(field: Int): String {
        computeIfNeed(field)

        when (field) {
            CHINESE_YEAR -> return getChinese(CHINESE_HEAVENLY_STEM) + getChinese(CHINESE_EARTHLY_BRANCH) + "年"
            CHINESE_MONTH -> return if (chineseMonth > 0)
                                chineseMonthNames[chineseMonth] + "月"
                            else
                                "闰" + chineseMonthNames[-chineseMonth] + "月"
            CHINESE_DATE -> return chineseDateNames[chineseDate]
            CHINESE_SECTIONAL_TERM -> return sectionalTermNames[get(Calendar.MONTH)]
            CHINESE_PRINCIPLE_TERM -> return principleTermNames[get(Calendar.MONTH)]
            CHINESE_HEAVENLY_STEM -> return stemNames[get(field)]
            CHINESE_EARTHLY_BRANCH -> return branchNames[get(field)]
            CHINESE_ZODIAC -> return animalNames[get(field)]
            Calendar.DAY_OF_WEEK -> return chineseWeekNames[get(field)]
            CHINESE_TERM_OR_DATE -> return getChinese(get(CHINESE_TERM_OR_DATE))
            else -> throw IllegalArgumentException("不支持的field中文获取：" + field)
        }
    }

    val simpleGregorianDateString: String
        get() = StringBuffer().append(get(Calendar.YEAR)).append("/")
                .append(get(Calendar.MONTH) + 1).append("/").append(get(Calendar.DATE))
                .toString()

    val simpleChineseDateString: String
        get() = StringBuffer()
                .append(get(CHINESE_YEAR))
                .append("-")
                .append(if (get(CHINESE_MONTH) > 0)
                    "" + get(CHINESE_MONTH)
                else
                    "*" + -get(CHINESE_MONTH)).append("-")
                .append(get(CHINESE_DATE)).toString()

    val chineseDateString: String
        get() = StringBuffer().append(getChinese(CHINESE_YEAR))
                .append(getChinese(CHINESE_MONTH))
                .append(getChinese(CHINESE_DATE)).toString()

    override fun toString(): String {
        val buf = StringBuffer()
        buf.append(simpleGregorianDateString).append(" ")
                .append(getChinese(Calendar.DAY_OF_WEEK)).append(" ")
                .append(chineseDateString).append(" ")
                .append(getChinese(CHINESE_ZODIAC)).append("年 ")
                .append(get(CHINESE_SECTIONAL_TERM)).append("日")
                .append(getChinese(CHINESE_SECTIONAL_TERM)).append(" ")
                .append(get(CHINESE_PRINCIPLE_TERM)).append("日")
                .append(getChinese(CHINESE_PRINCIPLE_TERM))
        return buf.toString()
    }

    /**
     * 判断是不是农历属性
     *
     * @param field
     * @return
     */
    private fun isChineseField(field: Int): Boolean {
        when (field) {
            CHINESE_YEAR, CHINESE_MONTH, CHINESE_DATE, CHINESE_SECTIONAL_TERM, CHINESE_PRINCIPLE_TERM, CHINESE_HEAVENLY_STEM, CHINESE_EARTHLY_BRANCH, CHINESE_ZODIAC, CHINESE_TERM_OR_DATE -> return true
            else -> return false
        }
    }

    /**
     * 判断是不是与节气有关的属性
     *
     * @param field
     * @return
     */
    private fun isChineseTermsField(field: Int): Boolean {
        when (field) {
            CHINESE_SECTIONAL_TERM, CHINESE_PRINCIPLE_TERM, CHINESE_TERM_OR_DATE -> return true
            else -> return false
        }
    }

    /**
     * 如果上一次设置的与这次将要设置或获取的属性不是同一类（农历/公历），<br></br>
     * 例如上一次设置的是农历而现在要设置或获取公历，<br></br>
     * 则需要先根据之前设置的农历日期计算出公历日期。
     *
     * @param field
     */
    private fun computeIfNeed(field: Int) {
        if (isChineseField(field)) {
            if (!lastSetChinese && !areChineseFieldsComputed) {
                super.complete()
                computeChineseFields()
                areFieldsSet = true
                areChineseFieldsComputed = true
                areSolarTermsComputed = false
            }
            if (isChineseTermsField(field) && !areSolarTermsComputed) {
                computeSolarTerms()
                areSolarTermsComputed = true
            }
        } else {
            if (lastSetChinese && !areFieldsSet) {
                computeGregorianFields()
                super.complete()
                areFieldsSet = true
                areChineseFieldsComputed = true
                areSolarTermsComputed = false
            }
        }
    }

    /**
     * 使用农历日期计算出公历日期
     */
    private fun computeGregorianFields() {
        var y = chineseYear
        var m = chineseMonth
        var d = chineseDate
        areChineseFieldsComputed = true
        areFieldsSet = true
        lastSetChinese = false

        // 调整日期范围
        if (y < 1900)
            y = 1899
        else if (y > 2100)
            y = 2101

        if (m < -12)
            m = -12
        else if (m > 12)
            m = 12

        if (d < 1)
            d = 1
        else if (d > 30)
            d = 30

        val dateint = y * 10000 + Math.abs(m) * 100 + d
        if (dateint < 19001111) { // 太小
            set(1901, Calendar.JANUARY, 1)
            super.complete()
        } else if (dateint > 21001201) { // 太大
            set(2100, Calendar.DECEMBER, 31)
            super.complete()
        } else {
            if (Math.abs(m) > 12) {
                m = 12
            }
            var days = ChineseCalendar.daysInChineseMonth(y, m)
            if (days == 0) {
                m = -m
                days = ChineseCalendar.daysInChineseMonth(y, m)
            }
            if (d > days) {
                d = days
            }
            set(y, Math.abs(m) - 1, d)
            computeChineseFields()

            var amount = 0
            while (chineseYear != y || chineseMonth != m) {
                amount += daysInChineseMonth(chineseYear, chineseMonth)
                chineseMonth = nextChineseMonth(chineseYear, chineseMonth)
                if (chineseMonth == 1) {
                    chineseYear++
                }
            }
            amount += d - chineseDate

            super.add(Calendar.DATE, amount)
        }
        computeChineseFields()
    }

    /**
     * 使用公历日期计算出农历日期
     */
    private fun computeChineseFields() {
        val gregorianYear = internalGet(Calendar.YEAR)
        val gregorianMonth = internalGet(Calendar.MONTH) + 1
        val gregorianDate = internalGet(Calendar.DATE)

        if (gregorianYear < 1901 || gregorianYear > 2100) {
            return
        }

        val startYear: Int
        val startMonth: Int
        val startDate: Int
        if (gregorianYear < 2000) {
            startYear = baseYear
            startMonth = baseMonth
            startDate = baseDate
            chineseYear = baseChineseYear
            chineseMonth = baseChineseMonth
            chineseDate = baseChineseDate
        } else {
            // 第二个对应日，用以提高计算效率
            // 公历 2000 年 1 月 1 日，对应农历 4697(1999) 年 11 月 25 日
            startYear = baseYear + 99
            startMonth = 1
            startDate = 1
            chineseYear = baseChineseYear + 99
            chineseMonth = 11
            chineseDate = 25
        }

        var daysDiff = 0

        // 年
        for (i in startYear..gregorianYear - 1) {
            if (isGregorianLeapYear(i)) {
                daysDiff += 366 // leap year
            } else {
                daysDiff += 365
            }
        }

        // 月
        for (i in startMonth..gregorianMonth - 1) {
            daysDiff += daysInGregorianMonth(gregorianYear, i - 1)
        }

        // 日
        daysDiff += gregorianDate - startDate

        chineseDate += daysDiff

        var lastDate = daysInChineseMonth(chineseYear, chineseMonth)
        while (chineseDate > lastDate) {
            chineseDate -= lastDate
            chineseMonth = nextChineseMonth(chineseYear, chineseMonth)
            if (chineseMonth == 1) {
                chineseYear++
            }
            lastDate = daysInChineseMonth(chineseYear, chineseMonth)
        }

    }

    /**
     * 计算节气
     */
    private fun computeSolarTerms() {
        val gregorianYear = internalGet(Calendar.YEAR)
        val gregorianMonth = internalGet(Calendar.MONTH)

        if (gregorianYear < 1901 || gregorianYear > 2100) {
            return
        }
        sectionalTerm = sectionalTerm(gregorianYear, gregorianMonth)
        principleTerm = principleTerm(gregorianYear, gregorianMonth)
    }

    companion object {
        private val serialVersionUID = 8L

        /** 农历年  */
        val CHINESE_YEAR = 801
        /** 农历月  */
        val CHINESE_MONTH = 802
        /** 农历日  */
        val CHINESE_DATE = 803
        /** 当月的节气对应的公历日(前一个节气)  */
        val CHINESE_SECTIONAL_TERM = 804
        /** 当月的中气对应的公历日(后一个节气)  */
        val CHINESE_PRINCIPLE_TERM = 805
        /** 天干  */
        val CHINESE_HEAVENLY_STEM = 806
        /** 地支  */
        val CHINESE_EARTHLY_BRANCH = 807
        /** 农历年的属相(生肖)  */
        val CHINESE_ZODIAC = 808
        /** 节气或者农历日  */
        val CHINESE_TERM_OR_DATE = 888

        /* 接下来是静态方法~ */
        /**
         * 是否为公历闰年
         *
         * @param year
         * @return
         */
        fun isGregorianLeapYear(year: Int): Boolean {
            var isLeap = false
            if (year % 4 == 0) {
                isLeap = true
            }
            if (year % 100 == 0) {
                isLeap = false
            }
            if (year % 400 == 0) {
                isLeap = true
            }
            return isLeap
        }

        /**
         * 计算公历年的当月天数，公历月从0起始！
         *
         * @param y
         * @param m
         * @return
         */
        fun daysInGregorianMonth(y: Int, m: Int): Int {
            var d = daysInGregorianMonth[m].toInt()
            if (m == Calendar.FEBRUARY && isGregorianLeapYear(y)) {
                d++ // 公历闰年二月多一天
            }
            return d
        }

        /**
         * 计算公历年当月的节气，公历月从0起始！
         *
         * @param y
         * @param m
         * @return
         */
        fun sectionalTerm(y: Int, m: Int): Int {
            var m = m
            m++
            if (y < 1901 || y > 2100) {
                return 0
            }
            var index = 0
            val ry = y - baseYear + 1
            while (ry >= sectionalTermYear[m - 1][index].toInt()) {
                index++
            }
            var term = sectionalTermMap[m - 1][4 * index + ry % 4].toInt()
            if (ry == 121 && m == 4) {
                term = 5
            }
            if (ry == 132 && m == 4) {
                term = 5
            }
            if (ry == 194 && m == 6) {
                term = 6
            }
            return term
        }

        /**
         * 计算公历年当月的中气，公历月从0起始！
         *
         * @param y
         * @param m
         * @return
         */
        fun principleTerm(y: Int, m: Int): Int {
            var m = m
            m++
            if (y < 1901 || y > 2100) {
                return 0
            }
            var index = 0
            val ry = y - baseYear + 1
            while (ry >= principleTermYear[m - 1][index].toInt()) {
                index++
            }
            var term = principleTermMap[m - 1][4 * index + ry % 4].toInt()
            if (ry == 171 && m == 3) {
                term = 21
            }
            if (ry == 181 && m == 5) {
                term = 21
            }
            return term
        }

        /**
         * 计算农历年的天数
         *
         * @param y
         * @param m
         * @return
         */
        fun daysInChineseMonth(y: Int, m: Int): Int {
            // 注意：闰月 m < 0
            val index = y - baseChineseYear + baseIndex
            var v = 0
            var l = 0
            var d = 30
            if (1 <= m && m <= 8) {
                v = chineseMonths[2 * index].toInt()
                l = m - 1
                if (v shr l and 0x01 == 1) {
                    d = 29
                }
            } else if (9 <= m && m <= 12) {
                v = chineseMonths[2 * index + 1].toInt()
                l = m - 9
                if (v shr l and 0x01 == 1) {
                    d = 29
                }
            } else {
                v = chineseMonths[2 * index + 1].toInt()
                v = v shr 4 and 0x0F
                if (v != Math.abs(m)) {
                    d = 0
                } else {
                    d = 29
                    for (i in bigLeapMonthYears.indices) {
                        if (bigLeapMonthYears[i] == index) {
                            d = 30
                            break
                        }
                    }
                }
            }
            return d
        }

        /**
         * 计算农历的下个月
         *
         * @param y
         * @param m
         * @return
         */
        fun nextChineseMonth(y: Int, m: Int): Int {
            var n = Math.abs(m) + 1
            if (m > 0) {
                val index = y - baseChineseYear + baseIndex
                var v = chineseMonths[2 * index + 1].toInt()
                v = v shr 4 and 0x0F
                if (v == m) {
                    n = -m
                }
            }
            if (n == 13) {
                n = 1
            }
            return n
        }

        /* 日历第一天的日期 */
        private val baseYear = 1901
        private val baseMonth = 1
        private val baseDate = 1
        private val baseIndex = 0
        private val baseChineseYear = 1900
        private val baseChineseMonth = 11
        private val baseChineseDate = 11

        /* 中文字符串 */
        private val chineseWeekNames = arrayOf("", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        private val chineseMonthNames = arrayOf("", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二")
        private val chineseDateNames = arrayOf("", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
        private val principleTermNames = arrayOf("大寒", "雨水", "春分", "谷雨", "夏满", "夏至", "大暑", "处暑", "秋分", "霜降", "小雪", "冬至")
        private val sectionalTermNames = arrayOf("小寒", "立春", "惊蛰", "清明", "立夏", "芒种", "小暑", "立秋", "白露", "寒露", "立冬", "大雪")
        private val stemNames = arrayOf("", "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
        private val branchNames = arrayOf("", "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
        private val animalNames = arrayOf("", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

        /* 接下来是数据压缩表~ */
        private val bigLeapMonthYears = intArrayOf(6, 14, 19, 25, 33, 36, 38, 41, 44, 52, 55, 79, 117, 136, 147, 150, 155, 158, 185, 193)
        private val sectionalTermMap = arrayOf(charArrayOf(7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 5.toChar(), 5.toChar()), charArrayOf(5.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 3.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 3.toChar(), 3.toChar(), 4.toChar(), 4.toChar(), 3.toChar(), 3.toChar(), 3.toChar()), charArrayOf(6.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar()), charArrayOf(5.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 5.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 4.toChar(), 5.toChar()), charArrayOf(6.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar()), charArrayOf(6.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 6.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 4.toChar(), 5.toChar(), 5.toChar(), 5.toChar(), 5.toChar()), charArrayOf(7.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 7.toChar()), charArrayOf(8.toChar(), 8.toChar(), 8.toChar(), 9.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar()), charArrayOf(8.toChar(), 8.toChar(), 8.toChar(), 9.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 7.toChar()), charArrayOf(9.toChar(), 9.toChar(), 9.toChar(), 9.toChar(), 8.toChar(), 9.toChar(), 9.toChar(), 9.toChar(), 8.toChar(), 8.toChar(), 9.toChar(), 9.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 9.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 8.toChar()), charArrayOf(8.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar()), charArrayOf(7.toChar(), 8.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 8.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 7.toChar(), 6.toChar(), 6.toChar(), 6.toChar(), 7.toChar(), 7.toChar()))
        private val sectionalTermYear = arrayOf(charArrayOf(13.toChar(), 49.toChar(), 85.toChar(), 117.toChar(), 149.toChar(), 185.toChar(), 201.toChar(), 250.toChar(), 250.toChar()), charArrayOf(13.toChar(), 45.toChar(), 81.toChar(), 117.toChar(), 149.toChar(), 185.toChar(), 201.toChar(), 250.toChar(), 250.toChar()), charArrayOf(13.toChar(), 48.toChar(), 84.toChar(), 112.toChar(), 148.toChar(), 184.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(13.toChar(), 45.toChar(), 76.toChar(), 108.toChar(), 140.toChar(), 172.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(13.toChar(), 44.toChar(), 72.toChar(), 104.toChar(), 132.toChar(), 168.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(5.toChar(), 33.toChar(), 68.toChar(), 96.toChar(), 124.toChar(), 152.toChar(), 188.toChar(), 200.toChar(), 201.toChar()), charArrayOf(29.toChar(), 57.toChar(), 85.toChar(), 120.toChar(), 148.toChar(), 176.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(13.toChar(), 48.toChar(), 76.toChar(), 104.toChar(), 132.toChar(), 168.toChar(), 196.toChar(), 200.toChar(), 201.toChar()), charArrayOf(25.toChar(), 60.toChar(), 88.toChar(), 120.toChar(), 148.toChar(), 184.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(16.toChar(), 44.toChar(), 76.toChar(), 108.toChar(), 144.toChar(), 172.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(28.toChar(), 60.toChar(), 92.toChar(), 124.toChar(), 160.toChar(), 192.toChar(), 200.toChar(), 201.toChar(), 250.toChar()), charArrayOf(17.toChar(), 53.toChar(), 85.toChar(), 124.toChar(), 156.toChar(), 188.toChar(), 200.toChar(), 201.toChar(), 250.toChar()))
        private val principleTermMap = arrayOf(charArrayOf(21.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 19.toChar(), 20.toChar()), charArrayOf(20.toChar(), 19.toChar(), 19.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 18.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 18.toChar(), 18.toChar(), 19.toChar(), 19.toChar(), 18.toChar(), 18.toChar(), 18.toChar(), 18.toChar(), 18.toChar(), 18.toChar(), 18.toChar()), charArrayOf(21.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar()), charArrayOf(20.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 19.toChar(), 20.toChar(), 20.toChar(), 19.toChar(), 19.toChar(), 19.toChar(), 20.toChar(), 20.toChar()), charArrayOf(21.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 21.toChar()), charArrayOf(22.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 20.toChar(), 20.toChar(), 21.toChar(), 21.toChar(), 21.toChar()), charArrayOf(23.toChar(), 23.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 23.toChar()), charArrayOf(23.toChar(), 24.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar()), charArrayOf(23.toChar(), 24.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar()), charArrayOf(24.toChar(), 24.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 24.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 24.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 23.toChar()), charArrayOf(23.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 22.toChar()), charArrayOf(22.toChar(), 22.toChar(), 23.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 23.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 22.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 21.toChar(), 22.toChar()))
        private val principleTermYear = arrayOf(charArrayOf(13.toChar(), 45.toChar(), 81.toChar(), 113.toChar(), 149.toChar(), 185.toChar(), 201.toChar()), charArrayOf(21.toChar(), 57.toChar(), 93.toChar(), 125.toChar(), 161.toChar(), 193.toChar(), 201.toChar()), charArrayOf(21.toChar(), 56.toChar(), 88.toChar(), 120.toChar(), 152.toChar(), 188.toChar(), 200.toChar(), 201.toChar()), charArrayOf(21.toChar(), 49.toChar(), 81.toChar(), 116.toChar(), 144.toChar(), 176.toChar(), 200.toChar(), 201.toChar()), charArrayOf(17.toChar(), 49.toChar(), 77.toChar(), 112.toChar(), 140.toChar(), 168.toChar(), 200.toChar(), 201.toChar()), charArrayOf(28.toChar(), 60.toChar(), 88.toChar(), 116.toChar(), 148.toChar(), 180.toChar(), 200.toChar(), 201.toChar()), charArrayOf(25.toChar(), 53.toChar(), 84.toChar(), 112.toChar(), 144.toChar(), 172.toChar(), 200.toChar(), 201.toChar()), charArrayOf(29.toChar(), 57.toChar(), 89.toChar(), 120.toChar(), 148.toChar(), 180.toChar(), 200.toChar(), 201.toChar()), charArrayOf(17.toChar(), 45.toChar(), 73.toChar(), 108.toChar(), 140.toChar(), 168.toChar(), 200.toChar(), 201.toChar()), charArrayOf(28.toChar(), 60.toChar(), 92.toChar(), 124.toChar(), 160.toChar(), 192.toChar(), 200.toChar(), 201.toChar()), charArrayOf(16.toChar(), 44.toChar(), 80.toChar(), 112.toChar(), 148.toChar(), 180.toChar(), 200.toChar(), 201.toChar()), charArrayOf(17.toChar(), 53.toChar(), 88.toChar(), 120.toChar(), 156.toChar(), 188.toChar(), 200.toChar(), 201.toChar()))

        private val daysInGregorianMonth = charArrayOf(31.toChar(), 28.toChar(), 31.toChar(), 30.toChar(), 31.toChar(), 30.toChar(), 31.toChar(), 31.toChar(), 30.toChar(), 31.toChar(), 30.toChar(), 31.toChar())
        private val chineseMonths = charArrayOf(0x00.toChar(), 0x04.toChar(), 0xad.toChar(), 0x08.toChar(), 0x5a.toChar(), 0x01.toChar(), 0xd5.toChar(), 0x54.toChar(), 0xb4.toChar(), 0x09.toChar(), 0x64.toChar(), 0x05.toChar(), 0x59.toChar(), 0x45.toChar(), 0x95.toChar(), 0x0a.toChar(), 0xa6.toChar(), 0x04.toChar(), 0x55.toChar(), 0x24.toChar(), 0xad.toChar(), 0x08.toChar(), 0x5a.toChar(), 0x62.toChar(), 0xda.toChar(), 0x04.toChar(), 0xb4.toChar(), 0x05.toChar(), 0xb4.toChar(), 0x55.toChar(), 0x52.toChar(), 0x0d.toChar(), 0x94.toChar(), 0x0a.toChar(), 0x4a.toChar(), 0x2a.toChar(), 0x56.toChar(), 0x02.toChar(), 0x6d.toChar(), 0x71.toChar(), 0x6d.toChar(), 0x01.toChar(), 0xda.toChar(), 0x02.toChar(), 0xd2.toChar(), 0x52.toChar(), 0xa9.toChar(), 0x05.toChar(), 0x49.toChar(), 0x0d.toChar(), 0x2a.toChar(), 0x45.toChar(), 0x2b.toChar(), 0x09.toChar(), 0x56.toChar(), 0x01.toChar(), 0xb5.toChar(), 0x20.toChar(), 0x6d.toChar(), 0x01.toChar(), 0x59.toChar(), 0x69.toChar(), 0xd4.toChar(), 0x0a.toChar(), 0xa8.toChar(), 0x05.toChar(), 0xa9.toChar(), 0x56.toChar(), 0xa5.toChar(), 0x04.toChar(), 0x2b.toChar(), 0x09.toChar(), 0x9e.toChar(), 0x38.toChar(), 0xb6.toChar(), 0x08.toChar(), 0xec.toChar(), 0x74.toChar(), 0x6c.toChar(), 0x05.toChar(), 0xd4.toChar(), 0x0a.toChar(), 0xe4.toChar(), 0x6a.toChar(), 0x52.toChar(), 0x05.toChar(), 0x95.toChar(), 0x0a.toChar(), 0x5a.toChar(), 0x42.toChar(), 0x5b.toChar(), 0x04.toChar(), 0xb6.toChar(), 0x04.toChar(), 0xb4.toChar(), 0x22.toChar(), 0x6a.toChar(), 0x05.toChar(), 0x52.toChar(), 0x75.toChar(), 0xc9.toChar(), 0x0a.toChar(), 0x52.toChar(), 0x05.toChar(), 0x35.toChar(), 0x55.toChar(), 0x4d.toChar(), 0x0a.toChar(), 0x5a.toChar(), 0x02.toChar(), 0x5d.toChar(), 0x31.toChar(), 0xb5.toChar(), 0x02.toChar(), 0x6a.toChar(), 0x8a.toChar(), 0x68.toChar(), 0x05.toChar(), 0xa9.toChar(), 0x0a.toChar(), 0x8a.toChar(), 0x6a.toChar(), 0x2a.toChar(), 0x05.toChar(), 0x2d.toChar(), 0x09.toChar(), 0xaa.toChar(), 0x48.toChar(), 0x5a.toChar(), 0x01.toChar(), 0xb5.toChar(), 0x09.toChar(), 0xb0.toChar(), 0x39.toChar(), 0x64.toChar(), 0x05.toChar(), 0x25.toChar(), 0x75.toChar(), 0x95.toChar(), 0x0a.toChar(), 0x96.toChar(), 0x04.toChar(), 0x4d.toChar(), 0x54.toChar(), 0xad.toChar(), 0x04.toChar(), 0xda.toChar(), 0x04.toChar(), 0xd4.toChar(), 0x44.toChar(), 0xb4.toChar(), 0x05.toChar(), 0x54.toChar(), 0x85.toChar(), 0x52.toChar(), 0x0d.toChar(), 0x92.toChar(), 0x0a.toChar(), 0x56.toChar(), 0x6a.toChar(), 0x56.toChar(), 0x02.toChar(), 0x6d.toChar(), 0x02.toChar(), 0x6a.toChar(), 0x41.toChar(), 0xda.toChar(), 0x02.toChar(), 0xb2.toChar(), 0xa1.toChar(), 0xa9.toChar(), 0x05.toChar(), 0x49.toChar(), 0x0d.toChar(), 0x0a.toChar(), 0x6d.toChar(), 0x2a.toChar(), 0x09.toChar(), 0x56.toChar(), 0x01.toChar(), 0xad.toChar(), 0x50.toChar(), 0x6d.toChar(), 0x01.toChar(), 0xd9.toChar(), 0x02.toChar(), 0xd1.toChar(), 0x3a.toChar(), 0xa8.toChar(), 0x05.toChar(), 0x29.toChar(), 0x85.toChar(), 0xa5.toChar(), 0x0c.toChar(), 0x2a.toChar(), 0x09.toChar(), 0x96.toChar(), 0x54.toChar(), 0xb6.toChar(), 0x08.toChar(), 0x6c.toChar(), 0x09.toChar(), 0x64.toChar(), 0x45.toChar(), 0xd4.toChar(), 0x0a.toChar(), 0xa4.toChar(), 0x05.toChar(), 0x51.toChar(), 0x25.toChar(), 0x95.toChar(), 0x0a.toChar(), 0x2a.toChar(), 0x72.toChar(), 0x5b.toChar(), 0x04.toChar(), 0xb6.toChar(), 0x04.toChar(), 0xac.toChar(), 0x52.toChar(), 0x6a.toChar(), 0x05.toChar(), 0xd2.toChar(), 0x0a.toChar(), 0xa2.toChar(), 0x4a.toChar(), 0x4a.toChar(), 0x05.toChar(), 0x55.toChar(), 0x94.toChar(), 0x2d.toChar(), 0x0a.toChar(), 0x5a.toChar(), 0x02.toChar(), 0x75.toChar(), 0x61.toChar(), 0xb5.toChar(), 0x02.toChar(), 0x6a.toChar(), 0x03.toChar(), 0x61.toChar(), 0x45.toChar(), 0xa9.toChar(), 0x0a.toChar(), 0x4a.toChar(), 0x05.toChar(), 0x25.toChar(), 0x25.toChar(), 0x2d.toChar(), 0x09.toChar(), 0x9a.toChar(), 0x68.toChar(), 0xda.toChar(), 0x08.toChar(), 0xb4.toChar(), 0x09.toChar(), 0xa8.toChar(), 0x59.toChar(), 0x54.toChar(), 0x03.toChar(), 0xa5.toChar(), 0x0a.toChar(), 0x91.toChar(), 0x3a.toChar(), 0x96.toChar(), 0x04.toChar(), 0xad.toChar(), 0xb0.toChar(), 0xad.toChar(), 0x04.toChar(), 0xda.toChar(), 0x04.toChar(), 0xf4.toChar(), 0x62.toChar(), 0xb4.toChar(), 0x05.toChar(), 0x54.toChar(), 0x0b.toChar(), 0x44.toChar(), 0x5d.toChar(), 0x52.toChar(), 0x0a.toChar(), 0x95.toChar(), 0x04.toChar(), 0x55.toChar(), 0x22.toChar(), 0x6d.toChar(), 0x02.toChar(), 0x5a.toChar(), 0x71.toChar(), 0xda.toChar(), 0x02.toChar(), 0xaa.toChar(), 0x05.toChar(), 0xb2.toChar(), 0x55.toChar(), 0x49.toChar(), 0x0b.toChar(), 0x4a.toChar(), 0x0a.toChar(), 0x2d.toChar(), 0x39.toChar(), 0x36.toChar(), 0x01.toChar(), 0x6d.toChar(), 0x80.toChar(), 0x6d.toChar(), 0x01.toChar(), 0xd9.toChar(), 0x02.toChar(), 0xe9.toChar(), 0x6a.toChar(), 0xa8.toChar(), 0x05.toChar(), 0x29.toChar(), 0x0b.toChar(), 0x9a.toChar(), 0x4c.toChar(), 0xaa.toChar(), 0x08.toChar(), 0xb6.toChar(), 0x08.toChar(), 0xb4.toChar(), 0x38.toChar(), 0x6c.toChar(), 0x09.toChar(), 0x54.toChar(), 0x75.toChar(), 0xd4.toChar(), 0x0a.toChar(), 0xa4.toChar(), 0x05.toChar(), 0x45.toChar(), 0x55.toChar(), 0x95.toChar(), 0x0a.toChar(), 0x9a.toChar(), 0x04.toChar(), 0x55.toChar(), 0x44.toChar(), 0xb5.toChar(), 0x04.toChar(), 0x6a.toChar(), 0x82.toChar(), 0x6a.toChar(), 0x05.toChar(), 0xd2.toChar(), 0x0a.toChar(), 0x92.toChar(), 0x6a.toChar(), 0x4a.toChar(), 0x05.toChar(), 0x55.toChar(), 0x0a.toChar(), 0x2a.toChar(), 0x4a.toChar(), 0x5a.toChar(), 0x02.toChar(), 0xb5.toChar(), 0x02.toChar(), 0xb2.toChar(), 0x31.toChar(), 0x69.toChar(), 0x03.toChar(), 0x31.toChar(), 0x73.toChar(), 0xa9.toChar(), 0x0a.toChar(), 0x4a.toChar(), 0x05.toChar(), 0x2d.toChar(), 0x55.toChar(), 0x2d.toChar(), 0x09.toChar(), 0x5a.toChar(), 0x01.toChar(), 0xd5.toChar(), 0x48.toChar(), 0xb4.toChar(), 0x09.toChar(), 0x68.toChar(), 0x89.toChar(), 0x54.toChar(), 0x0b.toChar(), 0xa4.toChar(), 0x0a.toChar(), 0xa5.toChar(), 0x6a.toChar(), 0x95.toChar(), 0x04.toChar(), 0xad.toChar(), 0x08.toChar(), 0x6a.toChar(), 0x44.toChar(), 0xda.toChar(), 0x04.toChar(), 0x74.toChar(), 0x05.toChar(), 0xb0.toChar(), 0x25.toChar(), 0x54.toChar(), 0x03.toChar())
    }
}