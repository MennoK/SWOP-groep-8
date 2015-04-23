package utility;

import java.time.DayOfWeek;
import java.time.temporal.TemporalUnit;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Groep 8
 * 
 */
public class WorkTime {

	/**
	 * Returns the time that is a duration after the given time, counting only
	 * work hours.
	 * 
	 * @param current
	 *            the time you want to start counting from
	 * @param duration
	 *            the duration in workhours/minutes
	 * @return the time the duration is finished
	 */
	public static LocalDateTime getFinishTime(LocalDateTime current,
			Duration duration) {
		long hoursLeft = duration.toHours();

		// Set to start of day
		if (current.getHour() < STARTHOUR) {
			current = current.plusHours(STARTHOUR - current.getHour());
		}

		while (hoursLeft > 0) {
			if (isWorkDay(current) && current.getHour() < ENDHOUR) {
				long hoursWorked = work(current, hoursLeft);
				hoursLeft -= hoursWorked;
				current = current.plusHours(hoursWorked);
			}
			if (hoursLeft > 0) {
				current = nextDay(current);
			}
		}

		return current;
	}

	private static long work(LocalDateTime current, long hoursLeft) {
		// Can finish today
		if ((current.getHour() + hoursLeft) <= ENDHOUR) {
			return hoursLeft;
		}
		return ENDHOUR - STARTHOUR;
	}

	private static LocalDateTime nextDay(LocalDateTime time) {
		time = time.plusDays(1);
		return time.plusHours(STARTHOUR - time.getHour());
	}

	private static boolean isWorkDay(LocalDateTime current) {

		if (WORKDAYS.contains(current.getDayOfWeek())) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates the duration in workhours between the two times
	 * 
	 * @param first
	 *            the time you want to start counting from
	 * @param second
	 *            the time until you want to count
	 * @return the duration in workhours between the two times
	 */
	public static Duration durationBetween(LocalDateTime first,
			LocalDateTime second) {
		if (!first.isBefore(second)) {
			throw new IllegalArgumentException("first day is after the second");
		}

		Duration hoursWorked = Duration.ofHours(0);
		LocalDateTime working = WorkTime.getFinishTime(first, hoursWorked);
		
		while (working.isBefore(second)) {
			hoursWorked = hoursWorked.plusHours(1);
			working = WorkTime.getFinishTime(working, Duration.ofHours(1));
		}

		return hoursWorked;
	}
	
	private class WorkTimeSimulation {
		private LocalDateTime currentTime;
		private Duration timeLeft;
		
		public WorkTimeSimulation(LocalDateTime startTime, Duration hoursToWork) {
			this.currentTime = startTime;
			this.timeLeft = hoursToWork;
		}
		
		private void workInterval(WorkTimeInterval interval) {
			
			switch(interval.getType()) {
			case WORK:
				this.timeLeft = this.timeLeft.minus(interval.getDuration());
				//NO BREAK
			case FREE:
				//current time = end of interval
				this.currentTime = this.currentTime.withHour(interval.getEnd().getHour()).withMinute(interval.getEnd().getMinute());
				break;
			default:
				break;
			}
		}
		
		private void workDay(List<WorkTimeInterval> intervals) {
			for(WorkTimeInterval interval : intervals) {
				this.workInterval(interval);
			}
		}
		
		public LocalDateTime workUntilFinished() {
			while(!this.timeLeft.isZero() && !this.timeLeft.isNegative()) {
				List<WorkTimeInterval> intervals = WorkDay.getScheduleOfDate(this.currentTime.toLocalDate());
				this.workDay(intervals);
				currentTime = this.currentTime.plusDays(1);
				currentTime = this.currentTime.withHour(0).withMinute(0);
			}
			return currentTime;
		}
		
		
	}

}
