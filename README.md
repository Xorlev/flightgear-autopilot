FlightGear Autopilot
--------------------

Experiments into control theory in Scala. Implements a simple proportional controller (P) as well as a full proportional-integral-derivative controller (PID) for both roll & pitch control.

Utilizes the FlightGear generic interface.

Uses RxJava totally unnecessarily to decouple the consumer from the controller.

![screen shot 2014-06-08 at 2 32 04 pm](https://cloud.githubusercontent.com/assets/348618/3212294/43d850d6-ef51-11e3-93ad-94dc47d0fc35.PNG)
PID control on pitch at work.

#### Future work

- Eliminate overcorrection issues. Can sometimes not converge due to wild control swings.
- Handle edge cases (e.g. upside down)
- Nav routing
- Speed, altitude, vertical speed holds
- FlightGear binary protocol (for fun)
- 
#### Inspiration

Initial proportional controller & control interface entirely inspired by and converted from http://nakkaya.com/2010/10/07/towards-a-clojure-autopilot-first-steps/
