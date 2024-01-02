package building;
import java.util.ArrayList;

import passengers.Passengers;

// Owner: Yash Panwar
/**
 * The Class Elevator.
 *
 * @author This class will represent an elevator, and will contain
 * configuration information (capacity, speed, etc) as well
 * as state information - such as stopped, direction, and count
 * of passengers targeting each floor...
 */
public class Elevator {

	/**  Elevator State Variables - These are visible publicly. */
	public final static int STOP = 0;

	/** Midway through door opening state */
	public final static int DOOR_IN_MOTION = 8;

	/** End of Door Open State */
	public final static int DOOR_NOT_IN_MOTION = 9;

	/** The Constant MVTOFLR. */
	public final static int MVTOFLR = 1;

	/** The Constant OPENDR. */
	public final static int OPENDR = 2;

	/** The Constant OFFLD. */
	public final static int OFFLD = 3;

	/** The Constant BOARD. */
	public final static int BOARD = 4;

	/** The Constant CLOSEDR. */
	public final static int CLOSEDR = 5;

	/** The Constant MV1FLR. */
	public final static int MV1FLR = 6;


	/** Default configuration parameters for the elevator. These should be
	 *  updated in the constructor.
	 */
	private int capacity = 15;				// The number of PEOPLE the elevator can hold

	/** The ticks per floor. */
	private int ticksPerFloor = 5;			// The time it takes the elevator to move between floors

	/** The ticks door open close. */
	private int ticksDoorOpenClose = 2;  	// The time it takes for doors to go from OPEN <=> CLOSED

	/** The pass per tick. */
	private int passPerTick = 3;            // The number of PEOPLE that can enter/exit the elevator per tick

	/**  Finite State Machine State Variables. */
	private int currState;		// current state

	/** The prev state. */
	private int prevState;      // prior state

	/** The prev floor. */
	private int prevFloor;      // prior floor

	/** The curr floor. */
	private int currFloor;      // current floor

	/** The direction. */
	private int direction;      // direction the Elevator is traveling in.

	/** The time in state. */
	private int timeInState;    // represents the time in a given state
	// reset on state entry, used to determine if
	// state has completed or if floor has changed
	// *not* used in all states 

	/** The door state. */
	private int doorState;      // used to model the state of the doors - OPEN, CLOSED
	// or moving

	/** The passengers. */
	private int numPassengers;  	// the number of people in the elevator

	/** The pass by floor. */
	private ArrayList<Passengers>[] passByFloor;  // Passengers to exit on the corresponding floor

	/** The move to floor. */
	private int moveToFloor;	// When exiting the STOP state, this is the floor to move to without
	// stopping.

	/** The post move to floor dir. */
	private int postMoveToFloorDir; // This is the direction that the elevator will travel AFTER reaching
	// the moveToFloor in MVTOFLR state.

	/** Delay time when boarding or offloading passengers */
	private int delayTime;
	
	/** Number of passengers boarded in current BOARD state */
	private int numBoarded;
	
	/** Records if the elevator is currently full */
	private boolean currFull;
	
	/**
	 * Instantiates a new elevator.
	 *
	 * @param numFloors the num floors
	 * @param capacity the capacity
	 * @param floorTicks the floor ticks
	 * @param doorTicks the door ticks
	 * @param passPerTick the pass per tick
	 */
	@SuppressWarnings("unchecked")
	public Elevator(int numFloors,int capacity, int floorTicks, int doorTicks, int passPerTick) {		
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 0;
		this.currFloor = 0;
		passByFloor = new ArrayList[numFloors];

		for (int i = 0; i < numFloors; i++) 
			passByFloor[i] = new ArrayList<Passengers>(); 

		//TODO: Finish this constructor, adding configuration initialiation and
		//      initialization of any other private fields, etc.
		delayTime = 0;
		numBoarded = 0;
		currFull = false;
	}

	/**
	 * Instantiates a new elevator
	 * 
	 * @param numFloors the num floors
	 */
	public Elevator(int numFloors) {
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 0;
		this.currFloor = 0;
		passByFloor = new ArrayList[numFloors];

		for (int i = 0; i < numFloors; i++) 
			passByFloor[i] = new ArrayList<Passengers>(); 
	}


	//TODO: Add Getter/Setters and any methods that you deem are required. Examples 
	//      include:
	//      1) moving the elevator
	//      2) closing the doors
	//      3) opening the doors
	//      and so on...

	/**
	 * Gets the capacity.
	 *
	 * @return the capacity
	 */
	int getCapacity() {
		return this.capacity;
	}

	/**
	 * Gets the ticks per floor.
	 *
	 * @return the ticks per floor
	 */
	int getTicksPerFloor() {
		return this.ticksPerFloor;
	}

	/**
	 * Gets the ticks door open close.
	 *
	 * @return the ticks door open close
	 */
	int getTicksDoorOpenClose() {
		return this.ticksDoorOpenClose;
	}

	/**
	 * Gets the pass per tick.
	 *
	 * @return the pass per tick
	 */
	int getPassPerTick() {
		return this.passPerTick;
	}

	/**
	 * Gets the curr state.
	 *
	 * @return the curr state
	 */
	int getCurrState() {
		return this.currState;
	}

	/**
	 * Gets the prev state.
	 *
	 * @return the prev state
	 */
	int getPrevState() {
		return this.prevState;
	}

	/**
	 * Gets the prev floor.
	 *
	 * @return the prev floor
	 */
	int getPrevFloor() {
		return this.prevFloor;
	}

