package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Project;
import taskmanager.ProjectFinishingStatus;
import taskmanager.ProjectStatus;
import taskmanager.Task;
import taskmanager.TaskStatus;

public class ProjectTester extends TaskManTester {

	private Project project;

	@Before
	public void setUp() {
		super.setUp();
		project = createStandardProject(time.plusDays(4));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeOnCreationTime() {
		new Project("project", "desc", time, time);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeBeforeCreationTime() {
		new Project("project", "desc", time, time.minusHours(1));
	}

	@Test
	public void testStatusEmptyProject() {
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testBaseProject() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));

		// Check the base data
		assertEquals("project", project.getName());
		assertEquals("desc", project.getDescription());
		assertEquals(time, project.getCreationTime());
		assertEquals(time.plusDays(4), project.getDueTime());

		// Check the presence and correctness of the created Task
		assertEquals(1, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());

		// Check the status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// finish Task
		tmc.setExecuting(baseTask, time);
		tmc.setFinished(baseTask, time.plusHours(8));

		// Check the status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testTwoTaskproject() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		Task task2 = createPlannedTask(project, Duration.ofHours(8));

		// Check project structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, task2.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(0, task2.getDependencies().size());

		// check status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set baseTask finished
		tmc.setExecuting(baseTask, time);
		tmc.setFinished(baseTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set task2 finished
		tmc.setExecuting(task2, time);
		tmc.setFinished(task2, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testAlternativeTaskProject() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		// Set baseTask to failed
		tmc.setExecuting(baseTask, time);
		tmc.setFailed(baseTask, time.plusHours(2));
		// Create alternativeTask
		Task alternativeTask = createPlannedAlternativeTask(project,
				Duration.ofHours(8), baseTask);

		// check structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(baseTask, alternativeTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(0, alternativeTask.getDependencies().size());

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, alternativeTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set alternativeTask to finished
		tmc.setExecuting(alternativeTask, time);
		tmc.setFinished(alternativeTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testDependentTaskProject() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		Task dependentTask = createPlannedTask(project, Duration.ofHours(8),
				baseTask);

		// check structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, dependentTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(1, dependentTask.getDependencies().size());
		assertTrue(dependentTask.getDependencies().contains(baseTask));

		// check status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set baseTask to finished
		tmc.setExecuting(baseTask, time);
		tmc.setFinished(baseTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set dependentTask to finished
		tmc.setExecuting(dependentTask, time);
		tmc.setFinished(dependentTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testProjectWithDependencyAndAlternativeTask() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		Task dependentTask = createPlannedTask(project, Duration.ofHours(8),
				baseTask);

		// Set baseTask to failed
		tmc.setExecuting(baseTask, time);
		tmc.setFailed(baseTask, time.plusHours(2));
		// Create alternativeTask
		Task alternativeTask = createPlannedAlternativeTask(project,
				Duration.ofHours(8), baseTask);

		// check structure
		assertEquals(3, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, dependentTask.getOriginal());
		assertEquals(baseTask, alternativeTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(1, dependentTask.getDependencies().size());
		assertTrue(dependentTask.getDependencies().contains(alternativeTask));
		assertEquals(0, alternativeTask.getDependencies().size());

		// double check
		assertFalse(dependentTask.hasDependency(baseTask));
		assertTrue(dependentTask.hasDependency(alternativeTask));

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, alternativeTask.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		tmc.setExecuting(alternativeTask, time);
		tmc.setFinished(alternativeTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		tmc.setExecuting(dependentTask, time);
		tmc.setFinished(dependentTask, time.plusHours(8));

		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskThatIsAlreadyInList() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		project.addTask(baseTask);
	}

	@Test
	public void testOverTime() {
		// Add a task that will take to long
		Task task = createPlannedTask(project, Duration.ofHours(3 * 8));

		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());

		// Let the task finish to late
		tmc.setExecuting(task, time);
		tmc.setFinished(task, time.plusDays(10));

		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayOnTime() {
		createPlannedTask(project, Duration.ofHours(8));
		project.getCurrentDelay();
	}

	@Test
	public void testDelayToMuchWork() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		Task.builder("bla", Duration.ofHours(2 * 8), 0.5)
				.addDependencies(baseTask).build(project);
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testDelayBaseTaskDelayed() {
		Task baseTask = createPlannedTask(project, Duration.ofHours(8));
		Task.builder("bla", Duration.ofHours(8), 0.5).addDependencies(baseTask)
				.build(project);
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// To delay task finish time with 8 work hours add 3*24.
		// 8 work hours = 24 real hours
		// + 2 days of weekend
		tmc.setExecuting(baseTask, time);
		tmc.setFinished(baseTask, time.plusHours(8).plusDays(3));
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(7), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayTwoTasks() {
		Task.builder("desc", Duration.ofHours(5 * 8), 0.5).build(project);
		Task.builder("desc", Duration.ofHours(4 * 8), 0.5).build(project);
		assertEquals(Duration.ofHours(3 * 8), project.getCurrentDelay());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayNoTask() {
		project.getCurrentDelay();
	}

}
