package utility;

import java.time.LocalTime;

public class WorkTimeInterval extends TimeInterval {
	
	private WorkTimeIntervalType type;
	
	/**
	 * Constructor of timeInterval class.
	 *  
	 * @param beginTime : begin time of the interval
	 * @param endTime : end time of the interval
	 * @param type: The type of interval (see IntervalType)
	 */
	public WorkTimeInterval(LocalTime beginTime, LocalTime endTime, WorkTimeIntervalType type) {
		super(beginTime, endTime);
		this.type = type;
	}
	
	public WorkTimeIntervalType getType() {
		return this.type;
	}

}
