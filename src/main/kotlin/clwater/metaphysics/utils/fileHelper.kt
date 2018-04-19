package clwater.metaphysics.utils

import clwater.metaphysics.model.Bo
import clwater.metaphysics.model.Gua
import java.io.File

fun getListFromFile():MutableList<Gua>{
    var listGua :MutableList<Gua> = ArrayList()
    val currentDir = System.getProperty("user.dir") + "/file"
    val file = File(currentDir, "baseInfo")

    val lines = file.readLines()

    for (line in lines) {
//        var line = lines[2]
        val items = line.split("|")

        var BoList: MutableList<Bo> = ArrayList()
        for (index in 0..6) {
            val bo = Bo(id = items[9 + index * 6 + 0].toInt(), name = items[9 + index * 6 + 1],
                    Bo = items[9 + index * 6 + 2], Xiang = items[9 + index * 6 + 3],
                    BoTr = items[9 + index * 6 + 4], XiangTr = items[9 + index * 6 + 5])
            BoList.add(bo)
        }
        var gua = Gua(id = items[0].toInt(), name = items[1], gua1 = items[2], gua2 = items[3],
                nameInfo = items[4], guaIndex1 = items[5].toInt(), guaIndex2 = items[6].toInt(),
                statu = items[7], info = items[8], Jie = BoList
        )

        println(gua.toString())
        listGua.add(gua)

    }

    return listGua
}