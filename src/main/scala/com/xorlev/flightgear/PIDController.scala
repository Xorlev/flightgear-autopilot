package com.xorlev.flightgear

/**
 * 2014-06-07
 * @author Michael Rose
 */
class PIDController extends Controller {
  var lastControl: Control = null
  val rollPid = new PID(0.0, 0.009, 0.005, 0.005)
  val pitchPid = new PID(0.0, 0.009, 0.005, 0.005)

  class PID(setPoint: Double, kp: Double, ki: Double, kd: Double) {
    var errSum = 0.0
    var lastInput = 0.0
    var lastErr = 0.0
    var lastTime = System.currentTimeMillis()

    def apply(input: Double) = calculate(input)

    def calculate(input: Double): Double = {
      val now = System.currentTimeMillis()
      val timeDelta = now - lastTime
      println(s"TimeDelta: $timeDelta")
      println(s"LastTime: $lastTime Now: ${now}")
      println(s"LastErr: $lastErr")

      val error = setPoint - input
      println(s"Error: $error")
      errSum += error*timeDelta
      println(s"ErrorSum: $errSum")
      val dErr = (error - lastErr) / timeDelta
      println(s"dErr: $dErr")
      val dInput = input - lastInput
      println(s"dInput: $dInput")

      val output = kp * error * ki * errSum - kd * dInput

      lastInput = input
      lastErr = error
      lastTime = now
      
      output
    }
  }

  override def control(sample: InstrumentSample): Control = {
    if (lastControl != null && System.currentTimeMillis() - lastControl.timestamp < 100) return lastControl

    val control = Control(
      scale(rollPid(sample.roll), -45, 45, -1, 1),
      scale(pitchPid(sample.pitch), -90, 90, -1, 1)
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