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

	protected Task createTask(Project project, Duration taskDuration) {
		return Task.builder("desc", taskDuration, 0.5).build(project);
	}

	protected Task createPlannedTask(Project project, Duration taskDuration) {
		Task task = createTask(project, taskDuration);
		Developer dev = tmc.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev, tmc.getPlanner()).build();
		return task;
	}

	protected Task createTask(Project project, Duration taskDuration,
			Task dependency) {
		return Task.builder("desc", taskDuration, 0.5)
				.addDependencies(dependency).build(project);
	}

	protected Task createPlannedTask(Project project, Duration taskDuration,
			Task dependency) {
		Task task = createTask(project, taskDuration, dependency);
		Developer dev = tmc.getDeveloperExpert().createDeveloper("dev");
		LocalDateTime depFinishTime = WorkTime.getFinishTime(time,
				dependency.getDuration());
		Planning.builder(depFinishTime, task, dev, tmc.getPlanner()).build();
		return task;
	}

	protected Task createPlannedAlternativeTask(Project project,
			Duration taskDuration, Task original) {
		Task task = Task.builder("desc", taskDuration, 0.5)
				.setOriginalTask(original).build(project);
		Developer dev = tmc.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev, tmc.getPlanner()).build();
		return task;
	}

	protected Task createRessourceTask(Project project, Duration taskDuration,
			ResourceType type) {
		Task task = Task.builder("desc", taskDuration, 0.5)
				.addRequiredResourceType(type, 1).build(project);
		return task;
	}
}
