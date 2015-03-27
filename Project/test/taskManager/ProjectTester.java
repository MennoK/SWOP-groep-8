package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class ProjectTester {

	private LocalDateTime time;
	private Project project;
	private Task baseTask;
	private Task dependentTask;
	private Task alternativeTask;

	/**
	 * create a project with one generic Task
	 */
	private void setUpBaseProject() {
		project.new TaskBuilder("desc", Duration.ofHours(8), 0.5).build();
		baseTask = getNewestTask(project);
	}

	/**
	 * create a project with dependentTask dependent on baseTask
	 */
	private void setUpProjectWithDependence() {
		setUpBaseProject();
		project.new TaskBuilder("desc", Duration.ofHours(8), 0.5)
				.addDependencies(baseTask).build();
		dependentTask = getNewestTask(project);
	}

	/**
	 * Helper method to extract a Task from a project just after creating it.
	 * 
	 * This is intentionally not trivial, such that user would not miss-use it,
	 * but is very handy for writting tests.
	 * 
	 * @param project
	 *            : The project to which the task was added
	 * @return The last Task added to the project
	 */
	private Task getNewestTask(Project project) {
		int numTasks = project.getAllTasks().size();
		return project.getAllTasks().get(numTasks - 1);
	}

	@Before
	public void setUp() {
		time = LocalDateTime.of(2015, 03, 06, 8, 00);
		project = new Project("project", "desc", time, time.plusDays(4));
	}

	@Test
	public void testDueTimeSetterAfterCreationTime() {
		project.setDueTime(time.plusHours(8));
		assertEquals(project.getDueTime(), time.plusHours(8));
	}

	@Test
	public void testDueTimeSetterOnCreationTime() {
		project.setDueTime(time);
		assertEquals(project.getDueTime(), time);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeSetterBeforeCreationTime() {
		project.setDueTime(time.minusHours(1));
	}

	@Test
	public void testStatusEmptyProject() {
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testProjectWithNoTasksIsOngoing() {
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testBaseProject() {
		setUpBaseProject();
		// Check the presence and correctness of the created Task
		assertEquals(1, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());

		// Check the status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// finish Task
		baseTask.updateStatus(time, time.plusHours(8), false);

		// Check the status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testTwoTaskproject() {
		setUpBaseProject();
		project.new TaskBuilder("desc", Duration.ofHours(8), 50).build();
		Task task2 = getNewestTask(project);

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
		baseTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set task2 finished
		task2.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testAlternativeTaskProject() {
		setUpBaseProject();
		// Set baseTask to failed
		baseTask.updateStatus(time, time.plusHours(2), true);
		// Create alternativeTask
		project.new TaskBuilder("desc", Duration.ofHours(8), 0.5)
				.setOriginalTask(baseTask).build();
		alternativeTask = getNewestTask(project);

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
		alternativeTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testDependentTaskProject() {
		setUpProjectWithDependence();

		// check structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, dependentTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(1, dependentTask.getDependencies().size());
		assertEquals(baseTask, dependentTask.getDependencies().get(0));

		// check status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set baseTask to finished
		baseTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set dependentTask to finished
		dependentTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testProjectWithDependencyAndAlternativeTask() {
		setUpProjectWithDependence();
		// Set baseTask to failed
		baseTask.updateStatus(time, time.plusHours(2), true);
		// Create alternativeTask
		project.new TaskBuilder("desc", Duration.ofHours(8), 0.5)
				.setOriginalTask(baseTask).build();
		alternativeTask = getNewestTask(project);

		// check structure
		assertEquals(3, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, dependentTask.getOriginal());
		assertEquals(baseTask, alternativeTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(1, dependentTask.getDependencies().size());
		assertEquals(alternativeTask, dependentTask.getDependencies().get(0));
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
		alternativeTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		dependentTask.updateStatus(time, time.plusHours(8), false);

		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskThatIsAlreadyInList() {
		setUpBaseProject();
		project.addTask(baseTask);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskNullTask() {
		project.addTask(null);
	}

	@Test
	public void testUpdate() {
		setUpBaseProject();

		project.handleTimeChange(time.plusHours(5));
		assertEquals(time.plusHours(5), project.getLastUpdateTime());
		assertEquals(time.plusHours(5), baseTask.getLastUpdateTime());
	}

	@Test
	public void testOverTime() {
		// Add a task that will take to long
		project.new TaskBuilder("task2 (dep task1)", Duration.ofHours(3 * 8),
				0.5).build();
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());

		// Let the task finish to late
		getNewestTask(project).updateStatus(time, time.plusDays(10), false);

		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayOnTime() {
		setUpBaseProject();
		project.getCurrentDelay();
	}

	@Test
	public void testDelayToMuchWork() {
		setUpBaseProject();
		project.new TaskBuilder("bla", Duration.ofHours(2 * 8), 0.5)
				.addDependencies(baseTask).build();
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testDelayBaseTaskDelayed() {
		setUpBaseProject();
		project.new TaskBuilder("bla", Duration.ofHours(8), 0.5)
				.addDependencies(baseTask).build();
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		baseTask.updateStatus(time, time.plusHours(2 * 8), false);
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayTwoTasks() {
		project.new TaskBuilder("desc", Duration.ofHours(5 * 8), 0.5).build();
		project.new TaskBuilder("desc", Duration.ofHours(4 * 8), 0.5).build();
		assertEquals(Duration.ofHours(3 * 8), project.getCurrentDelay());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayNoTask() {
		project.getCurrentDelay();
	}

}
