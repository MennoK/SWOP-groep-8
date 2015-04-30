package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;

import utility.WorkTime;

/**
 * This is meant to be a super class for all kind test classes which need to set
 * up standardized projects.
 *
 */
public class TaskManTester {

	LocalDateTime time;
	TaskManController tmc;

	@Before
	public void setUp() {
		time = LocalDateTime.of(2015, 03, 06, 8, 00);
		tmc = new TaskManController(time);
	}

	protected Project createStandardProject(LocalDateTime dueDate) {
		return tmc.getProjectExpert().createProject("project", "desc", dueDate);
	}

	protected Task createStandardTask(Project project, Duration taskDuration) {
		Task task = Task.builder("desc", taskDuration, 0.5).build(project);
		Developer dev = tmc.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev, tmc.getPlanner()).build();
		return task;
	}

	protected Task createDependentTask(Project project, Duration taskDuration,
			Task dependency) {
		Task task = Task.builder("desc", taskDuration, 0.5)
				.addDependencies(dependency).build(project);
		Developer dev = tmc.getDeveloperExpert().createDeveloper("dev");
		LocalDateTime depFinishTime = WorkTime.getFinishTime(time,
				dependency.getDuration());
		Planning.builder(depFinishTime, task, dev, tmc.getPlanner()).build();
		return task;
	}

	protected Task createAlternativeTask(Project project,
			Duration taskDuration, Task original) {
		Task task = Task.builder("desc", taskDuration, 0.5)
				.setOriginalTask(original).build(project);
		Developer dev = tmc.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev, tmc.getPlanner()).build();
		return task;
	}
}
