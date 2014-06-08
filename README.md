FlightGear Autopilot
--------------------

Experiments into control theory in Scala. Utilizes the FlightGear generic interface to implement a simple proportional controller (P) as well as a full proportional-integral-derivative controller (PID) for both roll & pitch control. A proportional controller implements heading holds.

PID control has proved to be vastly more appropriate for an autopilot (surprise). The integral term can account for environmental error such as wind as well as trim issues.

Uses RxJava totally unnecessarily to decouple the consumer from the controller.

### Running
- Put (or symlink) input/output protocol XML files into <flightgear-root>/data/Protocol. For me, that's `/Applications/FlightGear.app/Contents/Resources/`
- Start FlightGear from CLI. e.x.
```
    /Applications/FlightGear.app/Contents/Resources/fgfs.sh --timeofday=morning \\
    --aircraft=CitationX --geometry=640x480 \\
    --generic=socket,out,40,localhost,6666,udp,output-protocol \\
    --generic=socket,in,45,127.0.0.1,6789,udp,input-protocol
```
- Start AutopilotRunner


### Runtime Metrics
Most tuning parameters (including proportional/integral/derivative gains) and setpoints are exposed via JMX. Internal workings of each PID instance (pitch, roll, heading) are exposed through JMX as well.

![Pitch Change](https://cloud.githubusercontent.com/assets/348618/3212294/43d850d6-ef51-11e3-93ad-94dc47d0fc35.PNG)
PID control on pitch at work.

![Heading 230->180](https://cloud.githubusercontent.com/assets/348618/3212502/40e5cd4e-ef61-11e3-8083-11a20898704f.PNG)

Heading change from 230->180.

### Future work

- Eliminate overcorrection issues. Can sometimes not converge due to wild control swings.
- Handle edge cases (e.g. upside down)
- Nav routing
- Speed, altitude, vertical speed holds
- FlightGear binary protocol (for fun)

### Inspiration

Initial proportional controller & control interface entirely inspired by and converted from http://nakkaya.com/2010/10/07/towards-a-clojure-autopilot-first-steps/
