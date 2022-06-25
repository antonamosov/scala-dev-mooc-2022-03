package module1

object App {
  def main(args: Array[String]): Unit = {

//    //val two = (x: Int) => { return x; 2 }
//    def sumItUp: Int = {
//      def one(x: Int): Int = { return x; 1 }
//      def two(x: Int): Int = { return x; 2 }
//      1 + one(2) + two(11)
//    }
//
//    println(sumItUp)
//
//    println("Hello world")

    def doomy(a: String): Unit = {
      Thread.sleep(1000)
      println(a)
    }

    val r = hof.logRunningTime(doomy)

    r("Hello")
    //println(hof.logRunningTime(doomy()))

    val a = list.List(1, 2, 3)
    val aa = list.List("1", "2", "3")
    val b = list.incList(a)
    val c = b.mkString(", ")
    val d = list.shoutString(aa)

    println(c)
    println(a.mkString(", "))
    println(d.mkString(", "))
  }
}
