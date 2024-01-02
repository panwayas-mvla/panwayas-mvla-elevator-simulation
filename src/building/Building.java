package building;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import myfileio.MyFileIO;
import passengers.Passengers;

// Owner: Michael Xu
/**
 * The Class Building.
 */
public class Building {
	
	/**  Constants for direction. */
	private final static int UP = 1;
	
	/** The Constant DOWN. */
	private final static int DOWN = -1;
	
	/** The Constant LOGGER. */
	private final static Logger LOGGER = Logger.getLogger(Building.class.getName());
	
	/**  The fh - used by LOGGER to write the log messages to a file. */
	private FileHandler fh;
	
	/**  The fio for writing necessary files for data analysis. */
	private MyFileIO fio;
	
	/**  File that will receive the information for data analysis. */
	private File passDataFile;

	/**  passSuccess holds all Passengers who arrived at their destination floor. */
	private ArrayList<Passengers> passSuccess;
	
	/**  gaveUp holds all Passengers who gave up and did not use the elevator. */
	private ArrayList<Passengers> gaveUp;
	
	/**  The number of floors - must be initialized in constructor. */
	private final int NUM_FLOORS;
	
	/**  The size of the up/down queues on each floor. */
	private final int FLOOR_QSIZE = 10;	
	
	/** The floors. */
	public Floor[] floors;
	
	/** The elevator. */
	private Elevator elevator;
	
	/**  The Call Manager - it tracks calls for the elevator, analyzes them to answer questions and prioritize calls. */
	private CallManager callMgr;
	
	// Add any fields that you think you might need here...

	/**
	 * Instantiates a new building.
	 *
	 * @param numFloors the num floors
	 * @param logfile the logfile
	 */
	public Building(int numFloors, String logfile) {
		NUM_FLOORS = numFloors;
		passSuccess = new ArrayList<Passengers>();
		gaveUp = new ArrayList<Passengers>();
		initializeBuildingLogger(logfile);
		// passDataFile is where you will write all the results for those passengers who successfully
		// arrived at their destination and those who gave up...
		fio = new MyFileIO();
		passDataFile = fio.getFileHandle(logfile.replaceAll(".log","PassData.csv"));
		
		// create the floors, call manager and the elevator arrays
		// note that YOU will need to create and config each specific elevator...
		floors = new Floor[NUM_FLOORS];
		for (int i = 0; i < NUM_FLOORS; i++) {
			floors[i]= new Floor(FLOOR_QSIZE); 
		}
		callMgr = new CallManager(floors,NUM_FLOORS);
		//TODO: if you defined new fields, make sure to initialize them here
		configElevator(NUM_FLOORS);
		Passengers.resetStaticID();
	}
	
	// TODO: Place all of your code HERE - state methods and helpers...
	
	/**
	 * Adds passengers from the queue to their respective floor queues.
	 *
	 * @param passQ the passenger queue
	 */
	public void addPassengersToQueue(ArrayList<Passengers> passQ) {
		while (passQ.size() > 0) {
			Passengers p = passQ.remove(0);
			floors[p.getOnFloor()].add(p);
			logCalls(p.getTime(), p.getNumPass(), p.getOnFloor(), p.getDirection(), p.getId());
		}
		callMgr.updateCallStatus();
	}
	
	/** Returns the queue string of a floor queue for the controller to access.
	 * 
	 * @param floor the floor number
	 * @param dir the direction
	 * @return the queue string
	 */
	public String getFloorQueueString(int floor, int dir) {
		return floors[floor].queueString(dir);
	}
	
	/** Returns the direction of the elevator.
	 * 
	 * @return the direction
	 */
	public int getDirection() {
		return elevator.getDirection();
	}
	
	/** Returns the number of passengers in the elevator.
	 * 
	 * @return number of passengers in the elevator
	 */
	public int getNumPassInElevator() {
		return elevator.getNumPassInElevator();
	}
	
	
	/**
	 * Configure the elevator with given parameters.
	 *
	 * @param numFloors the number of floors
	 */
	public void configElevator(int numFloors) {
		elevator = new Elevator(numFloors);
	}
	
	/**
	 * Gets the elevator.
	 *
	 * @return the elevator
	 */
	public Elevator getElevator() {
		return elevator;
	}
	
	/**
	 * Gets the current state of the elevator.
	 *
	 * @return the elevator state
	 */
	public int getElevatorState() {
		return elevator.getCurrState();
	}
	
