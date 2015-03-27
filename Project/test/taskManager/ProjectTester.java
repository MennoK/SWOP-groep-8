package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class ProjectTester {

	private Project project;
	private LocalDateTime now;

	@Before
	public void setUp() {
		this.now = LocalDateTime.of(2015, 03, 06, 8, 00);
		project = new Project("testname", "testdescription", now,
				now.plusDays(4));

	}

	@Test
	public void testDueTimeSetterAfterCreationTime() {
		project.setDueTime(LocalDateTime.of(2015, 03, 07, 00, 00));
		assertEquals(project.getDueTime(),
				LocalDateTime.of(2015, 03, 07, 00, 00));
	}

	@Test
	public void testDueTimeSetterOnCreationTime() {
		project.setDueTime(now);
		assertEquals(project.getDueTime(), now);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDueTimeSetterBeforeCreationTime() {
		project.setDueTime(LocalDateTime.of(2015, 03, 04, 00, 00));
	}

	@Test
	public void testCreateTaskStandard() {
		project.createTask("desc", Duration.ofHours(5), 20);
		assertEquals(1, project.getAllTasks().size());
		assertEquals(null, project.getAllTasks().get(0).getOriginal());
		assertEquals(0, project.getAllTasks().get(0).getDependencies().size());
	}

	@Test
	public void testCreateTaskStandardWithAlternative() {
		project.createTask("desc2", Duration.ofHours(5), 20);
		project.getAllTasks().get(0)
				.updateStatus(now, LocalDateTime.now(), true);
		project.createTask("desc", Duration.ofHours(5), 20, project
				.getAllTasks().get(0));

		assertEquals(2, project.getAllTasks().size());
		assertEquals(project.getAllTasks().get(0), project.getAllTasks().get(1)
				.getOriginal());
		assertEquals(0, project.getAllTasks().get(0).getDependencies().size());

	}

	@Test
	public void testCreateTaskStandardWithdependencies() {
		project.new TaskBuilder("desc", Duration.ofHours(5), 20).build();
		ArrayList<Task> dependency = new ArrayList<Task>();
		dependency.add(project.getAllTasks().get(0));
		project.new TaskBuilder("desc", Duration.ofHours(5), 20)
				.setDependencies(dependency).build();

		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, project.getAllTasks().get(1).getOriginal());
		assertEquals(1, project.getAllTasks().get(1).getDependencies().size());
	}

	@Test
	public void testAddTaskValidTasks() {
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		Task task2 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		project.addTask(task1);
		project.addTask(task2);

		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(task1);
		tasks.add(task2);
		assertEquals(2, project.getAllTasks().size());
		assertEquals(tasks, project.getAllTasks());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskThatIsAlreadyInList() {
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		project.addTask(task1);
		project.addTask(task1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskNullTask() {
		Task task1 = null;
		project.addTask(task1);

	}

	@Test
	public void testProjectStatusIsFinishedNoDependencies()
			throws NullPointerException {
		// 0 task
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// 1 task
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		task1.updateStatus(now, LocalDateTime.now(), false);
		project.addTask(task1);
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

		// 2 tasks
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now);
		task2.updateStatus(now, LocalDateTime.now(), false);
		project.addTask(task2);
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testProjectStatusIsFinishedDependenciesTwoTaskFinished()
			throws NullPointerException {
		// 1(finished) -> 2(finished)
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now);
		task1.updateStatus(now, LocalDateTime.now(), false);
		task2.updateStatus(now, LocalDateTime.now(), false);
		task2.addDependency(task1);
		project.addTask(task1);
		project.addTask(task2);
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testProjectStatusIsFinishedDependenciesOneTaskFailedAlternativeFinished() {

		// 1(finished) -> 2(finished)
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		project.addTask(task1);
		task1.updateStatus(now, LocalDateTime.now(), true);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now, task1);
		project.addTask(task2);

		task2.updateStatus(now, LocalDateTime.now(), false);
		task2.addDependency(task1);
		// 1(failed) -> 2(finished)
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testProjectStatusIsFinishedThreeTasksWithDependencies() {
		// 1(failed) -x-> 2(finished) <- 3(finished)
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		project.addTask(task1);
		task1.updateStatus(now, LocalDateTime.now(), true);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now, task1);
		project.addTask(task2);
		task2.updateStatus(now, LocalDateTime.now(), false);
		Task task3 = new Task("testdescriptionTask3", Duration.ofHours(8), 50,
				now);
		project.addTask(task3);
		task2.addDependency(task3);
		task3.updateStatus(now, LocalDateTime.now(), false);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.FINISHED, task3.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

	}

	@Test
	public void testProjectStatusIsOngoingNoDependencies() {
		// 1 task
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		project.addTask(task1);
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// 2 tasks
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now);
		project.addTask(task2);
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testProjectOngoingWithDependenciesAvailable() {
		ArrayList<Task> dependencies = new ArrayList<>();

		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		dependencies.add(task1);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now, dependencies);
		project.addTask(task1);
		project.addTask(task2);
		task1.updateStatus(now, LocalDateTime.now(), false);

		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testProjectOngoingWithDependenciesUnavailable() {
		ArrayList<Task> dependencies = new ArrayList<>();

		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50,
				now);
		dependencies.add(task1);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50,
				now, dependencies);
		project.addTask(task1);
		project.addTask(task2);

		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testProjectWithNoTasksIsOngoing() {
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testUpdate() {
		project.createTask("descr", Duration.ofHours(20), 20);

		project.update(now);
		assertEquals(now, project.getLastUpdateTime());
		assertEquals(now, project.getAllTasks().get(0).getLastUpdateTime());
	}

	@Test
	public void testDepRedirectedAfterCreateAlternativeTask() {
		project.new TaskBuilder("task1", Duration.ofHours(3), 0.5).build();
		Task task1 = project.getAllTasks().get(0);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(task1);
		project.new TaskBuilder("task2 (dep task1)", Duration.ofHours(5), 0.5)
				.setDependencies(dep).build();
		Task task2 = project.getAllTasks().get(1);
		project.new TaskBuilder("task4 (dep task1)", Duration.ofHours(5), 0.5)
				.setDependencies(dep).build();
		Task task4 = project.getAllTasks().get(2);
		task1.updateStatus(now, now.plusHours(4), true);
		project.new TaskBuilder("task3", Duration.ofHours(1), 0.5)
				.setOriginalTask(task1).build();
		Task task3 = project.getAllTasks().get(3);
		assertFalse(task2.hasDependency(task1));
		assertTrue(task2.hasDependency(task3));
		assertFalse(task4.hasDependency(task1));
		assertTrue(task4.hasDependency(task3));
	}

	@Test
	public void willFinishOnTime() {
		project.createTask("task1", Duration.ofHours(3), 0.5);
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		project.createTask("task2 (dep task1)", Duration.ofHours(20), 0.5);
		project.createTask("task4 (dep task1)", Duration.ofHours(20), 0.5);

		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
	}

	@Test
	public void isFinishOnTime() {
		project.createTask("task1", Duration.ofHours(3), 0.5);
		project.getAllTasks().get(0).updateStatus(now, now.plusHours(3), false);
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		project.createTask("task2 (dep task1)", Duration.ofHours(20), 0.5);
		project.getAllTasks().get(1).updateStatus(now, now.plusDays(10), false);
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayOnTime() {
		project.createTask("bla", Duration.ofHours(8), 0.5);
		project.getCurrentDelay();
	}

	@Test
	public void testGetCurrentDelayToLongTask() {
		project.createTask("bla", Duration.ofHours(3 * 8), 0.5);
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayToLateTask() {
		project.createTask("bla", Duration.ofHours(3 * 8), 0.5);
		Task task1 = project.getAllTasks().get(0);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(task1);
		project.createTask("bla", Duration.ofHours(2 * 8), 0.5, dep);
		task1.updateStatus(now, now.plusHours(4 * 8), false);
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayTwoTasks() {
		project.createTask("bla", Duration.ofHours(5 * 8), 0.5);
		project.createTask("bla", Duration.ofHours(4 * 8), 0.5);
		assertEquals(Duration.ofHours(3 * 8), project.getCurrentDelay());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayNoTask() {
		project.getCurrentDelay();
	}

}
