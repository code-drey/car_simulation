
package ptolemy.carsim;

import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.carsim.CarInput;
import ptolemy.carsim.CarOutput;
import ptolemy.carsim.Intersection;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public abstract class AbstractDirectorCar extends DEDirector {

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
    public AbstractDirectorCar(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return an additional delay for a track to keep an aircraft in
     *  transit.
     *  @param track The track.
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract double handleRejectionWithDelay(Intersection intersection)
            throws IllegalActionException;


    /** Put an entry into _neighbors , _stormyTrack  and _inTransit for the initialized track.
     *  @param track The track.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleInitializedIntersection(Intersection intersction)
            throws IllegalActionException;

    /** Routing an aircraft based on its flight map.
     *  @param aircraft (this token is a record of "aircraftId","aircraftSpeed","flightMap" and "priorTrack"and ...)
     *  @param trackId The trackid.
     *  @return A RecordToken representing the routing.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract RecordToken routing(Token aircraft, Token intersectionId)
            throws IllegalActionException;

    /** Return status of the track.
     *  @param trackId The trackid.
     *  @return The status of the track.
     */
    public abstract boolean returnIntersectionStatus(Token intersectionId);

    /** Update inTransit status of a track.
     *  @param trackId The trackid
     *  @param trackStatus The status
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void setInTransitStatusOfIntersection(Token intersectionId,
            boolean intersectionStatus) throws IllegalActionException;

    /** Reroute an aircraft.
     *  @param aircraft The aircraft
     *  @return a Map of rerouted aircraft.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract Map<String, Token> rerouteUnacceptedCar(Token car)
            throws IllegalActionException;

    /** Return airplane's color. If the airplane has not color, set a color for that and store it.
     *  @param id id of the airplane
     *  @return The color of the airplane.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract ArrayToken handleCarColor(int id)
            throws IllegalActionException;

    /** Handle initializing of an airport.
     *  @param airport The airport
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleInitializedCarInput(CarInput carinput)
            throws IllegalActionException;

    /** Handle initializing of a destination airport. This function stores airport id in _airportsId
     *  @param destinationAirport The destination.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleInitializedDestination(CarOutput carOutput)
            throws IllegalActionException;

    public abstract void handleIntersectionAttributeChanged (Intersection intersection)
            throws IllegalActionException;
}
