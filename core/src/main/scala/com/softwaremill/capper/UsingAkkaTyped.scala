package com.softwaremill.capper

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}

import scala.concurrent.{Await, Future, Promise}
import concurrent.duration._
import scala.io.Source
import scala.util.Random

object UsingAkkaTyped {

  class AkkaTypedCappingInfo(actorSystem: ActorSystem[CappingMsg]) {
    def getInfo(id: String): Future[Option[Int]] = {
      val p = Promise[Option[Int]]
      actorSystem ! GetInfo(id, p)
      p.future
    }

    def update(): Unit = actorSystem ! Update

    def stop(): Unit = {
      val _ = Await.ready(actorSystem.terminate(), 10 seconds)
      ()
    }
  }

  object AkkaTypedCappingInfo {
    def create(defaultMap: Map[String, Int]): AkkaTypedCappingInfo = {
      val bhv = Behaviors.withTimers[CappingMsg]( timer =>
        capInfo(timer, CappingInfo(defaultMap))
      )
      new AkkaTypedCappingInfo(ActorSystem(bhv, "akka-typed-capping-info"))
    }

    private def capInfo(timer: TimerScheduler[CappingMsg], data: CappingInfo): Behavior[CappingMsg] = {
      Behaviors.receiveMessage {
        case GetInfo(id, p) =>
          p.success(data.map.get(id))
          capInfo(timer, data)
        case Update =>
          println("received")
          val newInfo = loadInfo()
          capInfo(timer, newInfo)
      }
    }

    private def loadInfo(): CappingInfo = {
      val str = Source.fromFile("info.txt").getLines().mkString
      println(str)

      CappingInfo((1 to 10).map(i => i.toString -> Random.nextInt(i)).toMap)
    }
  }


  private sealed trait CappingMsg
  private case class GetInfo(id: String, p: Promise[Option[Int]]) extends CappingMsg
  private case object Update extends CappingMsg

}
