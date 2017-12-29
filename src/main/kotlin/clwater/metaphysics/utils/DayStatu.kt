package clwater.metaphysics.utils

import java.util.*
import kotlin.collections.ArrayList

val list = arrayOf("写代码" , "改代码" , "出设计" , "改设计" , "出原型" , "改原型" , "加班"  , "开会" , "卖单" , "叫外卖"
        , "mearge" , "review" , "大括号换行" , "大括号不换行" , "tab缩进" , "空格缩进" , "和产品撕" , "和设计撕" , "用小键盘")

var templist: MutableList<String> = ArrayList()


fun getGood(index: Int): MutableList<String> {
    templist = list.toMutableList()

    var seed = index
    val random = Random(seed.toLong())


    var texts: MutableList<String> = ArrayList()

    val textNumber = random.nextInt(2) + 1

    for (i in 0..textNumber){
        val textsNumber = random.nextInt(templist.size)
        texts.add(templist[textsNumber])
        templist.removeAt(textsNumber)
    }
//    println(texts)
    return texts

}

fun getBad(index: Int): MutableList<String> {

    var seed = index
    val random = Random(seed.toLong())

    var texts: MutableList<String> = ArrayList()

    val textNumber = random.nextInt(2) + 1

    for (i in 0..textNumber){
        val textsNumber = random.nextInt(templist.size - 1)
        texts.add(templist[textsNumber])
        templist.removeAt(textsNumber)
    }
    println(texts)
    return texts
}