package com.softwaremill.capper

import cats.effect.Fiber
import com.typesafe.scalalogging.StrictLogging
import monix.eval.{MVar, Task}

import scala.util.Random

object UsingMonix {
  class MonixCappingInfo(queue: MVar[CappingMsg], queueFiber: Fiber[Task, Unit]) {
    def getInfo(id: String): Task[Option[Int]] = {
      for {
        mv <- MVar.empty[Option[Int]]
        _ <- queue.put(GetInfo(id, x => {
          println("f kicked")
          x.flatMap(mv.put).fork
          Task.apply(())
        }))
        r <- { println("mv must be taken"); mv.take }
      } yield {
        println("res is " + r)
        r
      }
    }

    def update(): Task[Unit] = {
      for {
        mv <- MVar.empty[Unit]
        _ <- queue.put(Update)
        r <- mv.take
      } yield r
    }

    def stop(): Task[Unit] = {
      queueFiber.cancel
    }
  }

  object MonixCappingInfo extends StrictLogging {
    def create(defaultMap: Map[String, Int]): Task[MonixCappingInfo] =
      for {
        queue <- MVar.empty[CappingMsg]
        runQueueFiber <- runQueue(CappingInfo(defaultMap), queue)
          .doOnCancel(Task.eval(logger.info("Stopping capper")))
          .fork
      } yield new MonixCappingInfo(queue, runQueueFiber)

    private def runQueue(data: CappingInfo, queue: MVar[CappingMsg]): Task[Unit] = {

      def loadInfo = {
        CappingInfo((1 to 10).map(i => i.toString -> Random.nextInt(i)).toMap)
      }

      queue
        // (1) take a message from the queue (or wait until one is available)
        .take
        // (2) modify the data structure accordingly
        .map {
        case GetInfo(id, f) =>
          println("get info received")
          f(Task(data.map.get(id)))
          data
        case Update => loadInfo
      }
        // (3) recursive call to handle the next message,
        // using the updated data structure
        .flatMap(d => runQueue(d, queue))
    }
  }

  private sealed trait CappingMsg
  private case class GetInfo(id: String, f: Task[Option[Int]] => Task[Unit]) extends CappingMsg
  private case object Update extends CappingMsg
}
