package taskmanager;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * 
 * The TaskManClock class implements the internal clock of the system.
 * 
 * @author Groep 8
 * 
 */

public class TaskManClock implements ImmutableClock {

	private LocalDateTime currentTime;

	private Memento memento;

	/**
	 * The constructor of the class TaskManClock sets the current time to the
	 * given time
	 * 
	 * @param startTime
	 *            : the given time
	 */
	public TaskManClock(LocalDateTime startTime) {
		this.currentTime = startTime;
	}

	/**
	 * Sets the new time of the system if and only if the given time is valid.
	 * An invalidTimeException will be thrown when the give time is invalid
	 * 
	 * @param newTime
	 *            : new time of the system
	 * @throws IllegalArgumentException
	 *             : thrown when the new time is not valid
	 */
	void setTime(LocalDateTime newTime) {
		if (!canHaveTime(newTime)) {
			throw new IllegalArgumentException(
					"The given time is before the current time");
		} else {
			this.currentTime = newTime;
		}
	}

	/**
	 * Determines if the class can have a new time (given as argument). The
	 * method returns true if and only if the given time is after or equal to
	 * the current time.
	 * 
	 * @param time
	 *            : given time
	 * @return true if and only if the given time is after or equal to the
	 *         current time
	 */
	private boolean canHaveTime(LocalDateTime time) {
		return time.isAfter(getCurrentTime()) || time.isEqual(getCurrentTime());
	}

	/**
	 * Saves the last state of the clock to a new memento
	 */
	void save() {
		this.memento = new Memento(this);
	}

	/**
	 * loads the last saved state of the clock
	 * 
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load(this);
		}
	}

	/**
	 * 
	 * Inner momento class of taskmanclock
	 * 
	 * @author groep 8
	 */
	private class Memento {
		private LocalDateTime currentTime;

		/**
		 * Constructor of the memento of taskManClock. Initialize the current
		 * time of the clock and all the observers of the current state
		 * 
		 * @param clock
		 *            : given taskmanclock
		 */
		public Memento(TaskManClock clock) {
			this.currentTime = clock.currentTime;
		}

		/**
		 * Sets the clock to the saved state
		 * 
		 * @param clock
		 *            : taskManClock
		 */
		public void load(TaskManClock clock) {
			clock.currentTime = this.currentTime;
		}
	}

	@Override
	public LocalDateTime getCurrentTime() {
		return this.currentTime;
	}
}
