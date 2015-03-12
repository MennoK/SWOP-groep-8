package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

import taskManager.exception.InvalidTimeException;
import taskManager.Project;
import taskManager.ProjectStatus;
import taskManager.Task;

public class ProjectTester {

	private Project project;
	private LocalDateTime now;

	@Before
	public void setUp() {
		this.now = LocalDateTime.of(2015, 03, 05, 00, 00);
		project = new Project("testname", "testdescription", now,
				now.plusDays(1));
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
		assertEquals(null, project.getAllTasks().get(0).getAlternativeFor());
		assertEquals(0, project.getAllTasks().get(0).getDependencies().size());
	}

	@Test
	public void testCreateTaskStandardWithAlternative()
			throws InvalidTimeException {
		project.createTask("desc2", Duration.ofHours(5), 20);
		project.getAllTasks().get(0)
				.updateStatus(now, LocalDateTime.now(), true);
		project.createTask("desc", Duration.ofHours(5), 20, project
				.getAllTasks().get(0));

		assertEquals(2, project.getAllTasks().size());
		assertEquals(project.getAllTasks().get(0), project.getAllTasks().get(1)
				.getAlternativeFor());
		assertEquals(0, project.getAllTasks().get(0).getDependencies().size());

	}

	@Test
	public void testCreateTaskStandardWithdependencies() {
		project.createTask("desc", Duration.ofHours(5), 20);
		ArrayList<Task> dependency = new ArrayList<Task>();
		dependency.add(project.getAllTasks().get(0));
		project.createTask("desc", Duration.ofHours(5), 20, dependency);

		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, project.getAllTasks().get(1).getAlternativeFor());
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
			throws NullPointerException, InvalidTimeException {
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
			throws NullPointerException, InvalidTimeException {
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
	public void testProjectStatusIsFinishedDependenciesOneTaskFailedAlternativeFinished()
			throws InvalidTimeException {

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
	public void testProjectStatusIsFinishedThreeTasksWithDependencies()
			throws InvalidTimeException {
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
	public void testProjectOngoingWithDependenciesAvailable()
			throws InvalidTimeException {
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

		project.update(LocalDateTime.now());
		assertEquals(LocalDateTime.now(), project.getLastUpdateTime());
		assertEquals(LocalDateTime.now(), project.getAllTasks().get(0)
				.getLastUpdateTime());
	}

	@Test
	public void testWillFinishOnTime() {

	}

	@Test
	public void testWillFinishOverTime() {

	}

	@Test
	public void testGetEstimatedFinishTime() {

	}

	@Test
	public void testGetTotalDelay() {

	}

	@Test
	public void testDepRedirectedAfterCreateAlternativeTask()
			throws InvalidTimeException {
		project.createTask("task1", Duration.ofHours(3), 0.5);
		Task task1 = project.getAllTasks().get(0);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(task1);
		project.createTask("task2 (dep task1)", Duration.ofHours(5), 0.5, dep);
		Task task2 = project.getAllTasks().get(1);
		project.createTask("task4 (dep task1)", Duration.ofHours(5), 0.5, dep);
		Task task4 = project.getAllTasks().get(2);
		task1.updateStatus(now, now.plusHours(4), true);
		project.createTask("task3", Duration.ofHours(1), 0.5, task1);
		Task task3 = project.getAllTasks().get(3);
		assertFalse(task2.hasDependency(task1));
		assertTrue(task2.hasDependency(task3));
		assertFalse(task4.hasDependency(task1));
		assertTrue(task4.hasDependency(task3));
	}

	@Test
	public void testGetCurrentDelayToLongTask() {
		project.createTask("bla", Duration.ofHours(16), 0.5);
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayToLateTask() throws InvalidTimeException {
		project.createTask("bla", Duration.ofHours(4), 0.5);
		Task task1 = project.getAllTasks().get(0);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(task1);
		project.createTask("bla", Duration.ofHours(4), 0.5, dep);
		task1.updateStatus(now, now.plusHours(6), false);
		assertEquals(Duration.ofHours(2), project.getCurrentDelay());
	}
}
