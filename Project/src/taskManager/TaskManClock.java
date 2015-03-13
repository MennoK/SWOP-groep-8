package taskManager;

import java.time.LocalDateTime;

/**
 * 
 * The TaskManClock class implements the internal clock of the system.
 * 
 * @author Groep 8
 *
 */

public class TaskManClock {

	private LocalDateTime currentTime;

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
		return time.isAfter(getTime()) || time.isEqual(getTime());
	}

	/**
	 * Returns the current time of the system
	 * 
	 * @return currentTime : current time of the system
	 */
	public LocalDateTime getTime() {
		return currentTime;
	}
}
