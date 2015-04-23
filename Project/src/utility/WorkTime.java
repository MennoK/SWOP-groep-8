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
		WorkTimeSimulation wts = new WorkTimeSimulation(current, duration);
		return wts.workUntilFinished();
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
}
