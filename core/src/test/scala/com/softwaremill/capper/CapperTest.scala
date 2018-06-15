package com.softwaremill.capper

import monix.eval.MVar
import monix.execution.Scheduler.Implicits.global
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

trait CapperTest extends Matchers with WordSpecLike with Eventually with ScalaFutures {


  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10 seconds, 100 millis)

  def doTest(name: String, create: Map[String, Int] => InfoCapper): Unit = {
    name should {
      "work with empty capping info" in {
        val capper = create(Map())
        capper.getInfo("1").futureValue shouldBe None
        capper.getInfo("2").futureValue shouldBe None

        capper.stop()
      }

      "update info" in {
        val capper = create(Map("1" -> 1, "2" -> 2))

        capper.getInfo("1").futureValue shouldBe Some(1)
        capper.getInfo("2").futureValue shouldBe Some(2)
        capper.getInfo("3").futureValue shouldBe None

        capper.stop()
      }

      "update capping info" in {
        val capper = create(Map())
        capper.update()

        eventually {
          (1 to 10).foreach(i => capper.getInfo(i.toString).futureValue shouldBe defined)
        }

        //        val map1 = (1 to 10).map(i => capper.getInfo(i.toString).futureValue.get)

        (1 to 10) foreach { i =>
          println(s"$i iteration")
          capper.update()
          Thread.sleep(2000)
          //          val res = capper.getInfo(i.toString)
          //          res onComplete {
          //            case Success(r) => println(r)
          //            case _ => println("failure")
          //          }
        }
        //        eventually {
        //          (1 to 10).foreach(i => capper.getInfo(i.toString).futureValue shouldBe defined)
        //        }
        //
        //        val map2 = (1 to 10).map(i => capper.getInfo(i.toString).futureValue.get)
        //        map1 shouldNot be(map2)
      }
    }
  }


  trait InfoCapper {
    def getInfo(id: String): Future[Option[Int]]
    def update(): Unit
    def stop(): Unit
  }

}
