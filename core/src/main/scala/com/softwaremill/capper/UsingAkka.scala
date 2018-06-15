package com.softwaremill.capper

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}

import scala.concurrent.Future
import scala.util.Random
import akka.pattern.ask
import akka.util.Timeout

import concurrent.ExecutionContext.Implicits.global
import concurrent.duration._
import scala.io.Source

object UsingAkka {

  class AkkaCappingInfo(cappingInfoActor: ActorRef) {
    implicit val timeout: Timeout = 5 seconds

    def getInfo(id: String): Future[Option[Int]] = {
      (cappingInfoActor ? GetInfo(id)).mapTo[Info].map(_.maybeInfo)
    }

    def update(): Unit = cappingInfoActor ! Update

    def stop(): Unit = cappingInfoActor ! PoisonPill
  }

  object AkkaCappingInfo {
    def create(defaultInfo: Map[String, Int] = Map())(implicit system: ActorSystem): AkkaCappingInfo = {
      val ref = system.actorOf(Props(new CappingInfoActor(CappingInfo(defaultInfo))))
      new AkkaCappingInfo(ref)
    }
  }

  private class CappingInfoActor(defaultInfo: CappingInfo) extends Actor {
    private var info: CappingInfo = defaultInfo

    override def receive = {
      case GetInfo(id) => sender ! Info(info.map.get(id))
      case Update =>
        println("received")
        info = loadInfo
    }

    private def loadInfo(): CappingInfo = {
      val str = Source.fromFile("info.txt").getLines().mkString
      println(str)

      CappingInfo((1 to 10).map(i => i.toString -> Random.nextInt(i)).toMap)
    }
  }

  private case class GetInfo(id: String)
  private case object Update
  private case class Info(maybeInfo: Option[Int])
}
