package com.softwaremill.capper

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll

class AkkaCapperTest extends CapperTest with BeforeAndAfterAll {

  implicit var system: ActorSystem = _

  override protected def beforeAll(): Unit = {
    system = ActorSystem("akka-capper-info-test")
  }

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  doTest("akka-capping-info",
    defaultMap =>
    new InfoCapper {
      val aci = UsingAkka.AkkaCappingInfo.create(defaultMap)
      override def getInfo(id: String) = aci.getInfo(id)
      override def update(): Unit = aci.update()
      override def stop(): Unit = aci.stop()
    }
  )
}
