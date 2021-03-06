package com.xorlev.flightgear

import java.net.{DatagramSocket, InetAddress, DatagramPacket}
import rx.lang.scala.Observable
import com.tzavellas.sse.jmx.export.annotation.Managed

/**
 * 2014-06-05
 * @author Michael Rose
 */
class FlightGearAutopilot(controller: Controller) extends Autopilot {
  val fgHost = InetAddress.getByName("127.0.0.1")
  val fgPortOut = 6666
  val fgPortIn = 6789
  val bufferLength = 2048

  val socketOut = new DatagramSocket()

  // Variables
  @volatile var active = false

  private[this] def observeInstruments(): Observable[InstrumentSample] = {
    val socketIn = new DatagramSocket(fgPortOut)
    val packet = new DatagramPacket(Array.ofDim(bufferLength), bufferLength)

    Observable(s => {
      try {
        while (active) {
          socketIn.receive(packet)

          try {
            val sample = parseDatagram(packet)
            s.onNext(sample)
          } catch {
            case e: NumberFormatException => println("Error parsing datagram: " + e)
          }
        }
      } catch {
        case t: Throwable => t.printStackTrace(); s.onError(t)
      } finally {
        socketIn.close()
        s.onCompleted()
      }
    })
  }

  private[this] def applyControl(control: Control) = {
    val payload = new String(s"${control.roll},${control.pitch}\n")
    socketOut.send(new DatagramPacket(payload.getBytes, payload.size, fgHost, fgPortIn))
  }

  def parseDatagram(packet: DatagramPacket): InstrumentSample = {
    new String(packet.getData, 0, packet.getLength).split(',') match {
      case Array(roll, pitch, heading) => InstrumentSample(roll.toDouble, pitch.toDouble, heading.toDouble)
      case _ => throw new IllegalArgumentException("Bad datagram: " + new String(packet.getData))
    }
  }

  @Managed
  override def start() = {
    active = true

    observeInstruments()
    .map(controller.control)
    .subscribe(c => applyControl(c))
  }

  @Managed
  override def stop() = active = false
}