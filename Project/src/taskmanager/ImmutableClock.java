package taskmanager;

import java.time.LocalDateTime;

public interface ImmutableClock {
	public LocalDateTime getCurrentTime();
}
