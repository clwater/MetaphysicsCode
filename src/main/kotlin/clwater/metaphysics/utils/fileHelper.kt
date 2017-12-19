package clwater.metaphysics.utils

import clwater.metaphysics.model.Gua
import java.io.File

fun getListFromFile():MutableList<Gua>{
    var listGua :MutableList<Gua> = ArrayList()
    val currentDir = System.getProperty("user.dir") + "/file"
    val file = File(currentDir, "baseInfo")

    println(file.readText())

    return listGua
}