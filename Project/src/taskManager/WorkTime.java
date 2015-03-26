package taskManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Groep 8
 * 
 */
public class WorkTime {

	private static final int STARTHOUR = 8;
	private static final int ENDHOUR = 16;

	private static final List<DayOfWeek> WORKDAYS = Collections.unmodifiableList(Arrays.asList(new DayOfWeek[] {
			DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
			DayOfWeek.THURSDAY, DayOfWeek.FRIDAY }));

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
		if (current.getHour() < STARTHOUR) {
			current = current.plusHours(STARTHOUR - current.getHour());
			current = current.minusMinutes(current.getMinute());
		}

		long minutesToWork = duration.toMinutes();

		while (minutesToWork > 0) {
			if (isWorkDay(current)) {
				int oldHour = current.getHour();
				int oldMinute = current.getMinute();
				current = workUntilEndOfDayOrMinutesRunOut(current,
						minutesToWork);

				minutesToWork -= getTimeDifference(current, oldHour, oldMinute);

			}
			if (minutesToWork > 0)
				current = setToNextDayStart(current);
		}
		return current;
	}

	private static int getTimeDifference(LocalDateTime current, int oldHour,
			int oldMinute) {
		return ((current.getHour() - oldHour) * 60)
				+ (current.getMinute() - oldMinute);
	}

	private static LocalDateTime setToNextDayStart(LocalDateTime current) {
		current = current.plusDays(1);
		current = current.minusHours(current.getHour() - STARTHOUR);
		current = current.minusMinutes(current.getMinute());
		return current;
	}

	private static boolean isWorkDay(LocalDateTime current) {

		if (WORKDAYS.contains(current.getDayOfWeek())) {
			return true;
		}
		return false;
	}

	private static LocalDateTime workUntilEndOfDayOrMinutesRunOut(
			LocalDateTime current, long minutesToWork) {
		while (current.getHour() < ENDHOUR && minutesToWork > 0) {
			current = current.plusMinutes(60);
			minutesToWork-=60;
		}

		return current;

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
