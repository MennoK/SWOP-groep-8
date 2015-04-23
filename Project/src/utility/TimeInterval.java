package utility;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Time Interval class implements a begin and end time
 * for a daily availability of an object
 * 
 * @author Groep8
 *
 */
public class TimeInterval {

	private LocalTime beginTime;
	private LocalTime endTime;

	/**
	 * Constructor of timeInterval class.
	 *  
	 * @param beginTime : begin time of the interval
	 * @param endTime : end time of the interval
	 * @throws IllegalArgumentException if the end time is before the begin time
	 */
	public TimeInterval(LocalTime beginTime, LocalTime endTime){
		if(!isValidInterval(beginTime,endTime)){
			throw new IllegalArgumentException("The begin time is later than or equal to the end time");
		}
		else{
			setBeginTime(beginTime);
			setEndTime(endTime);
		}
	}

	/**
	 * checks whether the begin and end time are valid.
	 * 
	 * @param beginTime 
	 * @param endTime
	 * @return true if the begin time is before the endtime
	 */
	private boolean isValidInterval(LocalTime beginTime, LocalTime endTime){
		return beginTime.isBefore(endTime);
	}

	/**
	 * sets the begin time
	 * 
	 * @param beginTime
	 */
	private void setBeginTime(LocalTime beginTime){
		this.beginTime = beginTime;
	}
	
	/**
	 * sets the end time
	 * 
	 * @param endTime
	 */
	private void setEndTime(LocalTime endTime){
		this.endTime = endTime;
	}
	
	/**
	 * Returns the begin time
	 * 
	 * @return beginTime : the begin time of the interval
	 */
	public LocalTime getBegin(){
		return beginTime;
	}

	/**
	 * Returns the end time
	 * 
	 * @return endTime : the end time of the interval
	 */
	public LocalTime getEnd(){
		return endTime;
	}
	
	public boolean isTimeInInterval(LocalTime time) {
		return time.equals(beginTime) || (time.isAfter(beginTime) && time.isBefore(endTime));
	}
	
	
	public Duration getDuration() {
		return Duration.ofMinutes(ChronoUnit.MINUTES.between(this.getBegin(), this.getEnd()));
	}
}
