package taskmanager;

import java.time.LocalDateTime;

/**
 * Immutable clock gives the current system time for
 * reading only
 * 
 * @author Groep 8
 *
 */
public interface ImmutableClock {
	public LocalDateTime getCurrentTime();
}
