package taskManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class WorkTime {

	int startHour = 8;
	int endHour = 16;
	
	static DayOfWeek[] workdays = new DayOfWeek[] { DayOfWeek.MONDAY,
			DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
			DayOfWeek.FRIDAY };

	long minutesToWork;

	LocalDateTime current;

	public WorkTime(LocalDateTime baseTime, Duration duration) {

		this.minutesToWork = duration.toMinutes();
		this.current = baseTime;

	}
	
	public WorkTime(LocalDateTime firstTime, LocalDateTime secondTime) {
		
	}

	public LocalDateTime getFinishTime() {
		if(current.getHour() < this.startHour)
		{
			current = current.plusHours(this.startHour - current.getHour());
			current = current.minusMinutes(current.getMinute());
		}
		
		while (minutesToWork > 0) {
			if (isWorkDay()) {
				workUntilEndOfDayOrMinutesRunOut();
			}
			if(minutesToWork > 0)
				setToNextDayStart();
		}

		return this.current;
	}

	private void setToNextDayStart() {
		current = current.plusDays(1);
		current = current.minusHours(current.getHour() - startHour);
		current = current.minusMinutes(current.getMinute());
	}

	private boolean isWorkDay() {
		

		if (Arrays.asList(workdays).contains(current.getDayOfWeek())) {
			return true;
		}
		return false;
	}

	private void workUntilEndOfDayOrMinutesRunOut() {
		while (current.getHour() < endHour && minutesToWork > 0) {
			current = current.plusMinutes(1);
			minutesToWork--;
		}

	}
	
	public static Duration durationBetween(LocalDateTime first, LocalDateTime second)
	{
		if(!first.isBefore(second)) {
			throw new IllegalArgumentException("first day is after the second");
		}
		
		Duration minutes = Duration.ofMinutes(0);
		LocalDateTime working = new WorkTime(first, minutes).getFinishTime();
		while(working.isBefore(second))
		{
			minutes = minutes.plusMinutes(60);
			working = new WorkTime(first, minutes).getFinishTime();
		}
		
		return minutes;
	}

}
