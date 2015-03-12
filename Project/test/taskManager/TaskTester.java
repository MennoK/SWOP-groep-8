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
		baseTask = new Task("a task", Duration.ofHours(8), 0.2);

		ArrayList<Task> dependencies = new ArrayList<Task>();
		dependencies.add(baseTask);
		dependentTask = new Task("a dependent task", Duration.ofHours(8), 0.2,
				dependencies);

		finishedTask = new Task("a finished task", Duration.ofHours(8), 0.2);
		finishedTask.updateStatus(now, now.plusHours(2), false);

		failedTask = new Task("a failed task", Duration.ofHours(8), 0.2);
		failedTask.updateStatus(now, now.plusHours(2), true);

		ArrayList<Task> level2dependencies = new ArrayList<Task>();
		level2dependencies.add(finishedTask);
		level2dependencies.add(failedTask);
		level2dependencies.add(dependentTask);
		level2DependentTask = new Task("a task dependent on all kind of tasks",
				Duration.ofHours(8), 0.2, level2dependencies);
	}

	@Test
	public void getStatusAvailableUndependentTask() {
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
	}

	@Test
	public void getStatusAvailableDependentTask() throws InvalidTimeException {
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
				baseTask.getEstimatedFinishTime(now));
	}

	@Test
	public void getEstimatedFinishTimeUvaillableTask() {
		assertEquals(LocalDateTime.of(2015, 03, 05, 8, 0),
				dependentTask.getEstimatedFinishTime(now));
	}

	@Test
	public void getEstimatedFinishTimeLevel2Task() {
		assertEquals(LocalDateTime.of(2015, 03, 06, 8, 0),
				level2DependentTask.getEstimatedFinishTime(now));
	}

	@Test
	public void getEstimatedFinishTimeUvaillableTaskOverDueDependence() {
		assertEquals(LocalDateTime.of(2015, 03, 06, 8, 0),
				dependentTask.getEstimatedFinishTime(LocalDateTime.of(2015, 03,
						05, 8, 0)));
	}

	@Test
	public void getId() {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.2);
		Task newTask2 = new Task("new task 2", Duration.ofHours(8), 0.2);

		assertEquals(newTask1.getId() + 1, newTask2.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGiveDoubleDependency() {
		ArrayList<Task> dependencies = new ArrayList<Task>();
		dependencies.add(baseTask);
		dependencies.add(baseTask);
		new Task("new task 2", Duration.ofHours(8), 0.2, dependencies);
	}

	@Test
	public void testFinishedEarly() throws InvalidTimeException,
			InvalidActivityException {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.5);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 00),
				LocalDateTime.of(2015, 1, 1, 14, 00), false);

		assertEquals(newTask1.getFinishStatus(), TaskFinishedStatus.EARLY);

	}

	@Test
	public void testFinishedWithADelay() throws InvalidTimeException,
			InvalidActivityException {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.5);
		newTask1.updateStatus(LocalDateTime.of(2015, 1, 1, 13, 0),
				LocalDateTime.of(2015, 1, 2, 03, 0), false);

		assertEquals(newTask1.getFinishStatus(),
				TaskFinishedStatus.WITH_A_DELAY);
	}

	@Test
	public void testFinishedOnTime() throws InvalidTimeException,
			InvalidActivityException {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.5);
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
		Task newTask = new Task("desc", Duration.ofHours(2), 2);
		newTask.getFinishStatus();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDuration() throws IllegalArgumentException {
		new Task("desc", Duration.ofHours(-2), 2);
		new Task("desc", Duration.ofHours(0), 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDeviation() throws IllegalArgumentException {
		new Task("desc", Duration.ofHours(3), -2);
	}

	@Test(expected = InvalidTimeException.class)
	public void testSetEndTimeBeforeStartTime() throws NullPointerException,
			InvalidTimeException {
		Task newTask = new Task("desc", Duration.ofHours(3), 2);
		newTask.updateStatus(now, now.minusDays(2), false);
	}

	@Test
	public void createAlternativeTask() throws InvalidTimeException {
		Task newTask = new Task("desc", Duration.ofHours(3), 2);
		newTask.updateStatus(now, now.plusDays(2), true);
		new Task("desc2", Duration.ofHours(3), 2, newTask);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setAlternativeTaskInvalidTaskNotFailed() {
		Task newTask = new Task("desc", Duration.ofHours(3), 2);
		new Task("desc2", Duration.ofHours(3), 2, newTask);
	}

	// TODO Status testing

}
