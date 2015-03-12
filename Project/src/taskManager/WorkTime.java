package taskManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class WorkTime {

	int startHour = 8;
	int endHour = 16;

	long minutesToWork;

	LocalDateTime current;

	public WorkTime(LocalDateTime baseTime, Duration duration) {

		this.minutesToWork = duration.toMinutes();
		this.current = baseTime;

	}

	public LocalDateTime getFinishTime() {
		while (minutesToWork > 0) {
			if (isWorkDay()) {
				workUntilEndOfDayOrMinutesRunOut();
			}
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
		DayOfWeek[] workdays = new DayOfWeek[] { DayOfWeek.MONDAY,
				DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
				DayOfWeek.FRIDAY };

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

}
