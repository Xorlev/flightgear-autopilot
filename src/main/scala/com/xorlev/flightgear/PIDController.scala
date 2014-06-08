package com.xorlev.flightgear

import com.tzavellas.sse.jmx.export.annotation.Managed
import com.tzavellas.sse.jmx.export.MBeanExporter
import javax.management.ObjectName
import org.slf4j.LoggerFactory
import com.codahale.metrics.{ExponentiallyDecayingDoubleReservoir, ExponentiallyDecayingReservoir}

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
 * Integral - proportion should be main controller, however it can't accord for all error. Integral term gets you to 0
 * Derivative - counteracts changes away from setpoint to minimize future error
 *
 * @author Michael Rose
 */
class PIDController extends Controller {
  var lastControl: Control = null
  val rollPid = new PID(0.1, 0.001, 0.0004, -40, 40, -0.5, 0.5)
  val pitchPid = new PID(0.01, 0.001, 0.0005, -90, 90, -0.5, 0.5)
  val headingPid = new PID(1.5, 0, 0, -90, 90, -30, 30)
  val log = LoggerFactory.getLogger(this.getClass)

  @Managed
  var pitchHold = 5.0

  @Managed
  var rollHold = 20.0
  
  @Managed
  var headingHold = -180.0 // disabled

  class PID(
            var kp2: Double,
             var ki2: Double,
             var kd2: Double,
             var inMin: Double,
             var inMax: Double,
             var outMin: Double,
             var outMax: Double) {
    @Managed var kp: Double = kp2
    @Managed var ki: Double = ki2
    @Managed var kd: Double = kd2
    // Process variables
    @Managed(readOnly = true) var proportionalTerm = 0.0
    @Managed(readOnly = true) var integralTerm = 0.0
    @Managed(readOnly = true) var clampedIntegralTerm = 0.0
    @Managed(readOnly = true) var derivativeTerm = 0.0
    @Managed(readOnly = true) var output = 0.0
    @Managed(readOnly = true) var lastInput = 0.0
    @Managed(readOnly = true) var lastErr = 0.0
    @Managed(readOnly = true) var lastTime = System.currentTimeMillis()

    def apply(input: Double, setPoint: Double) = calculate(input, setPoint)

    def calculate(input: Double, setPoint: Double): Double = {
      val now = System.currentTimeMillis()
      val timeDelta = now - lastTime
      log.debug(s"TimeDelta: $timeDelta")
      log.debug(s"LastTime: $lastTime Now: ${now}")
      log.debug(s"LastErr: $lastErr")

      val error = setPoint - input

      log.debug(s"Error: $error")
      val dErr = (error - lastErr) / timeDelta
      log.debug(s"dErr: $dErr")
      val dInput = input - lastInput
      log.debug(s"dInput: $dInput")
      log.debug(s"dInput*kd: ${dInput*kd}")

      proportionalTerm = kp * error
      integralTerm += error
      clampedIntegralTerm = clamp(ki*integralTerm, outMin, outMax)
      derivativeTerm = kd * dInput
      log.debug(s"proportionalTerm: $proportionalTerm")
      log.debug(s"integralTerm: $integralTerm")
      log.debug(s"clampedIntegralTerm: $clampedIntegralTerm")
      log.debug(s"derivativeTerm: $derivativeTerm")


      output = clamp(proportionalTerm + clampedIntegralTerm - derivativeTerm, outMin, outMax)

      lastInput = input
      lastErr = error
      lastTime = now

      output
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
    if (lastControl != null && System.currentTimeMillis() - lastControl.timestamp < 200) return lastControl

    if (headingHold >= 0) {
      rollHold = headingPid(sample.heading, headingHold)
    }

    var rollControl = rollPid(sample.roll, rollHold)
    var pitchControl = -pitchPid(sample.pitch, pitchHold)

    val control = Control(
      rollControl,
      pitchControl
    )

    lastControl = control
    log.info(s"Control: roll[${sample.roll}, ${control.roll}], pitch[${sample.pitch}, ${control.pitch}]")

    control
  }

  def scale(input: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double) = {
    val output = (((input - inMin) * (outMax - outMin)) / (inMax - inMin)) + outMin

    log.debug("=> Output " + output)

    if (output > outMax)
      outMax
    else if (output < outMin)
      outMin
    else if (output.isNaN)
      0.0
    else
      output
  }

  def export(exporter: MBeanExporter) {
    exporter.export(this)
    exporter.export(rollPid, ObjectName.getInstance("com.xorlev.flightgear:type=RollPID"))
    exporter.export(pitchPid,  ObjectName.getInstance("com.xorlev.flightgear:type=PitchPID"))
    exporter.export(headingPid,  ObjectName.getInstance("com.xorlev.flightgear:type=HeadingPID"))
  }
//
//  def pidPitch(input: Double) = {
//    val now = System.currentTimeMillis()
//    val timeDelta = 100
//    log.debug(s"TimeDelta: $timeDelta")
//    log.debug(s"LastTime: $lastControlled Now: ${now}")
//    log.debug(s"LastErr: $lastErr_pitch")
//
//    val error = setPoint - input
//    log.debug(s"Error: $error")
//    errSum_pitch += error*timeDelta
//    log.debug(s"ErrorSum: $errSum_pitch")
//    val dErr = (error - lastErr_pitch) / timeDelta
//    log.debug(s"dErr: $dErr")
//    val dInput = input - lastInput_pitch
//    log.debug(s"dInput: $dInput")
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