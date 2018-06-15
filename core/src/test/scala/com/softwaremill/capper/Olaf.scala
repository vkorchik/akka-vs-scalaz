package com.softwaremill.capper

import monix.eval.{MVar, Task}
import monix.execution.Scheduler.Implicits.global
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class Olaf extends Matchers with WordSpecLike with Eventually with ScalaFutures {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10 seconds, 100 millis)

  class MapHolder(defaultMap: Map[String, Int] = Map()) {
    private val mv = MVar(defaultMap).runSyncUnsafe(Duration.Inf)

    def getCur(id: String): Future[Option[Int]] = mv.read.map(_.get(id)).runAsync

    def update(): Unit = {
      val newMap = (1 to 10).map(i => i.toString -> Random.nextInt(i)).toMap
      mv.take.flatMap(_ => mv.put(newMap))
    }.runAsync
  }

  "asd" should {
    "asd" in {

      val holder = new MapHolder()
      holder.getCur("1").futureValue shouldBe None
      holder.getCur("2").futureValue shouldBe None
      holder.getCur("3").futureValue shouldBe None

      holder.update()
      eventually {
        holder.getCur("1").futureValue shouldBe defined
        holder.getCur("2").futureValue shouldBe defined
        holder.getCur("3").futureValue shouldBe defined
      }
    }
  }

}
