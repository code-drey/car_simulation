package ptolemy.carsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import ptolemy.actor.Receiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.atc.kernel.ATCReceiver;
import ptolemy.carsim.AbstractDirectorCar;
import ptolemy.carsim.CarOutput;
import ptolemy.carsim.Intersection;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class CarDirector extends AbstractDirectorCar {

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
        // TODO Auto-generated constructor stub
    }

    /** Return car's color. If the car has not color, set a color for that and store it.
     *  @param id id of the car
     *  @return The car's color.x
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

    /** Handle initializing of an carInput.
     *  @param carInput The carInput.
     *  @exception IllegalActionException If the id is invalid.
     */
    @Override
    public void handleInitializedCarInput(CarInput carInput)
            throws IllegalActionException {
        int carInputId = ((IntToken) carInput.carInputId.getToken()).intValue();
        //        if (carInputId == -1)
        //            throw new IllegalActionException("invalid id for car");
        //                if (_carsId.contains(carId))
        //            throw new IllegalActionException("duplication in  cars id");
        //        _carsId.add(carId);
        if (carInputId == -1) {
            throw new IllegalActionException("Invalid id for source carInput");
        }
        if (_stormyIntersections.containsKey(carInputId)) {
            throw new IllegalActionException("carInput id is same as intersection id");
        }
        if (!_carInputId.contains(carInputId)) {
            _carInputId.add(carInputId);
        }

        //        if (((ArrayToken)car.roadMap.getToken()) == null)
        //            throw new IllegalActionException("roadMap is empty");
    }

    /** Handle initializing of a destination carInput. This function stores carInput id in _carInputId
     *  @param destinationcarInput The destination carInput.
     *  @exception IllegalActionException If the id is invalid, the id is
     *  a duplicate of the idea of another carInput or if the carInput
     *  id is the same as the a intersection id.
     */
    
    @Override
    public void handleInitializedDestination(CarOutput carOutput)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        int carInputId = ((IntToken) carOutput.carInputId.getToken())
                .intValue();
        if (carInputId == -1) {
            throw new IllegalActionException(
                    "Invalid id for destination carInput");
        }
        if (_carInputId.contains(carInputId)) {
            throw new IllegalActionException("Duplication in carInputs id");
        }
        if (_stormyIntersections.containsKey(carInputId)) {
            throw new IllegalActionException("carInput id is same as intersection id");
        }
        _carInputId.add(carInputId);
    }

    /** Put an entry for neighbors, stormyintersection and inTransit for the initialized intersection.
     *  @param intersection The intersection.
     *  @exception IllegalActionException If there intersection is invalid.
     */
    @Override
    public void handleInitializedIntersection(Intersection intersection)
            throws IllegalActionException {
        int id = ((IntToken) intersection.intersectionId.getToken()).intValue();
        if (id == -1) {
            throw new IllegalActionException(
                    "Id of the intersection " + id + " is invalid (-1)");
        }
        if (_stormyIntersections.containsKey(id)) {
            throw new IllegalActionException(
                    "intersection with the id " + id + " has been duplicated");
        }
        if (_carInputId.contains(id)) {
            throw new IllegalActionException("intersection id is same as carInput id");
        } else {
            if (intersection.stormy.getToken() == null) {
                throw new IllegalActionException("Stormy parameter of intersection "
                        + id + " has not been filled");
            }
            _stormyIntersections.put(id, intersection.stormy.getToken());
        }

        _inTransit.put(id, false);
        _neighbors.put(id, (ArrayToken) intersection.neighbors.getToken());
    }

    /** Return an additional delay for a intersection to keep an car in
     *  transit.
     *  @param intersection The intersection
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
        _stormyIntersections = new TreeMap<>();
        _neighbors = new TreeMap<>();
        _inTransit = new TreeMap<>();
        _carInputId = new ArrayList<>();
        _carsId = new ArrayList<>();
        _carsColor = new HashMap<Integer, ArrayToken>();
        super.initialize();
    }

    /** Update _stormyintersections array because of a change in condition of a intersection.
     *  @param intersection The intersection
     *  @exception IllegalActionException If the entry for the intersection has
     *  not been set in the the stormyintersection array.
     */
    @Override
    public void handleIntersectionAttributeChanged(Intersection intersection)
            throws IllegalActionException {
        int id = ((IntToken) intersection.intersectionId.getToken()).intValue();
        if (_stormyIntersections.size() != 0) {
            if (_stormyIntersections.containsKey(id)) {
                _stormyIntersections.put(id, intersection.stormy.getToken());
            } else {
                throw new IllegalActionException(
                        "The entry for this intersection has not been set in stormyintersection array ");
            }
        }
    }

    /** Routing an car based on its flight map.
     *  @param car (this token is a record of "carId","carSpeed","roadMap" and "priorintersection"and ...)
     *  @param intersectionId the intersection id.
     *  @return The routing.
     *  @exception IllegalActionException If there is a routing problem.
     */
    @Override
    public RecordToken routing(Token car, Token intersectionId)
            throws IllegalActionException {
        RecordToken car1 = (RecordToken) car;
        ArrayToken roadMap = (ArrayToken) car1.get("roadMap");
        int id = ((IntToken) intersectionId).intValue();
        if (!roadMap.getElement(0).equals(intersectionId)) {
            throw new IllegalActionException(
                    "There is a mistake in routing: mismatch of intersection id " + id
                            + " with first element in flight map "
                            + ((IntToken) roadMap.getElement(0)).intValue());
        }
        Token nextintersectionInFlight = roadMap.getElement(1);
        int route = -1;
        if (_neighbors.containsKey(id)) {
            ArrayToken intersectionNeighbors = _neighbors.get(id);
            for (int i = 0; i < intersectionNeighbors.length(); i++) {
                if (intersectionNeighbors.getElement(i).equals(nextintersectionInFlight)) {
                    route = i;
                    break;
                }
            }
            if (route == -1) {
                throw new IllegalActionException("Mistake in routing. intersection "
                        + id + " has not neighbor intersection " + nextintersectionInFlight);
            }
        } else {
            throw new IllegalActionException(
                    "Neighbors of the current intersection with id " + id
                            + " have not been set.");
        }
        Token[] newroadMap = new Token[roadMap.length() - 1];
        int j = 0;
        for (int i = 1; i < roadMap.length(); i++) {
            newroadMap[j++] = roadMap.getElement(i);
        }

        //creating a new car record
        Map<String, Token> newcar = new TreeMap<String, Token>();
        newcar.put("carId", car1.get("carId"));
        newcar.put("carSpeed", car1.get("carSpeed"));
        newcar.put("roadMap",
                (new ArrayToken(BaseType.INT, newroadMap)));
        newcar.put("priorintersection", (new IntToken(id)));
        newcar.put("arrivalTimeTocarInput",
                car1.get("arrivalTimeTocarInput"));
        newcar.put("dipartureTimeFromcarInput",
                car1.get("dipartureTimeFromcarInput"));
        newcar.put("fuel", car1.get("fuel"));
        //
        //add some infromation to newcar and then exploit and remove them from newcar (for transfer information to intersection actor).
        newcar.put("delay", new DoubleToken(1.0));
        newcar.put("route", new IntToken(route));
        return (new RecordToken(newcar));
    }

    /** Return status of the intersection.
     *  @param intersectionId The intersection Id.
     *  @return The status
     */
    @Override
    public boolean returnIntersectionStatus(Token intersectionId) {
        int id = ((IntToken) intersectionId).intValue();
        return (_inTransit.get(id)
                || ((BooleanToken) _stormyIntersections.get(id)).booleanValue());
    }

    /** Update inTransit status of a intersection.
     *  @param intersectionId The intersection id
     *  @param intersectionStatus The intersection status
     *  @exception IllegalActionException If thrown while getting the intersection Id.
     */
    @Override
    public void setInTransitStatusOfIntersection(Token intersectionId, boolean intersectionStatus)
            throws IllegalActionException {
        int id = ((IntToken) intersectionId).intValue();
        if (_inTransit.containsKey(id)) {
            _inTransit.put(id, intersectionStatus);
        } else if (!_carInputId.contains(id)) {
            throw new IllegalActionException("There is no intersection with id " + id);
        }
    }

    /** Reroute an car.
     *  @param car The car
     *  @return A Map of rerouted car.
     *  @exception IllegalActionException If thrown while getting the roadMap or setting parameters.
     */
    @Override
    public Map<String, Token> rerouteUnacceptedCar(Token car)
            throws IllegalActionException {
        RecordToken car1 = (RecordToken) car;
        ArrayToken roadMap = (ArrayToken) car1.get("roadMap");
        if (roadMap.length() == 1) {// it just contains id of the destination carInput,
            //it should send the car to that again.
            Map<String, Token> map = new TreeMap<String, Token>();
            map.put("roadMap", roadMap);
            map.put("route", new IntToken(-1));
            map.put("delay", new DoubleToken(1.0));
            return map;
        }

        Token choosedNeighbor = null;
        int route = -1;
        boolean baseOnDestination = false;
        boolean neighborChoosed = false;
        int priorintersection = ((IntToken) car1.get("priorintersection")).intValue();
        Token currentintersection = roadMap.getElement(0);
        Token nextintersection = roadMap.getElement(1);
        Token destination = roadMap.getElement(roadMap.length() - 1);
        ArrayToken neighborsOfPriorintersection = _neighbors.get(priorintersection);
        for (int i = 0; i < neighborsOfPriorintersection.length(); i++) {
            Token temp = neighborsOfPriorintersection.getElement(i);
            int tempId = ((IntToken) temp).intValue();
            if (temp.equals(destination)) {
                route = i;
                neighborChoosed = true;
                choosedNeighbor = temp;
                baseOnDestination = true;
                break;
            }
            if (tempId != -1 && !_carInputId.contains(tempId)
                    && !temp.equals(currentintersection) && !_inTransit.get(tempId)
                    && !((BooleanToken) _stormyIntersections.get(tempId))
                            .booleanValue()) {
                ArrayToken tempNeighbors = _neighbors.get(tempId);
                for (int j = 0; j < tempNeighbors.length(); j++) {
                    if (tempNeighbors.getElement(j).equals(nextintersection)) {
                        neighborChoosed = true;
                        route = i;
                        choosedNeighbor = temp;
                        break;
                    }
                }
                if (neighborChoosed) {
                    break;
                }
            } //end of outer if
        } //end of for

        if (neighborChoosed && !baseOnDestination) {
            roadMap = roadMap.update(0, choosedNeighbor);
        } else if (neighborChoosed && baseOnDestination) {
            roadMap = roadMap.subarray(roadMap.length() - 1);
        } else if (car1.get("priorintersection").equals(nextintersection)) {
            Token nextOfnextintersection = roadMap.getElement(2);
            for (int i = 0; i < neighborsOfPriorintersection.length(); i++) {
                Token temp = neighborsOfPriorintersection.getElement(i);
                if (temp.equals(nextOfnextintersection)) {
                    route = i;
                    neighborChoosed = true;
                    roadMap = roadMap.subarray(2);
                }
            }
        }

        Map<String, Token> map = new TreeMap<String, Token>();
        map.put("roadMap", roadMap);
        map.put("route", new IntToken(route));
        map.put("delay", new DoubleToken(1.0));
        return map;
    }

    /** Return a new ATCReceiver.
     *  @return a new ATCReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new ATCReceiver();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    //private variables which show situation of intersections
    private Random _random = new Random();

    /**  Which intersection is stormy:first element is
     *  id of the intersection and last is a boolean token.
     */
    private Map<Integer, Token> _stormyIntersections = new TreeMap<>();

    /**  Neighbors of each intersection:first element is id of the intersection and
     *  last is array of its neighbors.
     */
    private Map<Integer, ArrayToken> _neighbors = new TreeMap<>();

    /** The existance of one car in the intersection: first element is
     * id and last is a boolean.
     */
    private Map<Integer, Boolean> _inTransit = new TreeMap<>();

    /** The id of the carInput. */
    private ArrayList<Integer> _carInputId = new ArrayList<>();

    /** The id of the car. */
    private ArrayList<Integer> _carsId = new ArrayList<>();

    /** A color for each car. */
    private Map<Integer, ArrayToken> _carsColor = new HashMap<Integer, ArrayToken>();
}