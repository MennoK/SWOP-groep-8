package taskManager;

import java.time.LocalDateTime;

/**
 * Time observer interface
 * 
 * @author groep 8
 *
 */
public interface TimeObserver {

	/**
	 * handles time changes
	 * 
	 * @param time
	 */
	void handleTimeChange(LocalDateTime time);

}
