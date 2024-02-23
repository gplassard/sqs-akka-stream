package fr.gplassard.sqsakkastream

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.ExecutionContext

@main def main(args: String*): Unit = {
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContext = system.dispatcher

  val source = Source(1 to 10)
  val sink = Sink.fold[Int, Int](0)(_ + _)
  val runnable = source.toMat(sink)(Keep.right).run()

  runnable.onComplete(res => {
    println(s"Complete $res")
    system.terminate()
  })
}
