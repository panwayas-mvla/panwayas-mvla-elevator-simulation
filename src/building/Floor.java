package building;
// ListIterater can be used to look at the contents of the floor queues for 
// debug/display purposes...
import java.util.ListIterator;

import genericqueue.GenericQueue;
import passengers.Passengers;

// Owner: Michael Xu
/**
 * The Class Floor. This class provides the up/down queues to hold
 * Passengers as they wait for the Elevator.
 */
public class Floor {
	/**  Constant for representing direction. */
	private static final int UP = 1;
	
	/** The Constant DOWN. */
	private static final int DOWN = -1;

	/**  The queues to represent Passengers going UP or DOWN. */	
	private GenericQueue<Passengers> down;
	
	private GenericQueue<Passengers> up;

	/**
	 * Instantiates a new floor.
	 *
	 * @param qSize the q size
	 */
	public Floor(int qSize) {
		down = new GenericQueue<Passengers>(qSize);
		up = new GenericQueue<Passengers>(qSize);
	}
	
	// TODO: Write the helper methods needed for this class. 
	// You probably will only be accessing one queue at any
	// given time based upon direction - you could choose to 
	
	/**
	 * Returns true if the queue is empty
	 *
	 * @param dir the direction
	 * @return true, if queue is empty
	 */
	public boolean empty(int dir) {
		return (dir == UP) ? up.isEmpty() : down.isEmpty();
	}
	
	/**
	 * Adds the passenger group to the queue
	 *
	 * @param p the passenger group to add
	 */
	public void add(Passengers p) {
		if (p.getDirection() == UP) up.add(p);
		else down.add(p);
	}
	
	/**
	 * Gets the first element of the queue
	 *
	 * @param dir the direction
	 * @return the passenger group
	 */
	public Passengers peek(int dir) {
		return (dir == UP) ? up.peek() : down.peek();
	}
	
	/**
	 * Gets and removes the first element of the queue
	 *
	 * @param dir the direction
	 * @return the passenger group 
	 */
	public Passengers poll(int dir) {
		return (dir == UP) ? up.poll() : down.poll();
	}
	
	/**
	 * Queue string. This method provides visibility into the queue
	 * contents as a string. What exactly you would want to visualize 
	 * is up to you
	 *
	 * @param dir determines which queue to look at
	 * @return the string of queue contents
	 */
	String queueString(int dir) {
		String str = "";
		ListIterator<Passengers> list;
		list = (dir == UP) ?up.getListIterator() : down.getListIterator();
		if (list != null) {
			while (list.hasNext()) {
				// choose what you to add to the str here.
				str += "(" + list.next().getNumPass() + ")";
				if (list.hasNext()) str += " ";
			}
		}
		return str;	
	}
	
	
}
