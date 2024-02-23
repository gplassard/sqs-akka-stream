package fr.gplassard.sqsakkastream

import akka.actor.ActorSystem
import akka.event.Logging.{ErrorLevel, WarningLevel}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{Attributes, Materializer}
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{DeleteMessageRequest, GetQueueUrlRequest, Message, ReceiveMessageRequest}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.CollectionConverters.*
import scala.jdk.FutureConverters.*

@main def main(args: String*): Unit = {
  println("start")
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContext = system.dispatcher

  val sqsClient = SqsAsyncClient.create()
  val queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("sqs-akka-stream").build()).get().queueUrl()
  val baseRequest = ReceiveMessageRequest.builder()
    .queueUrl(queueUrl)
    .maxNumberOfMessages(10)
    .visibilityTimeout(30)
    .waitTimeSeconds(10)
    .build()

  val source = Source
    .unfoldAsync(baseRequest) { request =>
      sqsClient.receiveMessage(request).asScala.map(response => Some(request, response))
    }
    .mapConcat(_.messages().asScala)

  val log = Flow[Message].log("message")

  val deleteMessage = Flow[Message].mapAsyncUnordered(10) { message =>
    sqsClient
      .deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build())
      .asScala
      .map(response => (response, message))
  }

  val sink = Sink.foreach(println)

  val runnable = source
    .takeWithin(20.seconds)
    .via(log)
    .via(deleteMessage)
    .log("first-delete")
    .map(_._2)
    .via(deleteMessage)
    .log("second-delete")
    .toMat(sink)(Keep.right)
    .withAttributes(Attributes.logLevels(
      onElement = WarningLevel,
      onFinish = WarningLevel,
      onFailure = ErrorLevel
    ))
    .run()

  runnable.onComplete(res => {
    println(s"Complete $res")
    system.terminate()
  })
  Await.ready(runnable, 30.seconds)
  println("end")
}
