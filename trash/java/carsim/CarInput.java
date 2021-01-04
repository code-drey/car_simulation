/* A model of a source airport in air traffic control systems.

Copyright (c) 2015-2016 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/

package ptolemy.carsim;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.Director;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.carsim.kernel.AbstractDirectorCar;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This actor receives a record token which shows an airplane decides to fly.
* Therefore, this actor just sends that to a proper direction based on the neighbors (of the airport)
* If the destination track (first track in the airplane's flight map is unavailable,
* then airport try to send it after a period of time.
*  @author Maryam Bagheri
*  @version $Id$
*  @since Ptolemy II 11.0
*  @Pt.ProposedRating Red (cxh)
*  @Pt.AcceptedRating Red (cxh)
*/
public class CarInput extends TypedAtomicActor {

 /** Construct an actor with the given container and name.
  *  @param container The container.
  *  @param name The name of this actor.
  *  @exception IllegalActionException If the actor cannot be contained
  *   by the proposed container.
  *  @exception NameDuplicationException If the container already has an
  *   actor with this name.
  */
 public CarInput(CompositeEntity container, String name)
         throws IllegalActionException, NameDuplicationException {
     super(container, name);

     input = new TypedIOPort(this, "input", true, false);
     input.setTypeEquals(BaseType.RECORD);

     output = new TypedIOPort(this, "output", false, true);
     output.setTypeEquals(BaseType.RECORD);
     output.setMultiport(true);

     delay = new Parameter(this, "delay");
     delay.setTypeEquals(BaseType.DOUBLE);
     delay.setExpression("1");

     start = new Parameter(this, "start");
     start.setTypeEquals(BaseType.DOUBLE);
     start.setExpression("1");

     carInputId = new Parameter(this, "carInputId");
     carInputId.setTypeEquals(BaseType.INT);
     carInputId.setExpression("-1");

     connectedIntersections = new Parameter(this, "connectedIntersections");
     connectedIntersections.setExpression("{}");
     connectedIntersections.setTypeEquals(new ArrayType(BaseType.INT));

 }

 /** The input port, which is of type record token. */
 public TypedIOPort input;

 /** The output port, which is of type record token. */
 public TypedIOPort output;

 /** The delay. */
 public Parameter delay;

 /** The airport Id. */
 public Parameter carInputId;
 
 public Parameter connectedIntersections;

 /** A double with the initial default value of 1. */
 public Parameter start;

 /** Fire the actor.
  *  @exception IllegalActionException If thrown by the baseclass
  *  or if there is a problem accessing the ports or parameters.
  */
 @Override
 public void fire() throws IllegalActionException {
     super.fire();
     Time currentTime = _director.getModelTime();
     if (currentTime.equals(_transitExpires) && _inTransit != null) {
         try {
             //***When carInput decides to send out a car it must set it's departure time.
             //For this purpose, we make a new recordtoken
             double startTime = currentTime.getDoubleValue()
                     - ((DoubleToken) start.getToken()).doubleValue();
             RecordToken firstCar = _cars.get(0);
             Map<String, Token> tempCar = new TreeMap<String, Token>();
             tempCar.put("carId", firstCar.get("carId"));
             tempCar.put("carSpeed",
                     firstCar.get("carSpeed"));
             tempCar.put("roadMap", firstCar.get("roadMap"));
             tempCar.put("fuel", firstCar.get("fuel"));
             tempCar.put("priorIntersection", firstCar.get("priorIntersection"));
             tempCar.put("arrivalTimeToDestination",
                     firstCar.get("arrivalTimeToDestination"));
             tempCar.put("departureTimeFromCarInput",
                     new DoubleToken(startTime));
             _cars.set(0, new RecordToken(tempCar));
             int i = _findDirection(_cars.get(0));
             output.send(i, _cars.get(0));
             _cars.remove(0);
             _inTransit = null;
         } catch (NoRoomException ex) {
             double additionalDelay = ((DoubleToken) delay.getToken())
                     .doubleValue();
             if (additionalDelay < 0.0) {
                 throw new IllegalActionException(this,
                         "Unable to handle rejection.");
             }
             _transitExpires = _transitExpires.add(additionalDelay);
             _director.fireAt(this, _transitExpires);
         }

         if (_inTransit == null && _cars.size() != 0) {
             _inTransit = _cars.get(0);
             double additionalDelay = ((DoubleToken) start.getToken())
                     .doubleValue();
             if (additionalDelay < 0.0) {
                 throw new IllegalActionException(this,
                         "Unable to handle rejection.");
             }
             _transitExpires = _transitExpires.add(additionalDelay);
             _director.fireAt(this, _transitExpires);
         }
     }

     if (input.hasToken(0)) {
         RecordToken token_car = (RecordToken) input.get(0);
         Map<String, Token> map_car = new TreeMap<String, Token>();
         map_car.put("carId", token_car.get("carId"));
         map_car.put("carSpeed", token_car.get("carSpeed"));
         map_car.put("roadMap", token_car.get("roadMap"));
         map_car.put("priorIntersection", carInputId.getToken());
         //new added fields to the carInput packet
         map_car.put("fuel", token_car.get("fuel"));
         double arrivalTime = currentTime.getDoubleValue();
         map_car.put("arrivalTimeToDestination", new DoubleToken(arrivalTime));
         map_car.put("departureTimeFromCarInput",
                 new DoubleToken(arrivalTime));
         //end of new added...
         _cars.add(new RecordToken(map_car));

         if (_inTransit == null) {
             double additionalDelay = ((DoubleToken) start.getToken())
                     .doubleValue();
             if (additionalDelay < 0.0) {
                 throw new IllegalActionException(this,
                         "Delay is negative in carInput.");
             }
             _inTransit = _cars.get(0);
             _transitExpires = currentTime.add(additionalDelay);
             _director.fireAt(this, _transitExpires);
         }
     }
 }

 /** Initialize this actor.  Derived classes override this method
  *  to perform actions that should occur once at the beginning of
  *  an execution, but after type resolution.  Derived classes can
  *  produce output data and schedule events.
  *  @exception IllegalActionException If a derived class throws it.
  */
 @Override
 public void initialize() throws IllegalActionException {
     super.initialize();
     _director = getDirector();
     ((AbstractDirectorCar) _director).handleInitializedCarInput(this);
     _inTransit = null;
     _intersections = (ArrayToken) connectedIntersections.getToken();
     if (_intersections.length() == 0) {
         throw new IllegalActionException(
                 "there is no connected intersection to the carInput in the carInput's parameters ");
     }
     _cars = new ArrayList<RecordToken>();

 }

 private int _findDirection(RecordToken car)
         throws IllegalActionException {
     ArrayToken roadMap = (ArrayToken) car.get("roadMap");
     boolean found = false;
     for (int i = 0; i < _intersections.length(); i++) {
         if (roadMap.getElement(0).equals(_intersections.getElement(i))) {
             found = true;
             return i;
         }
     }
     throw new IllegalActionException(
             "There is no route from the carInput to the first intersection in roadMap");
 }

 private Token _inTransit;
 private Time _transitExpires;
 private Director _director;
 private ArrayToken _intersections;
 private ArrayList<RecordToken> _cars;
}
