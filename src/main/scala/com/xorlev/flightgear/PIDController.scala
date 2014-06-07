package com.xorlev.flightgear

import com.tzavellas.sse.jmx.export.annotation.Managed

/**
 * Implements a basic PID controller, that is,
 *
 * output = proportional + integralTerm + derivativeTerm
 *
 * error = setPoint - measured
 * proportional = kp(tuning) * error
 * integralTerm = sum of all previous ki(tuning) * error
 * derivativeTerm = kd(tuning) * d(input)
 *
 * d(input) = input - lastInput
 *
 * This particular iteration applies control every 1000ms. kd,ki,kp have all been scaled to not overwhelm the max control
 * of [-1, 1] by scaling by a term of 1/(inputMax)
 *
 * @author Michael Rose
 */
class PIDController extends Controller {
  var lastControl: Control = null
  val rollPid = new PID(0.06, 0.006, 0.0005, -45, 45, -1, 1)
  val pitchPid = new PID(0.06, 0.006, 0.0005, -90, 90, -1, 1)

  @Managed
  def toggle() {
    enabled = !enabled
  }

  var enabled = true

  @Managed
  var pitchHold = 0.0

  @Managed
  var rollHold = 0.0

  class PID(kp: Double, ki: Double, kd: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double) {
    var integralTerm = 0.0
    var lastInput = 0.0
    var lastErr = 0.0
    var lastTime = System.currentTimeMillis()

    def apply(input: Double,setPoint: Double) = calculate(input, setPoint)

    def calculate(input: Double, setPoint: Double): Double = {
      val now = System.currentTimeMillis()
      val timeDelta = now - lastTime
      println(s"TimeDelta: $timeDelta")
      println(s"LastTime: $lastTime Now: ${now}")
      println(s"LastErr: $lastErr")

      val kp2 = 1.0/inMax

      val error = setPoint - input
      println(s"Error: $error")
      val dErr = (error - lastErr) / timeDelta
      println(s"dErr: $dErr")
      val dInput = input - lastInput
      println(s"dInput: $dInput")
      println(s"dInput*kd: ${dInput*kd}")

      integralTerm += ki*error
      println(s"integralTerm: $integralTerm")

      val clampedIntegralTerm = clamp(integralTerm, -1, 1)

      val output = kp2 * error + clampedIntegralTerm - kd * dInput

      lastInput = input
      lastErr = error
      lastTime = now
      
      clamp(output, -1, 1)
    }

    def clamp(output: Double, outMin: Double, outMax: Double) = {
      if (output > outMax)
        outMax
      else if (output < outMin)
        outMin
      else
        output
    }
  }

  override def control(sample: InstrumentSample): Control = {
    if (lastControl != null && System.currentTimeMillis() - lastControl.timestamp < 500) return lastControl

    val control = Control(
      rollPid(sample.roll, rollHold),
      -pitchPid(sample.pitch, pitchHold)
    )

    lastControl = control
    println(s"Control: roll[${sample.roll}, ${control.roll}], pitch[${sample.pitch}, ${control.pitch}]")

    control
  }

  def scale(input: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double) = {
    val output = (((input - inMin) * (outMax - outMin)) / (inMax - inMin)) + outMin

    println("=> Output " + output)

    if (output > outMax)
      outMax
    else if (output < outMin)
      outMin
    else if (output.isNaN)
      0.0
    else
      output
  }
//
//  def pidPitch(input: Double) = {
//    val now = System.currentTimeMillis()
//    val timeDelta = 100
//    println(s"TimeDelta: $timeDelta")
//    println(s"LastTime: $lastControlled Now: ${now}")
//    println(s"LastErr: $lastErr_pitch")
//
//    val error = setPoint - input
//    println(s"Error: $error")
//    errSum_pitch += error*timeDelta
//    println(s"ErrorSum: $errSum_pitch")
//    val dErr = (error - lastErr_pitch) / timeDelta
//    println(s"dErr: $dErr")
//    val dInput = input - lastInput_pitch
//    println(s"dInput: $dInput")
//
//    val output = kp * error * ki * errSum_pitch - kd * dInput
//
//    lastInput_pitch = input
//    lastErr_pitch = error
//    lastControlled = now
//
//    scale(output, 90, -90, -1, 1)
//  }
//
//  def pidRoll(input: Double) = {
//    val now = System.currentTimeMillis()
//    val timeDelta = now.toDouble - lastControlled
//
//    val error = setPoint - input
//    errSum_roll += error*timeDelta
//    val dErr = (error - lastErr_roll) / timeDelta
//
//    val output = kp * error * ki * errSum_roll + kd * dErr
//
//    lastErr_roll = error
//    lastControlled = now
//
//    scale(-output, -45, 45, -1, 1)
//  }
}