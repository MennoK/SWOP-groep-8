package utility;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class WorkDay {
	
	private static final Set<DayOfWeek> WORKDAYS = Collections
			.unmodifiableSet(new HashSet<DayOfWeek>(Arrays
					.asList(new DayOfWeek[] { DayOfWeek.MONDAY,
							DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
							DayOfWeek.THURSDAY, DayOfWeek.FRIDAY })));
	
	private static final LocalTime STARTTIME = LocalTime.of(8, 0);
	private static final LocalTime BREAKSTART = LocalTime.of(12, 0);
	private static final LocalTime BREAKEND = LocalTime.of(13, 0);
	private static final LocalTime ENDTIME = LocalTime.of(17, 0);

	public static List<WorkTimeInterval> getScheduleOfDate(LocalDate date) {
		if(WORKDAYS.contains(date.getDayOfWeek())) {
			return getWorkDaySchedule();
		} else {
			return getFreeDaySchedule();
		}
	}
	
	private static List<WorkTimeInterval> getWorkDaySchedule() {
		ArrayList<WorkTimeInterval> schedule = new ArrayList<>();
		schedule.add(new WorkTimeInterval(LocalTime.of(0, 0), STARTTIME, IntervalType.FREE));
		schedule.add(new WorkTimeInterval(STARTTIME, BREAKSTART, IntervalType.WORK));
		schedule.add(new WorkTimeInterval(BREAKSTART, BREAKEND, IntervalType.FREE));
		schedule.add(new WorkTimeInterval(BREAKEND, ENDTIME, IntervalType.WORK));
		schedule.add(new WorkTimeInterval(ENDTIME, LocalTime.of(23, 59), IntervalType.FREE));
		return schedule;
	}
	
	private static List<WorkTimeInterval> getFreeDaySchedule() {
		ArrayList<WorkTimeInterval> schedule = new ArrayList<>();
		schedule.add(new WorkTimeInterval(LocalTime.of(0, 0), LocalTime.of(23, 59), IntervalType.FREE));
		return schedule;
	}

}
