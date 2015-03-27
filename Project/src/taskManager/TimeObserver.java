package taskManager;

import java.time.LocalDateTime;

public interface TimeObserver {
	
	void handleTimeChange(LocalDateTime time);

}
