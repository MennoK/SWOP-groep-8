package TaskManager;

import java.time.LocalDateTime;
/**
 * 
 * @author Groep 8
 *
 */
public class Clock {

	private LocalDateTime currentTime;

	/**
	 * @param startTime 
	 */
	public Clock(LocalDateTime startTime){
		this.currentTime = startTime;
	}

	//TODO commentaar
	public void setTime(LocalDateTime newTime) {
		if(!canHaveTime(newTime)){
			throw new IllegalArgumentException("The given time is before the current time");
		}
		else {
			this.currentTime = newTime;
		}
	}

	public boolean canHaveTime(LocalDateTime time){
		return time.isAfter(getTime()) || time.isEqual(getTime());
	}
	
	/**
	 * 
	 * @return
	 */
	public LocalDateTime getTime() {
		return currentTime;
	}
}
