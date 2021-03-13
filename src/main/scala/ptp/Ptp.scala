package ptp

import synthesijer.scala._

object Ptp {

  def generate() : Module = {
    val m = new Module("ptp")

    val inData = m.inP("inData", 128)
    val inReq  = m.inP("inReq")
    val inEn   = m.inP("inEn")
    val inAck  = m.outP("inAck")

    val outData = m.outP("outData", 128)
    val outReq  = m.outP("outReq")
    val outEn   = m.outP("outEn")
    val outAck  = m.inP("outAck")

    // timestampe is represented in 80-bit
    // the second field in the timestamp is a 48-bit integer, whereas
    // the nanosecond field in the timestamp is a 32-bit integer
    val timestamp = m.signal(80)

    // PTP Header (34 bytes)
    // |  7 |  6 |  5 |  4 |  3 |  2 |  1 |  0 | oct. | off.
    // -----------------------------------------------------
    // | transportSpecific | messageType       | 1    | 0
    // | Reserved          | verstinPTP        | 1    | 1
    // | messageLength                         | 2    | 2
    // | domainNumber                          | 1    | 4
    // | Reserved                              | 1    | 5
    // | Flags                                 | 2    | 6
    // | correctionField                       | 8    | 8
    // -----------------------------------------------------
    // | Reserved                              | 4    | 16
    // | sourcePortIdentify                    | 10   | 20
    // | sequenceID                            | 2    | 30
    // -----------------------------------------------------
    // | controlField                          | 1    | 32
    // | logMessageInterval                    | 1    | 33
    val transportSpecific = m.signal(4)
    val messageType     = m.signal(4)
    val versionPTP      = m.signal(4)
    val messageLength   = m.signal(16)
    val domainNumber    = m.signal(8)
    val flags           = m.signal(16)
    val correctionField = m.signal(64)
    val sourcePortIdentify = m.signal(80)
    val sequenceID   = m.signal(16)
    val controlField = m.signal(8)
    val logMessageInterval = m.signal(8)

    // PTP Announce Message
    // |  7 |  6 |  5 |  4 |  3 |  2 |  1 |  0 | oct. | off.
    // -----------------------------------------------------
    // | header                                | 34   | 0
    // | originTimestamp(48b sec., 32b nsec.)  | 10   | 34
    // | curentUtcOffset                       | 2    | 44
    // | Reserved                              | 1    | 46
    // | grandmasterPriority1                  | 1    | 47
    // -----------------------------------------------------
    // | grandmasterClockQuality               | 4    | 48
    // | grandmasterPriority2                  | 1    | 52
    // | grandmasterIdentify                   | 8    | 53
    // | stepsRemoved                          | 2    | 61
    // | timeSource                            | 1    | 63
    val annOriginTimestamp         = m.signal(80)
    val annCurentUtcOffset         = m.signal(16)
    val annGrandmasterPriority1    = m.signal(8)
    val annGrandmasterClockQuality = m.signal(32)
    val annGrandmasterPriority2    = m.signal(8)
    val annGrandmasterIdentify     = m.signal(64)
    val annStepsRemoved            = m.signal(16)
    val annTimeSource              = m.signal(8)

    // PTP Sync Message
    // |  7 |  6 |  5 |  4 |  3 |  2 |  1 |  0 | oct. | off.
    // -----------------------------------------------------
    // | header                                | 34   | 0
    // | originTimestamp                       | 10   | 34
    val syncOriginTimestamp = m.signal(80)

    // PTP Follow-up Message
    // |  7 |  6 |  5 |  4 |  3 |  2 |  1 |  0 | oct. | off.
    // -----------------------------------------------------
    // | header                                | 34   | 0
    // | preciseOrigin Timestamp               | 10   | 34
    val folPriciseOriginTimeStamp = m.signal(80)

    // PTP Delay Request Message
    // |  7 |  6 |  5 |  4 |  3 |  2 |  1 |  0 | oct. | off.
    // -----------------------------------------------------
    // | header                                | 34   | 0
    // | originTimestamp                       | 10   | 34
    val dreqOriginTimestamp         = m.signal(80)

    // PTP Delay Response Message
    // |  7 |  6 |  5 |  4 |  3 |  2 |  1 |  0 | oct. | off.
    // -----------------------------------------------------
    // | header                                | 34   | 0
    // | receiveTimestamp                      | 10   | 34
    // | requestingPortIdentify                | 10   | 34
    val dresReceiveTimeStamp       = m.signal(80)
    val dresRequestingPortIdentify = m.signal(80)

    val seq = m.sequencer("main")
    outData <= (seq.idle, m.value(0, 128))
    outReq  <= (seq.idle, m.value(0, 1))
    outEn   <= (seq.idle, m.value(0, 1))

    val s0 = seq.add()
    transportSpecific <= (s0, m.range(inData, 127, 124))
    messageType       <= (s0, m.range(inData, 123, 120))
    versionPTP        <= (s0, m.range(inData, 115, 112))
    messageLength     <= (s0, m.range(inData, 111,  96))
    domainNumber      <= (s0, m.range(inData,  95,  88))
    flags             <= (s0, m.range(inData,  79,  64))
    correctionField   <= (s0, m.range(inData,  63,   0))

    val s1 = seq.add()
    sourcePortIdentify <= (s1, m.range(inData, 95, 16))
    sequenceID         <= (s1, m.range(inData, 15, 0))

    val s2 = seq.add()
    // header
    controlField       <= (s2, inData.range(127, 120))
    logMessageInterval <= (s2, inData.range(119, 112))
    annOriginTimestamp      <= (s2, inData.range(111, 32))
    annCurentUtcOffset      <= (s2, inData.range(31, 16))
    annGrandmasterPriority1 <= (s2, inData.range(7, 0))
    syncOriginTimestamp <= (s2, inData.range(111, 32))
    folPriciseOriginTimeStamp <= (s2, inData.range(111, 32))
    dreqOriginTimestamp <= (s2, inData.range(111, 32))
    dresReceiveTimeStamp <= (s2, inData.range(111, 32))
    dresRequestingPortIdentify <= (s2, inData.range(31, 0)) // 4-octets

    val s3 = seq.add()
    annGrandmasterClockQuality <= (s3, inData.range(127, 96))
    annGrandmasterPriority2    <= (s3, inData.range(95, 88))
    annGrandmasterIdentify     <= (s3, inData.range(87, 24))
    annStepsRemoved            <= (s3, inData.range(23, 8))
    annTimeSource              <= (s3, inData.range(7, 0))
    dresRequestingPortIdentify <= (s3, 
      (dresRequestingPortIdentify.drop(48).concat(inData.range(47, 0)))) // 6-octets


    seq.idle -> s0
    s0 * inEn -> s1
    s1 -> s2
    s2 -> s3
    s3 -> seq.idle

    for (s <- Vector(seq.idle, s1, s2, s3)){ // except s0
      inAck <= (s, m.value(1,1))
    }
    
    return m
  }

  def generate_sim(target:Module, name:String) : SimModule = {
      val sim = new SimModule(name)
      val inst = sim.instance(target, "U")
      
      val (clk, reset, counter) = sim.system(10)
      
      inst.sysClk := clk
      inst.sysReset := reset
      return sim
  }

  def main(args:Array[String]) = {
    var m = generate()
    val sim = generate_sim(m, "ptp_sim")
    m.genVHDL()
    m.genVerilog()
    sim.genVHDL()
    }
}
