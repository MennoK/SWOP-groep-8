package utility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WorkTimeSimulations simulate every work day
 * 
 * @author groep 8
 *
 */
public class WorkTimeSimulation {

	private LocalDateTime currentTime;
	private Duration timeLeft;

	/**
	 * Creates a new WorkTime Simulation
	 * 
	 * @param startTime
	 * @param hoursToWork
	 */
	public WorkTimeSimulation(LocalDateTime startTime, Duration hoursToWork) {
		this.currentTime = startTime;
		this.timeLeft = hoursToWork;
	}

	private void workInterval(WorkTimeInterval interval) {

		switch (interval.getType()) {
		case WORK:
			if (timeLeft.toMinutes() <= interval.getDuration().toMinutes()) {
				this.currentTime = this.currentTime.plus(timeLeft);
				this.timeLeft = Duration.ZERO;
			} else {
				if (!this.timeLeft.isZero()) {
					this.currentTime = this.currentTime.withHour(
							interval.getEnd().getHour()).withMinute(
							interval.getEnd().getMinute());
					this.timeLeft = this.timeLeft.minus(interval.getDuration());
				}
			}
			break;
		case FREE:
			// current time = end of interval
			if (!this.timeLeft.isZero()) {
				this.currentTime = this.currentTime.withHour(
						interval.getEnd().getHour()).withMinute(
						interval.getEnd().getMinute());
			}
			break;
		default:
			break;
		}
	}

	private void workDay(List<WorkTimeInterval> intervals) {
		for (WorkTimeInterval interval : intervals) {
			this.workInterval(interval);
		}
	}

	/**
	 * Calculates the the time you would be finished with the given parameters,
	 * taking in account the working hours
	 * 
	 * @return the finishtime
	 */
	public LocalDateTime workUntilFinished() {
		while (!this.timeLeft.isZero() && !this.timeLeft.isNegative()) {
			List<WorkTimeInterval> intervals = WorkDay
					.getScheduleOfDate(this.currentTime.toLocalDate());

			if (!this.currentTime.equals(this.currentTime.withHour(0)
					.withMinute(0))) {
				intervals = trimDayToCurrentTime(intervals);
			}

			this.workDay(intervals);
			if (!this.timeLeft.isZero()) {
				currentTime = this.currentTime.plusDays(1);
				currentTime = this.currentTime.withHour(0).withMinute(0);
			}
		}
		return currentTime;
	}

	private List<WorkTimeInterval> trimDayToCurrentTime(
			List<WorkTimeInterval> intervals) {
		while (!intervals.get(0).isTimeInInterval(
				this.currentTime.toLocalTime())) {
			intervals.remove(0);
		}
		WorkTimeInterval interval = intervals.get(0);
		interval.setBeginTime(this.currentTime.toLocalTime());

		return intervals;

	}

}
