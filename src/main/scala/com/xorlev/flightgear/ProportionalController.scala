package com.xorlev.flightgear

/**
 * 2014-06-07
 * @author Michael Rose <michael@fullcontact.com>
 */
class ProportionalController extends Controller {
  def control(sample: InstrumentSample) = {
    val rollControl = mapNumber(sample.roll, 90, -90, -1, 1)
    val pitchControl = mapNumber(sample.pitch, -45, 45, -1, 1)

    println(s"Control: roll[${sample.roll}, $rollControl], pitch[${sample.pitch}, $pitchControl]")

    Control(rollControl, pitchControl)
  }

  def mapNumber(x: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double) = {
    val control = (((x - inMin) * (outMax - outMin)) / (inMax - inMin)) + outMin

    if (control > outMax)
      outMax
    else if (control < outMin)
      outMin
    else
      control
  }
}