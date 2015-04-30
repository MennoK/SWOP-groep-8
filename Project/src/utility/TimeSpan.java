package utility;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * TimeSpan implements two localdateTime interval
 * 
 * @author groep 8
 *
 */
public class TimeSpan {
	private LocalDateTime begin;
	private LocalDateTime end;

	/**
	 * Initialize the interval from two time interval, the begin date time
	 * must be before end time
	 * 
	 * @param begin: begin local date time
	 * @param end : end local date time
	 */
	public TimeSpan(LocalDateTime begin, LocalDateTime end) {
		if (!isValid(begin, end))
			throw new IllegalArgumentException(
					"Begin of TimeSpan must be before end.");
		setBegin(begin);
		setEnd(end);
	}

	/**
	 * Initialize time span from begin time and a duration.
	 * 
	 * @param begin: begin local date time
	 * @param duration : duration
	 */
	public TimeSpan(LocalDateTime begin, Duration duration) {
		this(begin, WorkTime.getFinishTime(begin, duration));
	}

	/**
	 * checks whether a given time span overlaps with the
	 * current time span
	 * 
	 * @param other: other time span
	 */
	public boolean overlaps(TimeSpan other) {
		return !isBefore(other) && !isAfter(other);
	}

	/**
	 * checks whether the given time span is before the current time span 
	 */
	private boolean isBefore(TimeSpan other) {
		return !other.begin.isBefore(end);
	}

	/**
	 * checks whether the given time span is after the current time span 
	 */
	private boolean isAfter(TimeSpan other) {
		return !other.end.isAfter(begin);
	}


	/**
	 * checks whether the begin time and end time is valid. This
	 * is true if and only if the begin time is strictly 
	 * before the end time
	 */
	private boolean isValid(LocalDateTime begin, LocalDateTime end) {
		return begin.isBefore(end);
	}

	/**
	 * Returns the start time of the time span
	 * 
	 * @return begin : begin time of the time span
	 */
	public LocalDateTime getBegin() {
		return begin;
	}

	/**
	 * Sets the beginning of the time span
	 * 
	 * @param begin: begin time of the time span
	 */
	private void setBegin(LocalDateTime begin) {
		this.begin = begin;
	}

	/**
	 * Returns the end time of the time span
	 * 
	 * @return endtime : end time of the time span
	 */
	public LocalDateTime getEnd() {
		return end;
	}

	/**
	 * Sets the end time of the time span
	 * @param end: end time of the time span
	 */
	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	/**
	 * Returns the duration of time span
	 * 
	 * @return duration: duration of the time span
	 */
	public Duration getDuration() {
		return WorkTime.durationBetween(begin, end);
	}
	
	@Override
	public String toString() {
		return begin.toString() + " to " + end.toString();
	}
}
