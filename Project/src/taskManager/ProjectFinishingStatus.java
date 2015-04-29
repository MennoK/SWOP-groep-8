package taskManager;

import java.time.Duration;
import utility.WorkTime;


/**
 * The ProjectFinishingStatus class describes an enumerator class that lists the
 * statuses how a project has finished. There are two possibilities: on time or
 * over time.
 * 
 * @author Groep 8
 */
public enum ProjectFinishingStatus {
	ON_TIME{

		@Override
		public Duration getDelay(Project project) {
			throw new IllegalStateException("an not ask the current delay of a task which is expected to finish on time");
		}
	}, 
	OVER_TIME{

		@Override
		public Duration getDelay(Project project) {
			Task latestFinishingTask = project.getAllTasks().get(0);

			for (Task task : project.getAllTasks()) {
				if (task.getEstimatedFinishTime().isAfter(
						latestFinishingTask.getEstimatedFinishTime())) {
					latestFinishingTask = task;
				}
			}
			return WorkTime.durationBetween(project.getDueTime(),
					latestFinishingTask.getEstimatedFinishTime());
		}

	};
	
	
	abstract Duration getDelay(Project project);
}

