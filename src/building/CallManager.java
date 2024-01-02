package building;

import passengers.Passengers;


//Owner: Yash Panwar
/**
 * The Class CallManager. This class models all of the calls on each floor,
 * and then provides methods that allow the building to determine what needs
 * to happen (ie, state transitions).
 */
public class CallManager {

	/** The floors. */
	private Floor[] floors;

	/** The num floors. */
	private final int NUM_FLOORS;

	/** The Constant UP. */
	public final static int UP = 1;

	/** The Constant DOWN. */
	public final static int DOWN = -1;

	/** The up calls array indicates whether or not there is a up call on each floor. */
	private boolean[] upCalls;

	/** The down calls array indicates whether or not there is a down call on each floor. */
	private boolean[] downCalls;

	/** The up call pending - true if any up calls exist */
	private boolean upCallPending;

	/** The down call pending - true if any down calls exit */
	private boolean downCallPending;

	//TODO: Add any additional fields here..


	/**
	 * Instantiates a new call manager.
	 *
	 * @param floors the floors
	 * @param numFloors the num floors
	 */
	public CallManager(Floor[] floors, int numFloors) {
		this.floors = floors;
		NUM_FLOORS = numFloors;
		upCalls = new boolean[NUM_FLOORS];
		downCalls = new boolean[NUM_FLOORS];
		upCallPending = false;
		downCallPending = false;

		//TODO: Initialize any added fields here
	}

	/**
	 * Update call status. This is an optional method that could be used to compute
	 * the values of all up and down call fields statically once per tick (to be
	 * more efficient, could only update when there has been a change to the floor queues -
	 * either passengers being added or being removed. The alternative is to dynamically
	 * recalculate the values of specific fields when needed.
	 */
	void updateCallStatus() {
		upCallPending = false;
		downCallPending = false;
		int i = 0;
		for(Floor floor: floors) {
			if(!floor.empty(UP)) {
				upCalls[i] = true;
				upCallPending = true;
			} else 
				upCalls[i] = false; 

			if(!floor.empty(DOWN)) {
				downCalls[i] = true;
				downCallPending = true;
			} else 
				downCalls[i] = false;
			i++;
		}
	}


	/**
	 * Used by building to determine based on current floor
	 * 
	 * 
	 * @param floor
	 * @return
	 */
	public int direction(int currFloor) {
		int upDirection = 0;
		for (int i = 0; i <= currFloor; i++) {
			if (upCalls[i] || downCalls[i]) upDirection--;
		}
		for (int i = currFloor; i < NUM_FLOORS; i++) {
			if (upCalls[i] || downCalls[i]) upDirection++;
		}
		return (upDirection >= 0) ? UP : DOWN;
	}

	/**
	 * 
	 * @param currFloor
	 * @return
	 */
	public int getUpCalls() {
		int numCalls = 0;
		for(int i = 0; i < NUM_FLOORS; i++) {
			if(upCalls[i])
				numCalls++;
		}
		return numCalls;
	}

	/**
	 * 
	 * @param currFloor
	 * @return
	 */
	public int getDownCalls() {
		int numCalls = 0;
		for(int i = 0; i < NUM_FLOORS; i++) {
			if(downCalls[i])
				numCalls++;
		}
		return numCalls;
	}

	/**
	 * Called during STOP STATE when call detected, finds passengers who won the call
	 * difBwLowupAndFlr - difference between the lowest up calls and the current floor 
	 * difBwHighupAndFlr - difference between the highest down call and the current floor
	 * 
	 *
	 * @param current floor
	 * @return the passengers
	 */
	Passengers prioritizePassengerCalls(int floor) {
		updateCallStatus();
		int dir = direction(floor);
		if(upCalls[floor] || downCalls[floor]) {
			Passengers upCallOnFlr = floors[floor].peek(UP), downCallOnFlr = floors[floor].peek(DOWN);
			if(!upCalls[floor]) return downCallOnFlr;
			if(!downCalls[floor]) return upCallOnFlr;
			if(upCalls[floor] && downCalls[floor]) {
				return (dir == UP) ? upCallOnFlr : downCallOnFlr;
			}
		} else {
			Passengers lowUpPas = lowestUpCall(), highDownPas = highestDownCall();
			if (lowUpPas == null && highDownPas == null) return null;
			if (lowUpPas == null) return highDownPas;
			if (highDownPas == null) return lowUpPas;
			int diffLow = floor - lowUpPas.getOnFloor(), 
					diffHigh = highDownPas.getOnFloor() - floor;
			int numUp = getUpCalls(), numDown = getDownCalls();
			if(numUp > numDown) return lowUpPas;
			if(numDown > numUp) return highDownPas;
			return (diffLow <= diffHigh) ? lowUpPas : highDownPas;
		}
	//	System.out.println("No call1");
		return null;
	}


	//TODO: Write any additional methods here. Things that you might consider:
	//      1. pending calls - are there any? only up? only down?
	//      2. is there a call on the current floor in the current direction
	//      3. How many up calls are pending? how many down calls are pending? 
	//      4. How many calls are pending in the direction that the elevator is going
	//      5. Should the elevator change direction?
	//
	//      These are an example - you may find you don't need some of these, or you may need more...

	/**
	 * Checks if there is any call during the current tick
	 * 
	 * @return true if calls pending
	 */
	public boolean callPending() {
		if(upCallPending || downCallPending)
			return true;
		return false;
	}
	


	/** When the elevator is going down, finds the lowest floor to arrive on.
	 *  Will continue going until it reaches that floor (Newton's law of elevators)
	 * 
	 * @return pass on lowest call going up, null if there are no calls present
	 */
	public Passengers lowestUpCall() {
		for (int i = 0; i < NUM_FLOORS; i++) {
			if (upCalls[i]) return floors[i].peek(UP);
		}
		return null;
	}

	/**
	 * When the elevator is going up, finds the highest floor to arrive on.
	 * Will continue going until it reaches that floor (Newton's law of elevators)
	 *  
	 * @return pass on highest call going down, null if there are no calls present

	 */
	public Passengers highestDownCall() {
		for (int i = NUM_FLOORS - 1; i >= 0; i--) {
			if (downCalls[i]) return floors[i].peek(DOWN);
		}
		return null;
	}
	
	/**
	 * Checks if there are calls going in the direction the elevator is going 
	 * to go
	 * 
	 * @param currFloor
	 * @param dir
	 * @return if any call in direction
	 */
	public boolean callsInDir(int currFloor, int dir) {
		updateCallStatus();
		for (int i = currFloor + dir; i < NUM_FLOORS && i >= 0; i += dir) {
			if (upCalls[i] || downCalls[i]) return true;
		}
		return false;
	}


}
