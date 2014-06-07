package com.xorlev.flightgear

/**
 * 2014-06-07
 * @author Michael Rose <michael@fullcontact.com>
 */
class ProportionalController2 extends Controller {
  def control(sample: InstrumentSample) = {
    val rollControl = mapNumber(sample.roll, 90, -90, -1, 1)
    val pitchControl = mapNumber(sample.pitch, -45, 45, -1, 1)

    println(s"Control: roll[${sample.roll}, $rollControl], pitch[${sample.pitch}, $pitchControl]")

    Control(rollControl, pitchControl)
  }

  def mapNumber(pv: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double) = {
    val e = 0-pv
    val k_p = 1

    val output = k_p*e
    val scaled = (output - outMin)
    //
    //
    // Scaled
    (((output - inMin) * (outMax - outMin)) / (inMax - inMin)) + outMin

    if (output > outMax)
      outMax
    else if (output < outMin)
      outMin
    else
      output
  }
}