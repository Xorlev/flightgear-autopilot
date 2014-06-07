package com.xorlev.flightgear

import java.net.{DatagramSocket, InetAddress, DatagramPacket}

/**
 * 2014-06-05
 * @author Michael Rose <michael@fullcontact.com>
 */
class FlightGearAutopilot(controller: Controller) extends Autopilot {
  val fgHost = InetAddress.getByName("127.0.0.1")
  val fgPortOut = 6666
  val fgPortIn = 6789
  val bufferLength = 2048

  val socketIn = new DatagramSocket(fgPortOut)
  val socketOut = new DatagramSocket()
  val packet = new DatagramPacket(Array.ofDim(bufferLength), bufferLength)

  // Variables

  @volatile var active = false

  private[this] def controlLoop() {
    try {
      while(active) {
        socketIn.receive(packet)

        val sample = parseDatagram(packet)

        val control = controller.control(sample)
        val payload = new String(s"${control.roll},${control.pitch}\n")
        socketOut.send(new DatagramPacket(payload.getBytes, payload.size, fgHost, fgPortIn))
      }
    } finally {
      socketIn.close()
    }
  }

  def parseDatagram(packet: DatagramPacket): InstrumentSample = {
    val sample = new String(packet.getData).split(',') match {
      case Array(roll, pitch) => InstrumentSample(roll.toDouble, pitch.toDouble)
      case _ => sys.error("Unexpected datagram")
    }
    sample
  }

  override def start() = {
    active = true
    controlLoop()
  }

  override def stop() = active = false
}