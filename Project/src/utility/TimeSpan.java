package utility;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeSpan {
	private LocalDateTime begin;
	private LocalDateTime end;

	public TimeSpan(LocalDateTime begin, LocalDateTime end) {
		if (!isValid(begin, end))
			throw new IllegalArgumentException(
					"Begin of TimeSpan must be before end.");
		setBegin(begin);
		setEnd(end);
	}

	public TimeSpan(LocalDateTime begin, Duration duration) {
		this(begin, WorkTime.getFinishTime(begin, duration));
	}

	public boolean overlaps(TimeSpan other) {
		return other.contains(begin) || other.contains(end);
	}

	public boolean contains(LocalDateTime time) {
		return time.isAfter(begin) && time.isBefore(end);
	}

	private boolean isValid(LocalDateTime begin, LocalDateTime end) {
		return begin.isBefore(end);
	}

	public LocalDateTime getBegin() {
		return begin;
	}

	private void setBegin(LocalDateTime begin) {
		this.begin = begin;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	private void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public Duration getDuration() {
		return WorkTime.durationBetween(begin, end);
	}
}
