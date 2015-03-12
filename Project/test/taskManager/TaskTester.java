package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.activity.InvalidActivityException;

import org.junit.Before;
import org.junit.Test;

import taskManager.Task;
import taskManager.TaskFinishedStatus;
import taskManager.exception.InvalidTimeException;
import taskManager.exception.LoopingDependencyException;

public class TaskTester {

	LocalDateTime now;

	Task baseTask;
	Task dependentTask;
	Task finishedTask;
	Task failedTask;
	Task level2DependentTask;

	@Before
	public void setUp() throws Exception {
		now = LocalDateTime.of(2015, 03, 03, 8, 0);
		baseTask = new Task("a task", Duration.ofHours(8), 0.2, now);

		ArrayList<Task> dependencies = new ArrayList<Task>();
		dependencies.add(baseTask);
		dependentTask = new Task("a dependent task", Duration.ofHours(8), 0.2,
				now, dependencies);

		finishedTask = new Task("a finished task", Duration.ofHours(8), 0.2,
				now);
		finishedTask.updateStatus(now, now.plusHours(2), false);

		failedTask = new Task("a failed task", Duration.ofHours(8), 0.2, now);
		failedTask.updateStatus(now, now.plusHours(2), true);

		ArrayList<Task> level2dependencies = new ArrayList<Task>();
		level2dependencies.add(finishedTask);
		level2dependencies.add(failedTask);
		level2dependencies.add(dependentTask);
		level2DependentTask = new Task("a task dependent on all kind of tasks",
				Duration.ofHours(8), 0.2, now, level2dependencies);
	}

	@Test
	public void getStatusAvailableUndependentTask() {
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
	}

	@Test
	public void getStatusAvailableDependentTask() throws InvalidTimeException {
		baseTask.updateStatus(now.minusDays(1), now, false);
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
	}

	@Test
	public void getStatusUnavailableTask() {
		assertEquals(TaskStatus.UNAVAILABLE, dependentTask.getStatus());
	}

	@Test
	public void getStatusFinishedTask() {
		assertEquals(TaskStatus.FINISHED, finishedTask.getStatus());
	}

	@Test
	public void getStatusFailedTask() {
		assertEquals(TaskStatus.FAILED, failedTask.getStatus());
	}

	@Test
	public void getStatusLevel2DependentTask() {
		assertEquals(TaskStatus.UNAVAILABLE, level2DependentTask.getStatus());
	}

	@Test
	public void getEstimatedFinishTimeAvaillableTask() {
		assertEquals(LocalDateTime.of(2015, 03, 04, 8, 0),
				baseTask.getEstimatedFinishTime());
	}

	@Test
	public void getEstimatedFinishTimeUvaillableTask() {
		assertEquals(LocalDateTime.of(2015, 03, 05, 8, 0),
				dependentTask.getEstimatedFinishTime());
	}

	@Test
	public void getEstimatedFinishTimeLevel2Task() {
		assertEquals(LocalDateTime.of(2015, 03, 06, 8, 0),
				level2DependentTask.getEstimatedFinishTime());
	}

	@Test
	public void getId() {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.2, now);
		Task newTask2 = new Task("new task 2", Duration.ofHours(8), 0.2, now);

		assertEquals(newTask1.getId() + 1, newTask2.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTaskWithDoubleDependency() {
		ArrayList<Task> dependencies = new ArrayList<Task>();
		dependencies.add(baseTask);
		dependencies.add(baseTask);
		new Task("new task 2", Duration.ofHours(8), 0.2, now, dependencies);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddAlreadyPresentDependency()
			throws LoopingDependencyException {
		dependentTask.addDependency(baseTask);
	}

	@Test(expected = LoopingDependencyException.class)
	public void testAddLoopingDependency() throws LoopingDependencyException {
		baseTask.addDependency(dependentTask);
	}

	@Test
	public void testHasDirectDependency() {
		assertTrue(dependentTask.hasDependency(baseTask));
	}

	@Test
	public void testHasIndirectDependency() {
		assertTrue(level2DependentTask.hasDependency(baseTask));
	}

	@Test
	public void testFinishedEarly() throws InvalidTimeException,
			InvalidActivityException {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.5, now);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 00),
				LocalDateTime.of(2015, 1, 1, 14, 00), false);

		assertEquals(newTask1.getFinishStatus(), TaskFinishedStatus.EARLY);

	}

	@Test
	public void testFinishedWithADelay() throws InvalidTimeException,
			InvalidActivityException {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.5, now);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 0),
				LocalDateTime.of(2015, 1, 2, 03, 0), false);

		assertEquals(newTask1.getFinishStatus(),
				TaskFinishedStatus.WITH_A_DELAY);
	}

	@Test
	public void testFinishedOnTime() throws InvalidTimeException,
			InvalidActivityException {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.5, now);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 0),
				LocalDateTime.of(2015, 1, 1, 17, 0), false);
		assertEquals(newTask1.getFinishStatus(), TaskFinishedStatus.ON_TIME);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 0),
				LocalDateTime.of(2015, 1, 2, 1, 0), false);
		assertEquals(newTask1.getFinishStatus(), TaskFinishedStatus.ON_TIME);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 0),
				LocalDateTime.of(2015, 1, 1, 18, 0), false);
		assertEquals(newTask1.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test(expected = InvalidActivityException.class)
	public void testTaskIsNotFinishedYet() throws InvalidActivityException {
		Task newTask = new Task("desc", Duration.ofHours(2), 2, now);
		newTask.getFinishStatus();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDuration() throws IllegalArgumentException {
		new Task("desc", Duration.ofHours(-2), 2, now);
		new Task("desc", Duration.ofHours(0), 2, now);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDeviation() throws IllegalArgumentException {
		new Task("desc", Duration.ofHours(3), -2, now);
	}

	@Test(expected = InvalidTimeException.class)
	public void testSetEndTimeBeforeStartTime() throws NullPointerException,
			InvalidTimeException {
		Task newTask = new Task("desc", Duration.ofHours(3), 2, now);
		newTask.updateStatus(now, now.minusDays(2), false);
	}

	@Test
	public void createAlternativeTask() throws InvalidTimeException {
		Task newTask = new Task("desc", Duration.ofHours(3), 2, now);
		newTask.updateStatus(now, now.plusDays(2), true);
		new Task("desc2", Duration.ofHours(3), 2, now, newTask);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setAlternativeTaskInvalidTaskNotFailed() {
		Task newTask = new Task("desc", Duration.ofHours(3), 2, now);
		new Task("desc2", Duration.ofHours(3), 2, now, newTask);
	}

	// TODO Status testing

}
