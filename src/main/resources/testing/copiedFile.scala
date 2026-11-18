package testing

import cats.effect.{IO, IOApp, Resource}

import java.io.{File, FileWriter, PrintWriter}
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.Random

object ScalaCatsEffect extends IOApp.Simple {

  //  1. IO
//    - data strct (DS), describing arbitrary computations (including ones with side-effects)
  val firstIO : IO[Int] = IO.pure(42)
  val delayedIO: IO[Int] = IO {
    println("This is a delayed IO")
    42
  }

  def evaluateIO[A](io: IO[A]): Unit = {
    import cats.effect.unsafe.implicits.global
    val meaningOfLife = io.unsafeRunSync()
    println(s"the result of the effect is: $meaningOfLife")
  }

  // 1.2 Transformations:
//    - mainly map & flatMap
  val improvedMeaningOfLife: IO[Int] = firstIO.map(_ * 2)
  val printedMeaningOfLife: IO[Unit] = firstIO.flatMap(x => IO(println(x)))

//  - using a "for-comp"
  def smallProgram() = for {
    line1 <-  IO(StdIn.readLine())
    line2 <-  IO(StdIn.readLine())
    _ <- IO(println(line1 + line2))
  } yield ()

//  old-style standard Scala apps
//  def main(array: Array[String]): Unit = {
//    evaluateIO(smallProgram())
//  }

//  1.3 Other capabilities
//  - raise/catch errors (equivalent to the standard try/catch mechanism)
  val aFailure = IO.raiseError(new RuntimeException("a runtime exception"))
  val dealWithFailure = aFailure.handleErrorWith {
    case _: RuntimeException  => IO(println("Error recovery proc..."))
  }

//  - fibers (lightweight threads):
//    - running fibers sequentially
  val delayedPrint = IO.sleep(1.second) *> IO(println(Random.nextInt(100)))
  val manyPrints = for {
    _ <- delayedPrint
    _ <- delayedPrint
  } yield ()

//  - running fibers in-parallel
  val manyPrints2 = for {
    fib1 <- delayedPrint.start
    fib2 <- delayedPrint.start
    _ <- fib1.join
    _ <- fib2.join
  } yield ()

//  - cancelling a fiber
  val cancelledFiber = for {
    fib1  <- delayedPrint.onCancel(IO(println("Getting cancelled"))).start
    _ <- IO.sleep(500.millis) *> IO(println("Cancelling fiber")) *> fib1.cancel
    _ <- fib1.join
  } yield ()

//  - uncancellation
  val ignoredCancel = for {
    fib1  <- IO.uncancelable(_ => delayedPrint.onCancel(IO(println("Getting cancelled")))).start
    _ <- IO.sleep(500.millis) *> IO(println("Cancelling fiber")) *> fib1.cancel
    _ <- fib1.join
  }  yield ()

//  2. Resources
//    - glorified IOs with clauses for closing up resources such as db connections/sockets
  //    when the effects are finished or cancelled from the outside
  //    (to prevent leaking resources/threads/connections/etc)
  val readResource = Resource.make(
    IO(scala.io.Source.fromFile("src/main/scala/ScalaCatsEffect.scala"))
  )(source => IO(println("closing read resource")) *> IO(source.close()))
  val readEffect = readResource.use(source => IO(source.getLines().foreach(
                                                        line => println(line))))

//  "composite" resources
  val writeResource = Resource.make(
    IO(new PrintWriter(new FileWriter(new File("src/main/resources/copiedFile.scala"))))
  )(writer => IO(println("closing copy file resource")) *> IO(writer.close()))

  val compositeResource = for {
    source <- readResource
    dest <- writeResource
  } yield (source, dest)

  val copyFileEffect = compositeResource.use(
    (source, dest) => IO(source.getLines().foreach(dest.println)))

  //  cats effect automatically evaluate the "run" method below in a "main" method
  override def run: IO[Unit] = copyFileEffect
}