	/**
	 * Gets the current floor of the elevator.
	 *
	 * @return the elevator floor
	 */
	public int getElevatorFloor() {
		return elevator.getCurrFloor();
	}
	
	/**
	 * Checks if is elevator in stop state.
	 *
	 * @return true, if is elevator in stop state
	 */
	public boolean isElevatorInStopState() {
		return (elevator.getCurrState() == Elevator.STOP);
	}
	
	// DO NOT CHANGE ANYTHING BELOW THIS LINE:
	/**
	 * Initialize building logger. Sets formating, file to log to, and
	 * turns the logger OFF by default
	 *
	 * @param logfile the file to log information to
	 */
	void initializeBuildingLogger(String logfile) {
		System.setProperty("java.util.logging.SimpleFormatter.format","%4$-7s %5$s%n");
		LOGGER.setLevel(Level.OFF);
		try {
			fh = new FileHandler(logfile);
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Handles the STOP state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateStop(int time) {
		int floorNum = elevator.getCurrFloor();
		Passengers p = callMgr.prioritizePassengerCalls(floorNum);
		if (p == null) {
			return Elevator.STOP;
		} else if (floorNum == p.getOnFloor()) {
			elevator.setDirection(p.getDirection());
			return Elevator.OPENDR;
		} else {
			int dir = (floorNum < p.getOnFloor()) ? UP : DOWN;
			elevator.setDirection(dir);
			elevator.setMoveToFloor(p.getOnFloor());
			elevator.setPostMoveToFloorDir(p.getDirection());
			return Elevator.MVTOFLR;
		}
	}

	/**
	 * Handles the MVTOFLR state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateMvToFlr(int time) {
		elevator.moveElevator();
		if (elevator.getCurrFloor() == elevator.getMoveToFloor()) {
			elevator.setDirection(elevator.getPostMoveToFloorDir());
			return Elevator.OPENDR;
		}
		return Elevator.MVTOFLR;
	}
	
	/**
	 * Handles the OPENDR state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateOpenDr(int time) {
		elevator.closeOrOpenDoor();
		if (elevator.getDoorState() == Elevator.DOOR_NOT_IN_MOTION) {
			int floorNum = elevator.getCurrFloor();
			return (elevator.getPassByFloor()[floorNum].size() > 0) ? Elevator.OFFLD : Elevator.BOARD;
		}
		return Elevator.OPENDR;
	}
	
	/**
	 * Handles the OFFLD state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateOffLd(int time) {
		elevator.incrementTimeInState();
		int floorNum = elevator.getCurrFloor();
		if (elevator.getPrevState() != Elevator.OFFLD) {
			int numPass = 0;
			for (int i = 0; i < elevator.getPassByFloor()[floorNum].size(); i++) {
				Passengers p = elevator.getPassByFloor()[floorNum].get(i);
				int pNumPass = p.getNumPass();
				numPass += pNumPass;
				p.setTimeArrived(time);
				logArrival(time, pNumPass, floorNum, p.getId());
			}
			elevator.getPassByFloor()[floorNum].clear();
			elevator.calculateOffloadDelay(numPass);
		}
		if (elevator.doneOffloading()) {
			int dir = elevator.getDirection();
			if (!floors[floorNum].empty(dir)) return Elevator.BOARD;
			if (elevator.getNumPassInElevator() == 0 && !callMgr.callsInDir(floorNum, dir) && 
					!floors[floorNum].empty(-dir)) {
				elevator.setDirection(-dir);
				return Elevator.BOARD;
			}
			return Elevator.CLOSEDR;
		}
		return Elevator.OFFLD;
	}
	
	/**
	 * Handles the BOARD state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateBoard(int time) {
		int floorNum = elevator.getCurrFloor(), dir = elevator.getDirection();
		int eleNumPass = elevator.getNumPassInElevator();
		while (!floors[floorNum].empty(dir) && eleNumPass < elevator.getCapacity() &&
				!elevator.isCurrFull()) {
			Passengers p = floors[floorNum].peek(dir);
			int pNumPass = p.getNumPass();
			if (eleNumPass + pNumPass > elevator.getCapacity()) {
				logSkip(time, pNumPass, floorNum, dir, p.getId());
				elevator.setCurrFull(true);
				break;
			} else {
				p.setBoardTime(time);
				elevator.boardPassengers(floors[floorNum].poll(dir));
				logBoard(time, pNumPass, floorNum, dir, p.getId());
				elevator.calculateBoardDelay(pNumPass);
				elevator.setDirection(p.getDirection());
				passSuccess.add(p);
			}
		}
		elevator.incrementTimeInState();
		if (elevator.doneBoarding()) {
			elevator.setNumBoarded(0);
			elevator.setCurrFull(false);
			elevator.calculateBoardDelay(0);
			return Elevator.CLOSEDR;
		}
		return Elevator.BOARD;
	}
	
	/**
	 * Handles the CLOSEDR state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateCloseDr(int time) {
		elevator.closeOrOpenDoor();
		if (elevator.getDoorState() == Elevator.DOOR_IN_MOTION) return Elevator.CLOSEDR;
		int floorNum = elevator.getCurrFloor(), dir = elevator.getDirection();
		if (elevator.getNumPassInElevator() == 0) {
			callMgr.updateCallStatus();
			if (!callMgr.callPending()) return Elevator.STOP;
			if (callMgr.callsInDir(floorNum, dir)) return Elevator.MV1FLR;
			if (!floors[floorNum].empty(dir)) return Elevator.OPENDR;
			if (!floors[floorNum].empty(-dir)) {
				elevator.setDirection(-dir);
				return Elevator.OPENDR;
			}
			elevator.setDirection(-dir);
			return Elevator.MV1FLR;
		}
		return Elevator.MV1FLR;
	}
	
	/**
	 * Handles the MV1FLR state and transitions.
	 *
	 * @param time the time
	 * @return the next state
	 */
	private int currStateMv1Flr(int time) {
		elevator.moveElevator();
		int floorNum = elevator.getCurrFloor();
		if (elevator.getTimeInState() % elevator.getTicksPerFloor() == 0) {
			if (elevator.getPassByFloor()[floorNum].size() > 0) return Elevator.OPENDR;
			int dir = elevator.getDirection();
			if (!floors[floorNum].empty(dir)) return Elevator.OPENDR;
			if (elevator.getNumPassInElevator() == 0 && !callMgr.callsInDir(floorNum, dir) &&
					!floors[floorNum].empty(-dir)) {
				elevator.setDirection(-dir);
				return Elevator.OPENDR;
			}
		}
		return Elevator.MV1FLR;
	}
	
	/**
	 * Determines if the elevator state or floor changed.
	 *
	 * @return true, if the state or floor changed
	 */
	private boolean elevatorStateOrFloorChanged() {
		return (elevator.getPrevState() != elevator.getCurrState() || 
				elevator.getPrevFloor() != elevator.getCurrFloor());
	}
	
	/**
	 * Update elevator - this is called AFTER time has been incremented.
	 * -  Logs any state changes, if the have occurred,
	 * -  Calls appropriate method based upon currState to perform
	 *    any actions and calculate next state...
	 *
	 * @param time the time
	 */
	public void updateElevator(int time) {
		if (elevatorStateOrFloorChanged())
			logElevatorStateOrFloorChanged(time,elevator.getPrevState(),elevator.getCurrState(),
                    elevator.getPrevFloor(),elevator.getCurrFloor());

		switch (elevator.getCurrState()) {
		case Elevator.STOP: elevator.updateCurrState(currStateStop(time)); break;
		case Elevator.MVTOFLR: elevator.updateCurrState(currStateMvToFlr(time)); break;
		case Elevator.OPENDR: elevator.updateCurrState(currStateOpenDr(time)); break;
		case Elevator.OFFLD: elevator.updateCurrState(currStateOffLd(time)); break;
		case Elevator.BOARD: elevator.updateCurrState(currStateBoard(time)); break;
		case Elevator.CLOSEDR: elevator.updateCurrState(currStateCloseDr(time)); break;
		case Elevator.MV1FLR: elevator.updateCurrState(currStateMv1Flr(time)); break;
		}

	}
	
	/**
	 * Disables logging.
	 */
	public void disableLogging() {
		LOGGER.setLevel(Level.OFF);
	}

	/**
	 * Process passenger data. Do NOT change this - it simply dumps the 
	 * collected passenger data for successful arrivals and give ups. These are
	 * assumed to be ArrayLists...
	 */
	public void processPassengerData() {
		
		try {
			BufferedWriter out = fio.openBufferedWriter(passDataFile);
			out.write("ID,Number,From,To,WaitToBoard,TotalTime\n");
			for (Passengers p : passSuccess) {
				String str = p.getId()+","+p.getNumPass()+","+(p.getOnFloor()+1)+","+(p.getDestFloor()+1)+","+
				             (p.getBoardTime() - p.getTime())+","+(p.getTimeArrived() - p.getTime())+"\n";
				out.write(str);
			}
			for (Passengers p : gaveUp) {
				String str = p.getId()+","+p.getNumPass()+","+(p.getOnFloor()+1)+","+(p.getDestFloor()+1)+","+
				             p.getWaitTime()+",-1\n";
				out.write(str);
			}
			fio.closeFile(out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enable logging. Prints the initial configuration message.
	 * For testing, logging must be enabled BEFORE the run starts.
	 */
	public void enableLogging() {
		LOGGER.setLevel(Level.INFO);
			logElevatorConfig(elevator.getCapacity(),elevator.getTicksPerFloor(), elevator.getTicksDoorOpenClose(), 
					          elevator.getPassPerTick(), elevator.getCurrState(),elevator.getCurrFloor());
		
	}
	
	/**
	 * Close logs, and pause the timeline in the GUI.
	 *
	 * @param time the time
	 */
	public void closeLogs(int time) {
		if (LOGGER.getLevel() == Level.INFO) {
			logEndSimulation(time);
			fh.flush();
			fh.close();
		}
	}
	
	/**
	 * Prints the state.
	 *
	 * @param state the state
	 * @return the string
	 */
	private String printState(int state) {
		String str = "";
		
		switch (state) {
			case Elevator.STOP: 		str =  "STOP   "; break;
			case Elevator.MVTOFLR: 		str =  "MVTOFLR"; break;
			case Elevator.OPENDR:   	str =  "OPENDR "; break;
			case Elevator.CLOSEDR:		str =  "CLOSEDR"; break;
			case Elevator.BOARD:		str =  "BOARD  "; break;
			case Elevator.OFFLD:		str =  "OFFLD  "; break;
			case Elevator.MV1FLR:		str =  "MV1FLR "; break;
			default:					str =  "UNDEF  "; break;
		}
		return(str);
	}
	
	/**
	 * Log elevator config.
	 *
	 * @param capacity the capacity
	 * @param ticksPerFloor the ticks per floor
	 * @param ticksDoorOpenClose the ticks door open close
	 * @param passPerTick the pass per tick
	 * @param state the state
	 * @param floor the floor
	 */
	private void logElevatorConfig(int capacity, int ticksPerFloor, int ticksDoorOpenClose, 
			                       int passPerTick, int state, int floor) {
		LOGGER.info("CONFIG:   Capacity="+capacity+"   Ticks-Floor="+ticksPerFloor+"   Ticks-Door="+ticksDoorOpenClose+
				    "   Ticks-Passengers="+passPerTick+"   CurrState=" + (printState(state))+"   CurrFloor="+(floor+1));
	}
		
	/**
	 * Log elevator state changed.
	 *
	 * @param time the time
	 * @param prevState the prev state
	 * @param currState the curr state
	 * @param prevFloor the prev floor
	 * @param currFloor the curr floor
	 */
	private void logElevatorStateOrFloorChanged(int time, int prevState, int currState, int prevFloor, int currFloor) {
		LOGGER.info("Time="+time+"   Prev State: " + printState(prevState) + "   Curr State: "+printState(currState)
		            +"   PrevFloor: "+(prevFloor+1) + "   CurrFloor: " + (currFloor+1));
	}
	
	/**
	 * Log arrival.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param id the id
	 */
	private void logArrival(int time, int numPass, int floor,int id) {
		LOGGER.info("Time="+time+"   Arrived="+numPass+" Floor="+ (floor+1)
		            +" passID=" + id);						
	}
	
	/**
	 * Log calls.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logCalls(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Called="+numPass+" Floor="+ (floor +1)
			 	    +" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);
	}
	
	/**
	 * Log give up.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logGiveUp(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   GaveUp="+numPass+" Floor="+ (floor+1) 
				    +" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	/**
	 * Log skip.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logSkip(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Skip="+numPass+" Floor="+ (floor+1) 
			   	    +" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	/**
	 * Log board.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logBoard(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Board="+numPass+" Floor="+ (floor+1) 
				    +" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	/**
	 * Log end simulation.
	 *
	 * @param time the time
	 */
	private void logEndSimulation(int time) {
		LOGGER.info("Time="+time+"   Detected End of Simulation");
	}
}