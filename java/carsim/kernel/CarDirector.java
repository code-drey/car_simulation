/* A director for modeling air traffic control systems.

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
package ptolemy.carsim.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import ptolemy.actor.Receiver;
import ptolemy.carsim.CarInput;
import ptolemy.carsim.Destination;
import ptolemy.carsim.Intersection;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.atc.kernel.ATCReceiver;
import ptolemy.domains.atc.kernel.AbstractATCDirector;
import ptolemy.domains.atc.lib.Airport;
import ptolemy.domains.atc.lib.DestinationAirport;
import ptolemy.domains.atc.lib.Track;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A director for modeling air traffic control systems.
 *  This director provides a receiver that consults the destination actor
 *  to determine whether it can accept an input, and provides mechanisms
 *  for handling rejection of an input.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public class CarDirector extends AbstractDirectorCar {

    // FIXME: This class and policy2/ATCDirector.java have quite a bit
    // of duplicated code.  It would be better to create a common base
    // class.

    /** Create a new director in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public CarDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return airplane's color. If the airplane has not color, set a color for that and store it.
     *  @param id id of the airplane
     *  @return The color of the airplane
     *  @exception IllegalActionException If thrown while creating an ArrayToken.
     */
    @Override
    public ArrayToken handleCarColor(int id)
            throws IllegalActionException {
        ArrayToken color = _carsColor.get(id);

        if (color == null) {
            Token[] colorSpec = new DoubleToken[4];
            colorSpec[0] = new DoubleToken(_random.nextDouble());
            colorSpec[1] = new DoubleToken(_random.nextDouble());
            colorSpec[2] = new DoubleToken(_random.nextDouble());
            colorSpec[3] = new DoubleToken(1.0);
            color = new ArrayToken(colorSpec);
            _carsColor.put(id, color);
        }

        return color;
    }

    /** Handle initializing of an airport.
     *  @param airport The airport
     *  @exception IllegalActionException If the id is invalid.
     */
    @Override
    public void handleInitializedCarInput(CarInput carInput)
            throws IllegalActionException {
        // TODO Auto-generated method stub

        int carInputId = ((IntToken) carInput.carInputId.getToken()).intValue();
        //        if (airportId==-1)
        //            throw new IllegalActionException("invalid id for airplane");
        //                if (_airplanesId.contains(airplaneId))
        //            throw new IllegalActionException("duplication in  airplanes id");
        //        _airplanesId.add(airplaneId);
        if (carInputId == -1) {
            throw new IllegalActionException("Invalid id for source CarInput");
        }
        if (_stormyTracks.containsKey(carInputId)) {
            throw new IllegalActionException("CarInput id is same as Intersection id");
        }
        if (!_carInputsId.contains(carInputId)) {
            _carInputsId.add(carInputId);
        }

        //        if (((ArrayToken)airplane.flightMap.getToken())==null)
        //            throw new IllegalActionException("flightMap is empty");
    }

    /** Handle initializing of a destination airport. This function stores airport id in _airportsId
     *  @param destinationAirport The destination airport.
     *  @exception IllegalActionException If the id is invalid, the id is
     *  a duplicate of the idea of another airport or if the airport
     *  id is the same as the a track id.
     */
    //TODO: maybe think about a new "_destinations" list ?
    @Override
    public void handleInitializedDestination(
            Destination destination)
            throws IllegalActionException {
        int destinationId = ((IntToken) destination.destinationId.getToken())
                .intValue();
        if (destinationId == -1) {
            throw new IllegalActionException(
                    "Invalid id for destination");
        }
        if (_carInputsId.contains(destinationId)) {
            throw new IllegalActionException("Duplication in destination id");
        }
        if (_stormyTracks.containsKey(destinationId)) {
            throw new IllegalActionException("Destination id is same as Intersection id");
        }
        _carInputsId.add(destinationId);

    }

    /** Update _stormyTracks array because of a change in condition of a track.
     *  @param track The track
     *  @exception IllegalActionException If the entry for the track has
     *  not been set in the the stormyTrack array.
     */
    @Override
    public void handleIntersectionAttributeChanged(Intersection intersection)
            throws IllegalActionException {
        int id = ((IntToken) intersection.intersectionId.getToken()).intValue();
        if (_stormyTracks.size() != 0) {
            if (_stormyTracks.containsKey(id)) {
                _stormyTracks.put(id, intersection.stormy.getToken());
            } else {
                throw new IllegalActionException(
                        "The entry for this intersection has not been set in stormyTrack array ");
            }
        }
    }

    /** Put an entry into _neighbors , _stormyTrack  and _inTransit for the initialized track.
     *  @param track The track.
     *  @exception IllegalActionException If there track is invalid.
     */
    @Override
    public void handleInitializedIntersection(Intersection intersection)
            throws IllegalActionException {
        int id = ((IntToken) intersection.intersectionId.getToken()).intValue();
        if (id == -1) {
            throw new IllegalActionException(
                    "Id of the intersection " + id + " is invalid (-1)");
        }
        if (_stormyTracks.containsKey(id)) {
            throw new IllegalActionException(
                    "Intersection with the id " + id + " has been duplicated");
        }
        if (_carInputsId.contains(id)) {
            throw new IllegalActionException("Track id is same as airport id");

        } //else {
//            if (intersection.stormy.getToken() == null) {
//                throw new IllegalActionException("Stormy parameter of track "
//                        + id + " has not been filled");
//            }
//            _stormyTracks.put(id, intersection.stormy.getToken());
//        }

        _inTransit.put(id, false);
        _neighbors.put(id, (ArrayToken) intersection.neighbors.getToken());
    }

    /** Return an additional delay for a track to keep an aircraft in
     *  transit.
     *  @param track The track
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  @exception IllegalActionException Not thrown in this method.
     */
    @Override
    public double handleRejectionWithDelay(Intersection intersection)
            throws IllegalActionException {
        // FIXME: what value should be returned here?
        return 1.0;
    }

    /** Initialize the state of this director.
     *  @exception IllegalActionException If thrown by the parent method.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _stormyTracks = new TreeMap<>();
        _neighbors = new TreeMap<>();
        _inTransit = new TreeMap<>();
        _carInputsId = new ArrayList<>();
        _carsId = new ArrayList<>();
        _carsColor = new HashMap<Integer, ArrayToken>();
        super.initialize();
    }

    /** Return a new ATCReceiver.
     *  @return a new ATCReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new CarReceiver();
    }

    /** Routing an aircraft based on its flight map.
     *  @param aircraft (this token is a record of "aircraftId","aircraftSpeed","flightMap" and "priorTrack"and ...)
     *  @param trackId The track Id.
     *  @return The routing.
     *  @exception IllegalActionException If there is a routing problem.
     */
    @Override
    public RecordToken routing(Token car, Token intersectionId)
            throws IllegalActionException {
        RecordToken token_car = (RecordToken) car; // TODO: for a queue maybe take a ArrayToken??
        ArrayToken roadMap = (ArrayToken) token_car.get("roadMap");
        int id = ((IntToken) intersectionId).intValue();
        if (!roadMap.getElement(0).equals(intersectionId)) {
            throw new IllegalActionException(
                    "There is a mistake in routing: mismatch of intersection id " + id
                            + " with first element in road map "
                            + ((IntToken) roadMap.getElement(0)).intValue());
        }
        Token nextIntersectionInTrip = roadMap.getElement(1);
        int route = -1;
        if (_neighbors.containsKey(id)) {
            ArrayToken intersectionNeighbors = _neighbors.get(id);
            for (int i = 0; i < intersectionNeighbors.length(); i++) {
                if (intersectionNeighbors.getElement(i).equals(nextIntersectionInTrip)) {
                    route = i;
                    break;
                }
            }
            if (route == -1) {
                throw new IllegalActionException("Mistake in routing. intersection "
                        + id + " has not neighbor intersection " + nextIntersectionInTrip);
            }
        } else {
            throw new IllegalActionException(
                    "Neighbors of the current intersection with id " + id
                            + " have not been set.");
        }

        Token[] newRoadMap = new Token[roadMap.length() - 1];
        int j = 0;
        for (int i = 1; i < roadMap.length(); i++) {
            newRoadMap[j++] = roadMap.getElement(i);
        }

        //creating a new airplane record
        Map<String, Token> newCar = new TreeMap<String, Token>();
        newCar.put("carId", token_car.get("carId"));
        newCar.put("carSpeed", token_car.get("carSpeed"));
        newCar.put("roadMap",
                (new ArrayToken(BaseType.INT, newRoadMap)));
        newCar.put("priorIntersection", (new IntToken(id)));
        newCar.put("arrivalTimeToCarInput",
                token_car.get("arrivalTimeToCarInput"));
        newCar.put("departureTimeFromDestination",
                token_car.get("departureTimeFromDestination"));
        newCar.put("fuel", token_car.get("fuel"));
        //
        //add some information to newAirplane and then exploit and remove them from newAirplane (for transfer information to Track actor).
        newCar.put("delay", new DoubleToken(1.0));
        newCar.put("route", new IntToken(route));
        return (new RecordToken(newCar));
    }

    /** Return status of the track.
     *  @param trackId The track Id.
     *  @return The status
     */
    @Override
    public boolean returnIntersectionStatus(Token intersectionId) {
        int id = ((IntToken) intersectionId).intValue();
        return (_inTransit.get(id)
                || ((BooleanToken) _stormyTracks.get(id)).booleanValue());
    }

    /** Reroute an aircraft.
     *  @param aircraft The aircraft
     *  @return A Map of rerouted aircraft.
     *  @exception IllegalActionException If thrown while getting the flightMap or setting parameters.
     */
    @Override
    public Map<String, Token> rerouteUnacceptedCar(Token car)
            throws IllegalActionException {
        RecordToken token_car = (RecordToken) car;
        ArrayToken roadMap = (ArrayToken) token_car.get("roadMap");
        if (roadMap.length() == 1) {// it just contains id of the destination,
            //it should send the car to that again.
            Map<String, Token> map = new TreeMap<String, Token>();
            map.put("roadMap", roadMap);
            map.put("route", new IntToken(-1));
            map.put("delay", new DoubleToken(1.0));
            return map;
        }

        int priorIntersection = ((IntToken) token_car.get("priorIntersection")).intValue();
        Token currentIntersection = roadMap.getElement(0);// this is the rejecting track
        Token destination = roadMap.getElement(roadMap.length() - 1);
        int route = -1;
        boolean baseOnDestination = false;
        boolean neighborChoosed = false;
        ArrayToken neighborsOfPriorIntersection = _neighbors.get(priorIntersection);

        for (int i = 0; i < neighborsOfPriorIntersection.length(); i++) {
            Token temp = neighborsOfPriorIntersection.getElement(i);
            int tempId = ((IntToken) temp).intValue();
            if (temp.equals(destination)) {
                route = i;
                neighborChoosed = true;
                baseOnDestination = true;
                break;
            }
            if (tempId != -1 && !_carInputsId.contains(tempId)
                    && !temp.equals(currentIntersection) && !_inTransit.get(tempId)
                    && !((BooleanToken) _stormyTracks.get(tempId))
                            .booleanValue()) {
                DijkstraAlgorithm x = new DijkstraAlgorithm();
                Token[] shortestPath = x.callDijkstra(_neighbors, _carInputsId,
                        tempId, ((IntToken) destination).intValue(),
                        _stormyTracks, _inTransit);
                if (shortestPath == null) {
                    continue;
                } else {
                    neighborChoosed = true;
                    route = i;
                    roadMap = new ArrayToken(BaseType.INT, shortestPath);
                    break;
                }
            } //end of outer if
        } //end of for

        if (neighborChoosed && baseOnDestination) {
            roadMap = roadMap.subarray(roadMap.length() - 1);
        }

        Map<String, Token> map = new TreeMap<String, Token>();
        map.put("roadMap", roadMap);
        map.put("route", new IntToken(route));
        map.put("delay", new DoubleToken(1.0));
        return map;
    }

    /** Update inTransit status of a track.
     *  @param trackId The track id
     *  @param trackStatus The track status
     *  @exception IllegalActionException If thrown while getting the track Id.
     */
    @Override
    public void setInTransitStatusOfIntersection(Token intersectionId, boolean intersectionStatus)
            throws IllegalActionException {
        int id = ((IntToken) intersectionId).intValue();
        if (_inTransit.containsKey(id)) {
            _inTransit.put(id, intersectionStatus);
        } else if (!_carInputsId.contains(id)) {
            throw new IllegalActionException("There is no intersection with id " + id);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    // private variables which show situation of tracks

    private Random _random = new Random();

    /**  Which track is stormy:first element is
     *  id of the track and last is a boolean token.
     */
    private Map<Integer, Token> _stormyTracks = new TreeMap<>();

    /**  Neighbors of each track:first element is id of the track and
     *  last is array of its neighbors.
     */
    private Map<Integer, ArrayToken> _neighbors = new TreeMap<>();

    /** The existance of one aircraft in the track: first element is
     * id and last is a boolean.
     */
    private Map<Integer, Boolean> _inTransit = new TreeMap<>();

    /** The id of the airport. */
    private ArrayList<Integer> _carInputsId = new ArrayList<>();

    /** The id of the airplane. */
    private ArrayList<Integer> _carsId = new ArrayList<>();

    /** A color for each airplane. */
    private Map<Integer, ArrayToken> _carsColor = new HashMap<Integer, ArrayToken>();
}
