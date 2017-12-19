package clwater.metaphysics.model


data class Gua(val id :Int , val name: String, val gua1: String,
               val gua2:String , val nameInfo: String,
               val guaIndex1: Int , val guaIndex2: Int , val Jie: List<Bo>,
               val statu: String , val info: String)
data class Bo(val id : Int , val name: String , val Bo: String ,
              val Xiang: String , val BoTr: String , val XiangTr: String)