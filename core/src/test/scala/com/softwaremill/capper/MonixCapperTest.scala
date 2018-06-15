package com.softwaremill.capper

import scala.concurrent.duration.Duration
import monix.execution.Scheduler.Implicits.global

class MonixCapperTest extends CapperTest {

  doTest("monix-capping-info",
    defaultMap =>
    new InfoCapper {
      val aci = UsingMonix.MonixCappingInfo.create(defaultMap).runSyncUnsafe(Duration.Inf)
      override def getInfo(id: String) = aci.getInfo(id).runAsync
      override def update(): Unit = aci.update().runAsync
      override def stop(): Unit = aci.stop().runSyncUnsafe(Duration.Inf)
    }
  )
}
