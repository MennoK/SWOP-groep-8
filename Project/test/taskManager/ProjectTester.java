package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.sound.midi.ControllerEventListener;

import org.junit.Before;
import org.junit.Test;

public class ProjectTester {

	private LocalDateTime time;
	private TaskManController controler;
	private Project project;
	private Task baseTask;
	private Task dependentTask;
	private Task alternativeTask;

	/**
	 * create a project with one generic Task
	 */
	private void setUpBaseProject() {
		project.taskBuilder("desc", Duration.ofHours(8), 0.5).build();
		baseTask = getNewestTask(project);
		controler.getPlanner()
				.createPlanning(time, baseTask, new Developer("Jef"))
				.build(controler.getPlanner());
	}

	/**
	 * create a project with dependentTask dependent on baseTask
	 */
	private void setUpProjectWithDependence() {
		setUpBaseProject();
		project.taskBuilder("desc", Duration.ofHours(8), 0.5)
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
		controler = new TaskManController(time);
		controler.getProjectExpert().createProject("project", "desc",
				time.plusDays(4));
		project = controler.getProjectExpert().getAllProjects().get(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeOnCreationTime() {
		project = new Project("project", "desc", time, time);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeBeforeCreationTime() {
		project = new Project("project", "desc", time, time.minusHours(1));
	}

	@Test
	public void testStatusEmptyProject() {
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testBaseProject() {
		setUpBaseProject();

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
		assertEquals(TaskStatus.AVAILABLE, baseTask.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// finish Task
		baseTask.updateStatus(time, time.plusHours(8), false);

		// Check the status
		assertEquals(TaskStatus.FINISHED, baseTask.getCalculatedStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testTwoTaskproject() {
		setUpBaseProject();
		project.taskBuilder("desc", Duration.ofHours(8), 50).build();
		Task task2 = getNewestTask(project);

		// Check project structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, task2.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(0, task2.getDependencies().size());

		// check status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set baseTask finished
		baseTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set task2 finished
		task2.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.FINISHED, task2.getCalculatedStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testAlternativeTaskProject() {
		setUpBaseProject();
		// Set baseTask to failed
		baseTask.updateStatus(time, time.plusHours(2), true);
		// Create alternativeTask
		project.taskBuilder("desc", Duration.ofHours(8), 0.5)
				.setOriginalTask(baseTask).build();
		alternativeTask = getNewestTask(project);

		// check structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(baseTask, alternativeTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(0, alternativeTask.getDependencies().size());

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.AVAILABLE,
				alternativeTask.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set alternativeTask to finished
		alternativeTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getCalculatedStatus());
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
		assertEquals(TaskStatus.AVAILABLE, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.UNAVAILABLE,
				dependentTask.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set baseTask to finished
		baseTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set dependentTask to finished
		dependentTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getCalculatedStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testProjectWithDependencyAndAlternativeTask() {
		setUpProjectWithDependence();
		// Set baseTask to failed
		baseTask.updateStatus(time, time.plusHours(2), true);
		// Create alternativeTask
		project.taskBuilder("desc", Duration.ofHours(8), 0.5)
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
		assertEquals(TaskStatus.FAILED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.AVAILABLE,
				alternativeTask.getCalculatedStatus());
		assertEquals(TaskStatus.UNAVAILABLE,
				dependentTask.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		alternativeTask.updateStatus(time, time.plusHours(8), false);

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getCalculatedStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getCalculatedStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		dependentTask.updateStatus(time, time.plusHours(8), false);

		assertEquals(TaskStatus.FAILED, baseTask.getCalculatedStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getCalculatedStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getCalculatedStatus());
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
		project.taskBuilder("task2 (dep task1)", Duration.ofHours(3 * 8), 0.5)
				.build();
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
		project.taskBuilder("bla", Duration.ofHours(2 * 8), 0.5)
				.addDependencies(baseTask).build();
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testDelayBaseTaskDelayed() {
		setUpBaseProject();
		project.taskBuilder("bla", Duration.ofHours(8), 0.5)
				.addDependencies(baseTask).build();
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// To delay task finish time with 8 work hours add 3*24.
		// 8 work hours = 24 real hours
		// + 2 days of weekend
		baseTask.updateStatus(time, time.plusHours(9).plusDays(3), false);
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayTwoTasks() {
		project.taskBuilder("desc", Duration.ofHours(5 * 8), 0.5).build();
		project.taskBuilder("desc", Duration.ofHours(4 * 8), 0.5).build();
		assertEquals(Duration.ofHours(3 * 8), project.getCurrentDelay());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayNoTask() {
		project.getCurrentDelay();
	}

}
