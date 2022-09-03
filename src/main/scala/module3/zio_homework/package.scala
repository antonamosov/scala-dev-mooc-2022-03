package module3

import module3.zio_homework.doWhile
import zio.{Has, IO, Task, ULayer, URIO, ZIO, ZLayer}
import zio.clock.{Clock, sleep}
import zio.console._
import zio.duration.durationInt
import zio.macros.accessible
import zio.random._

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.io.StdIn
import scala.language.postfixOps

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в когнсоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */

  lazy val guessProgram: ZIO[Random with Clock with Console, Nothing, Unit] = for {
    console <- ZIO.environment[Console].map(_.get)
    random <- ZIO.environment[Random].map(_.get)
    start <- ZIO.succeed(1)
    end <- ZIO.succeed(3)
    _ <- console.putStrLn(s"Guess the number from $start to $end:")
    systemNumber <- random.nextIntBetween(start, end + 1)
    clientNumber <- console.getStrLn.orDie
    message <- {
      if (systemNumber == clientNumber.toInt) ZIO.succeed("Guessed right.")
      else ZIO.succeed(s"Guessed wrong. Right answer is ${systemNumber.toString}")
    }
    _ <- console.putStrLn(message)
  } yield ()

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   * 
   */

  def doWhile[R](effect: ZIO[R, Nothing, Boolean]): ZIO[R, Nothing, Boolean] = for {
    isTrue <- effect
    _ <- {
      if (isTrue) ZIO.succeed(isTrue)
      else doWhile(effect)
    }
  } yield isTrue


  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */

  def loadConfigOrDefault: ZIO[Any, Throwable, Unit] = for {
    config <- config.load orElse ZIO.succeed(config.AppConfig("Default app name", "https://default_url"))
    _ <- ZIO.effect(println(config.toString))
  } yield ()


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff: ZIO[Clock with Random, NoSuchElementException, Int] = for {
    random <- ZIO.environment[Random].map(_.get)
    _ <- ZIO.sleep(1 seconds)
    number <- random.nextIntBetween(0, 11)
  } yield number

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  lazy val effects: List[ZIO[Clock with Random, NoSuchElementException, Int]] =
    Range(0, 10).toList.map(_ => eff)


  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекци "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  lazy val app: ZIO[Console with Clock with Random, Throwable, Int] = for {
    list <- ZIO.foreachPar(effects) { effect => effect }
    sum <- ZIO.succeed(list.sum)
    _ <- ZIO.effect(println(sum))
  } yield sum


  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  // Программа 4.3 уже выполняется 1-2 сек., подразумеваю, что это благодаря использованию foreachPar()
  lazy val appSpeedUp = ???

  lazy val appSpeedDown: ZIO[Clock with Random, Throwable, Int] = for {
    sum <- effects.fold(ZIO.succeed(0))((a, b) => for {
        v1 <- a
        v2 <- b
        acc <- ZIO.succeed(v1 + v2)
      } yield acc)
    _ <- ZIO.effect(println(sum))
  } yield sum


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * молжно было использовать аналогично zio.console.putStrLn например
   */

  type EffectRunningTimeService = Has[EffectRunningTimeService.Service]

  @accessible
  object EffectRunningTimeService {
    trait Service {
      def print[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A]
    }

    val live: ULayer[EffectRunningTimeService] = ZLayer.succeed(new Service {
      override def print[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A] =
        zioConcurrency.printEffectRunningTime(zio)
    })
  }

   /**
     * 6.
     * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
     *
     * 
     */

  lazy val appWithTimeLogg: ZIO[Console with Clock with Random with EffectRunningTimeService, Throwable, Int] =
    EffectRunningTimeService.print(app)

  /**
    * 
    * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
    */

  lazy val appEnv: ZLayer[Any, Nothing, EffectRunningTimeService] = EffectRunningTimeService.live

  lazy val runApp: ZIO[Console with Clock with Random, Throwable, Int] =
    appWithTimeLogg.provideSomeLayer[Console with Clock with Random](appEnv)
  
}
