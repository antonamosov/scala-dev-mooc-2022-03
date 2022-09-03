package module3.zio_homework

import module3.zioConcurrency
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.{ExitCode, URIO}

object App {

  def main(args: Array[String]): Unit = {
    //zio.Runtime.default.unsafeRun(guessProgram)
    //zio.Runtime.default.unsafeRun(loadConfigOrDefault)
    //zio.Runtime.default.unsafeRun(eff)
    //zio.Runtime.default.unsafeRun(zioConcurrency.printEffectRunningTime(app))
    zio.Runtime.default.unsafeRun(zioConcurrency.printEffectRunningTime(appSpeedDown))

  }
}

object ZioHomeWorkApp extends zio.App {
  override def run(args: List[String]): URIO[Clock with Random with Console, ExitCode] = runApp.exitCode
}
