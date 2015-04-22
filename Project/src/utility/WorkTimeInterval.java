package utility;

import java.time.LocalTime;

public class WorkTimeInterval extends TimeInterval {
	
	private IntervalType type;
	
	public WorkTimeInterval(LocalTime beginTime, LocalTime endTime, IntervalType type) {
		super(beginTime, endTime);
		this.type = type;
	}
	
	public IntervalType getType() {
		return this.type;
	}

}