	/**
	 * Gets the curr floor.
	 *
	 * @return the curr floor
	 */
	int getCurrFloor() {
		return this.currFloor;
	}

	/**
	 * Sets the direction
	 * 
	 * @param direction
	 */
	void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * Update curr state.
	 *
	 * @param nextState the next state
	 */
	void updateCurrState(int currState) {
		this.prevState = this.currState;
		this.currState = currState;
		if(this.prevState != this.currState) 
			timeInState = 0;

	}

	/**
	 * Increments the time in state, moves the elevator if reached new floor.
	 * 
	 */
	public void moveElevator() {
		timeInState++;
		prevFloor = currFloor;
		if((timeInState % ticksPerFloor) == 0) {
			currFloor += direction;
		}
	}


	/**
	 * Returns passByFloor
	 * 
	 * @return the passengers by floor
	 */
	public ArrayList<Passengers>[] getPassByFloor() {
		return passByFloor;
	}

	/**
	 * Gets moveToFloor
	 * 
	 * @return moveToFloor
	 */
	public int getMoveToFloor() {
		return moveToFloor;
	}

	/**
	 * Sets moveToFloor
	 * 
	 * @param moveToFloor
	 */
	public void setMoveToFloor(int moveToFloor) {
		this.moveToFloor = moveToFloor;
	}

	/**
	 * Checks based on the current ticks whether the door is moving or not
	 * 
	 * @return DOOR_IN_MOTION if door is in motion, DOOR_NOT_IN_MOTION otherwise
	 */
	public int getDoorState() {
		return (timeInState < ticksDoorOpenClose) ? DOOR_IN_MOTION : DOOR_NOT_IN_MOTION;
	}

	
	/**
	 * Updates number of passengers and calculates the board delay
	 *  
	 * @param numPass
	 */
	public void calculateBoardDelay(int numPass) {
		numBoarded += numPass;
		delayTime = numBoarded / passPerTick + ((numBoarded % passPerTick > 0) ? 1 : 0);
	}
	
	/**
	 * Updates number of passengers and calculates the offload delay
	 * 
	 * @param numPass
	 */
	public void calculateOffloadDelay(int numPass) {
		delayTime = numPass / passPerTick + ((numPass % passPerTick > 0) ? 1 : 0);
		numPassengers -= numPass;
	}
	
	/**
	 * Checks based on time in state whether elevator is done boarding
	 * 
	 * @return if boarding is done
	 */
	public boolean doneBoarding() {
		return (timeInState >= delayTime);
	}
	
	/**
	 * Checks based on time in state whether elevator is done offloading
	 * 
	 * @return if offload is done
	 */
	public boolean doneOffloading() {
		return (timeInState == delayTime);
	}

	/**
	 * Boards the passenger group and updates the number of passengers on elevator
	 * 
	 * @param p the passenger group
	 */
	public void boardPassengers(Passengers p) {
		passByFloor[p.getDestFloor()].add(p);
		numPassengers += p.getNumPass();
	}

	/**
	 * Increments time in state, updates door state accordingly
	 */
	public void closeOrOpenDoor() {
		if (currState == OPENDR) prevFloor = currFloor;
		timeInState++;
		if((timeInState % ticksDoorOpenClose) == 0) {
			doorState = DOOR_NOT_IN_MOTION;
		}
	}
	

	/**
	 * Gets the direction of the elevator
	 * 
	 * @return direction
	 */
	public int getDirection() {
		return this.direction;
	}

	/**
	 * Gets the number of passengers in the elevator
	 * 
	 * @return the number of passengers in the elevator
	 */
	public int getNumPassInElevator() {
		return numPassengers;
	}

	/**
	 * Gets the time in state
	 * 
	 * @return the time in state
	 */
	public int getTimeInState() {
		return timeInState;
	}

	/**
	 * Increments the time in state
	 * 
	 */
	public void incrementTimeInState() {
		timeInState++;
	}
	
	/**
	 * Sets the previous state
	 * 
	 * @param prevState the previous state
	 */
	public void setPrevState(int prevState) {
		this.prevState = prevState;
	}
	
	/**
	 * Gets the number of passengers currently boarded in this state
	 * 
	 * @return numBoarded the number of passengers boarded
	 */
	public int getNumBoarded() {
		return numBoarded;
	}
	
	/**
	 * Sets the number of passengers currently boarded in this state
	 * 
	 * @param numBoarded the number of passengers baorded
	 */
	public void setNumBoarded(int numBoarded) {
		this.numBoarded = numBoarded;
	}
	
	/**
	 * Returns currFull
	 * 
	 * @return true if the elevator cannot take the next passenger group
	 */
	public boolean isCurrFull() {
		return currFull;
	}
	
	/**
	 * Sets the currFull
	 * 
	 * @param currFull true if the elevator cannot take the next passenger group
	 */
	public void setCurrFull(boolean currFull) {
		this.currFull = currFull;
	}
	
	/**
	 * Gets the post move to floor direction
	 * 
	 * @return the post move to floor direction
	 */
	public int getPostMoveToFloorDir() {
		return postMoveToFloorDir;
	}
	
	/**
	 * Sets the post move to floor direction
	 * 
	 * @param postMoveToFloorDir the post move to floor direction
	 */
	public void setPostMoveToFloorDir(int postMoveToFloorDir) {
		this.postMoveToFloorDir = postMoveToFloorDir;
	}

}
