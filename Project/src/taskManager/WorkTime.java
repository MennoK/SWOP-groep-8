package taskManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Groep 8
 * 
 */
public class WorkTime {

	private static final int STARTHOUR = 8;
	private static final int ENDHOUR = 16;

	private static final Set<DayOfWeek> WORKDAYS = Collections
			.unmodifiableSet(new HashSet<DayOfWeek>(Arrays
					.asList(new DayOfWeek[] { DayOfWeek.MONDAY,
							DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
							DayOfWeek.THURSDAY, DayOfWeek.FRIDAY })));

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

		Duration minutes = Duration.ofMinutes(0);
		LocalDateTime working = WorkTime.getFinishTime(first, minutes);
		while (working.isBefore(second)) {
			minutes = minutes.plusMinutes(60);
			working = WorkTime.getFinishTime(first, minutes);
		}

		return minutes;
	}

}
