FlightGear Autopilot
--------------------

Experiments into control theory in Scala.

Uses RxJava totally unnecessarily to decouple the consumer from the controller.

#### Future work

- Eliminate overcorrection issues. Can sometimes not converge due to wild control swings.
- Handle edge cases (e.g. upside down)
- Nav routing
- Speed, altitude, vertical speed holds.

#### Inspiration

Initial proportional controller & control interface entirely inspired by and converted from http://nakkaya.com/2010/10/07/towards-a-clojure-autopilot-first-steps/
