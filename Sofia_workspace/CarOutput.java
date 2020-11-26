
package ptolemy.carsim;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.carsim.AbstractDirectorCar;
import ptolemy.domains.atc.kernel.Rejecting;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;

public class CarOutput extends TypedAtomicActor implements Rejecting {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CarOutput(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new RecordType(_lables, _types));

        carInputId = new Parameter(this, "carInputId");
        carInputId.setTypeEquals(BaseType.INT);
        carInputId.setExpression("-1");

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1");

        EditorIcon node_icon = new EditorIcon(this, "_icon");

        //rectangle
        _rectangle = new RectangleAttribute(node_icon, "_rectangleShape");
        _rectangle.centered.setToken("true");
        _rectangle.width.setToken("60");
        _rectangle.height.setToken("50");
        _rectangle.rounding.setToken("10");
        _rectangle.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        _rectangle.fillColor.setToken("{0.8,0.8,1.0,1.0}");

        //inner triangle of the icon
        _shape = new ResizablePolygonAttribute(node_icon, "_triangleShape");
        _shape.centered.setToken("true");
        _shape.width.setToken("40");
        _shape.height.setToken("40");
        _shape.vertices.setExpression("{0.0,1.0,0.0,-1.0,2.0,0.0}");
        _shape.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");

    }

    /** The input port, which is a multiport. */
    public TypedIOPort input;

    /** The output port, which is of type RecordToken. */
    public TypedIOPort output;

    /** The id of the airport, which defaults to -1. */
    public Parameter carInputId;

    /** The delay. */
    public Parameter delay;

    /** Return true if the token cannot be accepted at the specified port.
     *  @param token The token that may be rejected.
     *  @param port The port.
     *  @return True to reject the token.
     */
    @Override
    public boolean reject(Token token, IOPort port) {
        if (_inTransit != null) {
            return true;
        }

        if (_called == false) {
            _called = true;
            return (_inTransit != null);
        } else {
            return true;
        }
    }

    /** Fire the actor.
     *  @exception IllegalActionException If thrown by the baseclass
     *  or if there is a problem accessing the ports or parameters.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
            output.send(0, _inTransit);
            //Set icon to white color
            _setIcon(-1);

            _inTransit = null;
            _called = false;
            return;
        }

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasNewToken(i)) {
                _inTransit = input.get(i);
                //Set icon to color of the airplane
                int id = ((IntToken) ((RecordToken) _inTransit)
                        .get("carId")).intValue();
                _setIcon(id);
                //
                _transitExpires = currentTime
                        .add(((DoubleToken) delay.getToken()).doubleValue());
                director.fireAt(this, _transitExpires);
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
        Director _director = getDirector();
        ((AbstractDirectorCar) _director).handleInitializedDestination(this);
        _inTransit = null;
        _called = false;
        _setIcon(-1);
    }

    /** Set the visual indication of the icon for the specified ID.
     *  @param id The aircraft ID or -1 to indicate no aircraft.
     *  @exception IllegalActionException
     */
    protected void _setIcon(int id) throws IllegalActionException {
        ArrayToken color = _noCarColor;
        if (id > -1) {
            Director _director = getDirector();
            color = ((AbstractDirectorCar) _director).handleCarColor(id);
            if (color == null) {
                throw new IllegalActionException(
                        "Color for the car " + id + " has not been set");
            }
        }
        _shape.fillColor.setToken(color);
    }

    //to change color of the icon
    private ResizablePolygonAttribute _shape;
    private RectangleAttribute _rectangle;
    private DoubleToken _one = new DoubleToken(1.0);
    private Token[] _white = { _one, _one, _one, _one };
    private ArrayToken _noCarColor = new ArrayToken(_white);
    //

    private Token _inTransit;
    private Time _transitExpires;
    private boolean _called;
    private String[] _lables = { "carId", "carSpeed", "roadMap",
            "priorIntersection", "fuel", "arrivalTimeToCarInput",
            "dipartureTimeFromCarInput" };
    private Type[] _types = { BaseType.INT, BaseType.INT,
            new ArrayType(BaseType.INT), BaseType.INT, BaseType.DOUBLE,
            BaseType.DOUBLE, BaseType.DOUBLE };

}