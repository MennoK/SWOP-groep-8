package test.unitTests;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import TaskManager.LoopingDependencyException;
import TaskManager.Task;
import TaskManager.TaskStatus;

public class TaskTest {

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
		baseTask.setStartTime(now);

		dependentTask = new Task("a dependent task", Duration.ofHours(8), 0.2);
		dependentTask.addDependency(baseTask);

		finishedTask = new Task("a finished task", Duration.ofHours(8), 0.2);
		finishedTask.setEndTime(now);

		failedTask = new Task("a failed task", Duration.ofHours(8), 0.2);
		failedTask.setFailed(true);

		level2DependentTask = new Task("a task dependent on all kind of tasks",
				Duration.ofHours(8), 0.2);
		level2DependentTask.addDependency(finishedTask);
		level2DependentTask.addDependency(failedTask);
		level2DependentTask.addDependency(dependentTask);
	}

	@Test
	public void getStatusAvailableUndependentTask() {
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
	}

	@Test
	public void getStatusAvailableDependentTask() {
		baseTask.setEndTime(now);
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

	@Test(expected = LoopingDependencyException.class)
	public void dependencyLoopTest() throws LoopingDependencyException {
		baseTask.addDependency(dependentTask);
	}

	@Test(expected = LoopingDependencyException.class)
	public void level2dependencyLoopTest() throws LoopingDependencyException {
		baseTask.addDependency(level2DependentTask);
	}

}
