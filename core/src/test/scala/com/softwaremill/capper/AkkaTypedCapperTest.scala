package com.softwaremill.capper

class AkkaTypedCapperTest extends CapperTest {

  doTest("akka-typed-capping-info",
    defaultMap =>
    new InfoCapper {
      val aci = UsingAkkaTyped.AkkaTypedCappingInfo.create(defaultMap)
      override def getInfo(id: String) = aci.getInfo(id)
      override def update(): Unit = aci.update()
      override def stop(): Unit = aci.stop()
    }
  )
}
