package com.xorlev.flightgear

import com.tzavellas.sse.jmx.export.MBeanExporter

/**
 * 2014-06-02
 * @author Michael Rose
 */
object AutopilotRunner extends App {
  /*
  (def fg-host (InetAddress/getByName "127.0.0.1"))
(def fg-port-out 6666)
(def fg-port-in 6789)

   */


  /*}}
  (defn map-number [x in-min in-max out-min out-max]
  (let [val (+ (/ (* (- x in-min)
                     (- out-max out-min))
                  (- in-max in-min)) out-min)]
    (cond (> val out-max) out-max
          (< val out-min) out-min
          :default val)))

(defn controller [roll pitch]
  (let [roll-cntrl (float (map-number roll 90 -90 -1 1))
        pitch-cntrl (float (map-number pitch -45 45 -1 1))]
    (println "Control: " roll roll-cntrl pitch pitch-cntrl)
    [roll-cntrl pitch-cntrl]))

   */
  val exporter = new MBeanExporter
  val pidController = new PIDController
  exporter.export(pidController)

  new FlightGearAutopilot(pidController).start()
}