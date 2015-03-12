package taskManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class WorkTime {

	static final int startHour = 8;
	static final int endHour = 16;
	
	static final DayOfWeek[] workdays = new DayOfWeek[] { DayOfWeek.MONDAY,
			DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
			DayOfWeek.FRIDAY };
	

	static public LocalDateTime getFinishTime(LocalDateTime current, Duration duration) {
		if(current.getHour() < startHour)
		{
			current = current.plusHours(startHour - current.getHour());
			current = current.minusMinutes(current.getMinute());
		}
		
		long minutesToWork = duration.toMinutes();
		
		while (minutesToWork > 0) {
			if (isWorkDay(current)) {
				int oldHour = current.getHour();
				int oldMinute = current.getMinute();
				current = workUntilEndOfDayOrMinutesRunOut(current, minutesToWork);
				
				minutesToWork -= getTimeDifference(current, oldHour, oldMinute);
				
				
			}
			if(minutesToWork > 0)
				current = setToNextDayStart(current);
		}
		return current;
	}

	private static int getTimeDifference(LocalDateTime current, int oldHour,
			int oldMinute) {
		return ( (current.getHour() - oldHour) * 60 ) + ( current.getMinute() - oldMinute );
	}

	private static LocalDateTime setToNextDayStart(LocalDateTime current) {
		current = current.plusDays(1);
		current = current.minusHours(current.getHour() - startHour);
		current = current.minusMinutes(current.getMinute());
		return current;
	}

	private static boolean isWorkDay(LocalDateTime current) {
		
		if (Arrays.asList(workdays).contains(current.getDayOfWeek())) {
			return true;
		}
		return false;
	}

	private static LocalDateTime workUntilEndOfDayOrMinutesRunOut(LocalDateTime current, long minutesToWork) {
		while (current.getHour() < endHour && minutesToWork > 0) {
			current = current.plusMinutes(1);
			minutesToWork--;
		}
		
		return current;

	}
	
	public static Duration durationBetween(LocalDateTime first, LocalDateTime second) {
		if(!first.isBefore(second)) {
			throw new IllegalArgumentException("first day is after the second");
		}
		
		Duration minutes = Duration.ofMinutes(0);
		LocalDateTime working = WorkTime.getFinishTime(first, minutes);
		while(working.isBefore(second))
		{
			minutes = minutes.plusMinutes(60);
			working = WorkTime.getFinishTime(first, minutes);
		}
		
		return minutes;
	}

}
