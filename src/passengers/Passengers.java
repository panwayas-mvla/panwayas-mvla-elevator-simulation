package passengers;

// Owner: Michael Xu
/**
 * The Class Passengers. Represents a GROUP of passengers that are 
 * traveling together from one floor to another. Tracks information that 
 * can be used to analyze Elevator performance.
 */
public class Passengers {
	
	/**  Constant for representing direction. */
	private static final int UP = 1;
	
	/** The Constant DOWN. */
	private static final int DOWN = -1;
	
	/**  ID represents the NEXT available id for the passenger group. */
	private static int ID=0;

	/** id is the unique ID assigned to each Passenger during construction.
	 *  After assignment, static ID must be incremented.
	 */
	private int id;
	
	/** These fields will be passed into the constructor by the Building.
	 *  This data will come from the .csv file read by the SimController
	 */
	private int time;         // the time that the Passenger will call the elevator
	
	/** The num pass. */
	private int numPass;      // the number of passengers in this group
	
	/** The on floor. */
	private int onFloor;      // the floor that the Passenger will appear on
	
	/** The dest floor. */
	private int destFloor;	  // the floor that the Passenger will get off on
	
	/** The polite. */
	private boolean polite;   // will the Passenger let the doors close?
	
	/** The wait time. */
	private int waitTime;     // the amount of time that the Passenger will wait for the
	                          // Elevator
	
	/** These values will be calculated during construction.
	 */
	private int direction;      // The direction that the Passenger is going
	
	/** The time will give up. */
	private int timeWillGiveUp; // The calculated time when the Passenger will give up
	
	/** These values will actually be set during execution. Initialized to -1 */
	private int boardTime=-1;
	
	/** The time arrived. */
	private int timeArrived=-1;

	/**
	 * Instantiates a new passengers.
	 *
	 * @param time the time
	 * @param numPass the number of people in this Passenger
	 * @param on the floor that the Passenger calls the elevator from
	 * @param dest the floor that the Passenger is going to
	 * @param polite - are the passengers polite?
	 * @param waitTime the amount of time that the passenger will wait before giving up
	 */
	public Passengers(int time, int numPass, int on, int dest, boolean polite, int waitTime) {
		this.time = time;
		this.numPass = numPass;
		this.onFloor = on - 1;
		this.destFloor = dest - 1;
		this.polite = polite;
		this.waitTime = waitTime;
		this.direction = (onFloor < destFloor) ? UP : DOWN;
		this.timeWillGiveUp = time + waitTime;
		this.id = ID++;
	}
	
	
	// TODO: Write any required getters/setters for this class

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Gets the num pass.
	 *
	 * @return the num pass
	 */
	public int getNumPass() {
		return numPass;
	}

	/**
	 * Gets the on floor.
	 *
	 * @return the on floor
	 */
	public int getOnFloor() {
		return onFloor;
	}

	/**
	 * Gets the dest floor.
	 *
	 * @return the dest floor
	 */
	public int getDestFloor() {
		return destFloor;
	}

	/**
	 * Gets the wait time.
	 *
	 * @return the wait time
	 */
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * Gets the board time.
	 *
	 * @return the board time
	 */
	public int getBoardTime() {
		return boardTime;
	}

	/**
	 * Gets the time arrived.
	 *
	 * @return the time arrived
	 */
	public int getTimeArrived() {
		return timeArrived;
	}

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Gets the time will give up.
	 *
	 * @return the time will give up
	 */
	public int getTimeWillGiveUp() {
		return timeWillGiveUp;
	}
	
	
	/**
	 * Checks if passenger is polite or not
	 * 
	 * @return true if polite, false if impolite
	 */
	public boolean isPolite() {
		return polite;
	}
	
	/**
	 * Sets the board time.
	 * 
	 * @param boardTime
	 */
	public void setBoardTime(int boardTime) {
		this.boardTime = boardTime;
	}
	
	/**
	 * Sets the time arrived.
	 * 
	 * @param timeArrived
	 */
	public void setTimeArrived(int timeArrived) {
		this.timeArrived = timeArrived;
	}
	
	/**
	 * Reset static ID. 
	 * This method MUST be called during the building constructor BEFORE
	 * reading the configuration files. This is to provide consistency in the
	 * Passenger ID's during JUnit testing.
	 */
	public static void resetStaticID() {
		ID = 0;
	}

	/**
	 * toString - returns the formatted string for this class.
	 *
	 * @return the
	 */
	@Override
	public String toString() {
		return("ID="+id+"   Time="+time+"   NumPass="+numPass+"   From="+(onFloor+1)+"   To="+(destFloor+1)+"   Polite="+polite+"   Wait="+waitTime);
	}

}
