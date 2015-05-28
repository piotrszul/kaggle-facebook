package name.szul.edu.kaggle.tests

object Tests {

  def main(args:Array[String]) = {
    
     println("Hello")
     val adam = "piotr"
     
     println(s"Cheers: $adam")
     println(new StringContext("hello $adam").s("hello $adam", adam));
  }
}