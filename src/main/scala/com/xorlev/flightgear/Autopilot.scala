package com.xorlev.flightgear

/**
 * 2014-06-02
 * @author Michael Rose
 */

trait Autopilot {
  def start()
  def stop()
}
trait Controller {
  def control(sample: InstrumentSample): Control
}
case class InstrumentSample(roll: Double, pitch: Double, timestamp: Double = System.currentTimeMillis())
case class Control(roll: Double, pitch: Double, timestamp: Double = System.currentTimeMillis())