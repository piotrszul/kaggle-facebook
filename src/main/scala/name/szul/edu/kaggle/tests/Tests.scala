package name.szul.edu.kaggle.tests



case class Feature(val test:Int, val version:Int = 2)  {
}

object Tests {

  def main(args:Array[String]) = {
    
     println("Hello")
     val adam = "piotr"
     
     println(s"Cheers: $adam")
     val feat = Feature(3)
     println(feat.hashCode())
     //685445846
     
     println(String.format("One: %1$s and afain %1$s", "first"))
  }
}