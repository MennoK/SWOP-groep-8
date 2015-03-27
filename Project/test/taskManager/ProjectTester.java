package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ProjectTester {

	/** The old standard project */
	private Project emptyProject;
	private LocalDateTime now;

	/**
	 * The new standard project (since most test create at least some standard
	 * Tasks)
	 */
	private Project standardProject;
	private Task task0;
	private List<Task> tasksStandardProject;

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
		now = LocalDateTime.of(2015, 03, 06, 8, 00);
		emptyProject = new Project("emptyProject", "desc", now, now.plusDays(4));
		standardProject = new Project("standardProject", "desc", now,
				now.plusDays(4));
		standardProject.new TaskBuilder("desc", Duration.ofHours(8), 50)
				.build();
		task0 = getNewestTask(standardProject);
		tasksStandardProject = new ArrayList<Task>();
		tasksStandardProject.add(task0);
	}

	@Test
	public void testDueTimeSetterAfterCreationTime() {
		emptyProject.setDueTime(LocalDateTime.of(2015, 03, 07, 00, 00));
		assertEquals(emptyProject.getDueTime(),
				LocalDateTime.of(2015, 03, 07, 00, 00));
	}

	@Test
	public void testDueTimeSetterOnCreationTime() {
		emptyProject.setDueTime(now);
		assertEquals(emptyProject.getDueTime(), now);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeSetterBeforeCreationTime() {
		emptyProject.setDueTime(LocalDateTime.of(2015, 03, 04, 00, 00));
	}

	@Test
	public void testCreateTask() {
		assertEquals(1, standardProject.getAllTasks().size());
		assertEquals(null, task0.getOriginal());
		assertEquals(0, task0.getDependencies().size());
	}

	@Test
	public void testCreateSecondTask() {
		standardProject.new TaskBuilder("desc", Duration.ofHours(8), 50)
				.build();
		Task newTask = getNewestTask(standardProject);
		tasksStandardProject.add(newTask);
		assertEquals(2, standardProject.getAllTasks().size());
		assertEquals(tasksStandardProject, standardProject.getAllTasks());
	}

	@Test
	public void testCreateAlternativeTask() {
		task0.updateStatus(now, LocalDateTime.now(), true);

		standardProject.new TaskBuilder("desc", Duration.ofHours(5), 20)
				.setOriginalTask(task0).build();
		Task newTask = getNewestTask(standardProject);

		assertEquals(2, standardProject.getAllTasks().size());
		assertEquals(task0, newTask.getOriginal());
		assertEquals(0, task0.getDependencies().size());

	}

	@Test
	public void testCreateTaskStandardWithDependencies() {
		standardProject.new TaskBuilder("desc", Duration.ofHours(5), 20)
				.setDependencies(tasksStandardProject).build();
		Task newTask = getNewestTask(standardProject);

		assertEquals(2, standardProject.getAllTasks().size());
		assertEquals(null, newTask.getOriginal());
		assertEquals(task0, newTask.getDependencies().get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskThatIsAlreadyInList() {
		standardProject.addTask(task0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskNullTask() {
		standardProject.addTask(null);
	}

	@Test
	public void testStatusEmptyProject() {
		assertEquals(ProjectStatus.ONGOING, emptyProject.getStatus());
	}

	@Test
	public void testFinishedProjectStatus() {
		// 1 task
		task0.updateStatus(now, LocalDateTime.now(), false);
		assertEquals(ProjectStatus.FINISHED, standardProject.getStatus());

		// 2 tasks
		standardProject.new TaskBuilder("desc", Duration.ofHours(8), 50)
				.build();
		getNewestTask(standardProject).updateStatus(now, LocalDateTime.now(),
				false);
		assertEquals(ProjectStatus.FINISHED, standardProject.getStatus());
	}

	@Test
	public void testFinishedProjectStatusWithDependencies() {
		// Create task1 (dependent on task0)
		standardProject.new TaskBuilder("desc", Duration.ofHours(8), 50)
				.setDependencies(tasksStandardProject).build();
		Task task1 = getNewestTask(standardProject);

		// set task0 to finished
		task0.updateStatus(now, LocalDateTime.now(), false);
		// set task1 to finished
		task1.updateStatus(now, LocalDateTime.now(), false);

		// 1(finished) -> 2(finished)
		assertEquals(TaskStatus.FINISHED, task0.getStatus());
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(ProjectStatus.FINISHED, standardProject.getStatus());
	}

	@Test
	public void testFinishedProjectStatusWithAlternativeTask() {
		// set task0 to failed
		task0.updateStatus(now, LocalDateTime.now(), true);

		// create task1 (alternative to task0)
		standardProject.new TaskBuilder("desc", Duration.ofHours(8), 50)
				.setOriginalTask(task0).build();
		Task task1 = getNewestTask(standardProject);

		// set task1 to finished
		task1.updateStatus(now, now.plusHours(8), false);

		// 1(failed) -> 2(finished)
		assertEquals(TaskStatus.FAILED, task0.getStatus());
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(ProjectStatus.FINISHED, standardProject.getStatus());
	}

	@Test
	public void testProjectStatusIsFinishedThreeTasksWithDependencies() {
		// TODO refactor Project Tester further

		// 1(failed) -x-> 2(finished) <- 3(finished)
		// TODO Can this not be done with TaskBuilder?
		Task task1 = new Task("desc", Duration.ofHours(8), 50, now, null,
				new ArrayList<Task>());
		emptyProject.addTask(task1);
		task1.updateStatus(now, LocalDateTime.now(), true);
		// TODO Can this not be done with TaskBuilder?
		Task task2 = new Task("desc", Duration.ofHours(8), 50, now, task1,
				new ArrayList<Task>());
		emptyProject.addTask(task2);
		task2.updateStatus(now, LocalDateTime.now(), false);
		// TODO Can this not be done with TaskBuilder?
		Task task3 = new Task("desc", Duration.ofHours(8), 50, now, null,
				new ArrayList<Task>());
		emptyProject.addTask(task3);
		task2.addDependency(task3);
		task3.updateStatus(now, LocalDateTime.now(), false);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.FINISHED, task3.getStatus());
		assertEquals(ProjectStatus.FINISHED, emptyProject.getStatus());

	}

	@Test
	public void testProjectStatusIsOngoingNoDependencies() {
		// 1 task
		// TODO Can this not be done with TaskBuilder?
		Task task1 = new Task("desc", Duration.ofHours(8), 50, now, null,
				new ArrayList<Task>());
		emptyProject.addTask(task1);
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(ProjectStatus.ONGOING, emptyProject.getStatus());

		// 2 tasks
		// TODO Can this not be done with TaskBuilder?
		Task task2 = new Task("desc", Duration.ofHours(8), 50, now, null,
				new ArrayList<Task>());
		emptyProject.addTask(task2);
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, emptyProject.getStatus());
	}

	@Test
	public void testProjectOngoingWithDependenciesAvailable() {
		ArrayList<Task> dependencies = new ArrayList<>();

		// TODO Can this not be done with TaskBuilder?
		Task task1 = new Task("desc", Duration.ofHours(8), 50, now, null,
				new ArrayList<Task>());
		dependencies.add(task1);
		// TODO Can this not be done with TaskBuilder?
		Task task2 = new Task("desc", Duration.ofHours(8), 50, now, null,
				dependencies);
		emptyProject.addTask(task1);
		emptyProject.addTask(task2);
		task1.updateStatus(now, LocalDateTime.now(), false);

		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, emptyProject.getStatus());
	}

	@Test
	public void testProjectOngoingWithDependenciesUnavailable() {
		ArrayList<Task> dependencies = new ArrayList<>();

		// TODO Can this not be done with TaskBuilder?
		Task task1 = new Task("desc", Duration.ofHours(8), 50, now, null,
				new ArrayList<Task>());
		dependencies.add(task1);
		// TODO Can this not be done with TaskBuilder?
		Task task2 = new Task("desc", Duration.ofHours(8), 50, now, null,
				dependencies);
		emptyProject.addTask(task1);
		emptyProject.addTask(task2);

		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, emptyProject.getStatus());
	}

	@Test
	public void testProjectWithNoTasksIsOngoing() {
		assertEquals(ProjectStatus.ONGOING, emptyProject.getStatus());
	}

	@Test
	public void testUpdate() {
		emptyProject.new TaskBuilder("desc", Duration.ofHours(20), 20).build();

		emptyProject.update(now);
		assertEquals(now, emptyProject.getLastUpdateTime());
		assertEquals(now, emptyProject.getAllTasks().get(0).getLastUpdateTime());
	}

	@Test
	public void testDepRedirectedAfterCreateAlternativeTask() {
		emptyProject.new TaskBuilder("task1", Duration.ofHours(3), 0.5).build();
		Task task1 = emptyProject.getAllTasks().get(0);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(task1);
		emptyProject.new TaskBuilder("task2 (dep task1)", Duration.ofHours(5),
				0.5).setDependencies(dep).build();
		Task task2 = emptyProject.getAllTasks().get(1);
		emptyProject.new TaskBuilder("task4 (dep task1)", Duration.ofHours(5),
				0.5).setDependencies(dep).build();
		Task task4 = emptyProject.getAllTasks().get(2);
		task1.updateStatus(now, now.plusHours(4), true);
		emptyProject.new TaskBuilder("task3", Duration.ofHours(1), 0.5)
				.setOriginalTask(task1).build();
		Task task3 = emptyProject.getAllTasks().get(3);
		assertFalse(task2.hasDependency(task1));
		assertTrue(task2.hasDependency(task3));
		assertFalse(task4.hasDependency(task1));
		assertTrue(task4.hasDependency(task3));
	}

	@Test
	public void willFinishOnTime() {
		emptyProject.new TaskBuilder("task1", Duration.ofHours(3), 0.5).build();
		assertEquals(ProjectFinishingStatus.ON_TIME,
				emptyProject.finishedOnTime());

		emptyProject.new TaskBuilder("task2 (dep task1)", Duration.ofHours(20),
				0.5).build();
		emptyProject.new TaskBuilder("task4 (dep task1)", Duration.ofHours(20),
				0.5).build();

		assertEquals(ProjectFinishingStatus.OVER_TIME,
				emptyProject.finishedOnTime());
	}

	@Test
	public void isFinishOnTime() {
		emptyProject.new TaskBuilder("task1", Duration.ofHours(3), 0.5).build();
		emptyProject.getAllTasks().get(0)
				.updateStatus(now, now.plusHours(3), false);
		assertEquals(ProjectFinishingStatus.ON_TIME,
				emptyProject.finishedOnTime());

		emptyProject.new TaskBuilder("task2 (dep task1)", Duration.ofHours(20),
				0.5).build();
		emptyProject.getAllTasks().get(1)
				.updateStatus(now, now.plusDays(10), false);
		assertEquals(ProjectFinishingStatus.OVER_TIME,
				emptyProject.finishedOnTime());
		assertEquals(ProjectStatus.FINISHED, emptyProject.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayOnTime() {
		emptyProject.new TaskBuilder("desc", Duration.ofHours(8), 0.5).build();
		emptyProject.getCurrentDelay();
	}

	@Test
	public void testGetCurrentDelayToLongTask() {
		emptyProject.new TaskBuilder("desc", Duration.ofHours(3 * 8), 0.5)
				.build();
		assertEquals(Duration.ofHours(8), emptyProject.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayToLateTask() {
		emptyProject.new TaskBuilder("desc", Duration.ofHours(3 * 8), 0.5)
				.build();
		Task task1 = emptyProject.getAllTasks().get(0);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(task1);
		emptyProject.new TaskBuilder("bla", Duration.ofHours(2 * 8), 0.5)
				.setDependencies(dep).build();
		task1.updateStatus(now, now.plusHours(4 * 8), false);
		assertEquals(Duration.ofHours(8), emptyProject.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayTwoTasks() {
		emptyProject.new TaskBuilder("desc", Duration.ofHours(5 * 8), 0.5)
				.build();
		emptyProject.new TaskBuilder("desc", Duration.ofHours(4 * 8), 0.5)
				.build();
		assertEquals(Duration.ofHours(3 * 8), emptyProject.getCurrentDelay());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayNoTask() {
		emptyProject.getCurrentDelay();
	}

}
