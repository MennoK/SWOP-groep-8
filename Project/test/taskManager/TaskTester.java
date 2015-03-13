package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.activity.InvalidActivityException;

import org.junit.Before;
import org.junit.Test;

public class TaskTester {

	private LocalDateTime now;

	private Task baseTask;
	private Task dependentTask;
	private Task finishedTask;
	private Task failedTask;
	private Task level2DependentTask;

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
	public void getStatusAvailableDependentTask() {
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
		assertEquals(LocalDateTime.of(2015, 03, 03, 16, 0),
				baseTask.getEstimatedFinishTime());
	}

	@Test
	public void getEstimatedFinishTimeUvaillableTask() {
		assertEquals(LocalDateTime.of(2015, 03, 04, 16, 0),
				dependentTask.getEstimatedFinishTime());
	}

	@Test
	public void getEstimatedFinishTimeLevel2Task() {
		assertEquals(LocalDateTime.of(2015, 03, 05, 16, 0),
				level2DependentTask.getEstimatedFinishTime());
	}

	@Test
	public void testGetEstimatedFinishTime() {
		Task task = new Task("bla", Duration.ofHours(5 * 8), 0.5, now);
		assertEquals(now.plusDays(6).plusHours(8),
				task.getEstimatedFinishTime());
	}

	@Test
	public void getId() {
		Task newTask1 = new Task("new task 1", Duration.ofHours(8), 0.2, now);
		Task newTask2 = new Task("new task 2", Duration.ofHours(8), 0.2, now);

		assertEquals(newTask1.getId() + 1, newTask2.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createTaskWithDoubleDependency() {
		ArrayList<Task> dependencies = new ArrayList<Task>();
		dependencies.add(baseTask);
		dependencies.add(baseTask);
		new Task("new task 2", Duration.ofHours(8), 0.2, now, dependencies);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addAlreadyPresentDependency() {
		dependentTask.addDependency(baseTask);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingDependency() {
		baseTask.addDependency(dependentTask);
	}

	@Test
	public void hasDirectDependency() {
		assertTrue(dependentTask.hasDependency(baseTask));
	}

	@Test
	public void hasIndirectDependency() {
		assertTrue(level2DependentTask.hasDependency(baseTask));
	}

	@Test
	public void finishedEarly() throws InvalidActivityException {
		baseTask.updateStatus(now, now.plusHours(2), false);
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.EARLY);
	}

	@Test
	public void finishedWithADelay() throws InvalidActivityException {
		baseTask.updateStatus(now, now.plusDays(3), false);
		assertEquals(baseTask.getFinishStatus(),
				TaskFinishedStatus.WITH_A_DELAY);
	}

	@Test
	public void finishedOnTimeEarly() throws InvalidActivityException {
		baseTask.updateStatus(now, now.plusHours(7), false);
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test
	public void finishedOnTimeExact() throws InvalidActivityException {
		baseTask.updateStatus(now, now.plusHours(8), false);
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test
	public void finishedOnTimeLate() throws InvalidActivityException {
		baseTask.updateStatus(now, now.plusHours(8), false);
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test(expected = InvalidActivityException.class)
	public void taskIsNotFinishedYet() throws InvalidActivityException {
		baseTask.getFinishStatus();
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDuration() {
		new Task("desc", Duration.ofHours(-2), 2, now);
	}

	@Test(expected = IllegalArgumentException.class)
	public void zeroDuration() {
		new Task("desc", Duration.ofHours(0), 2, now);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidDeviation() {
		new Task("desc", Duration.ofHours(3), -2, now);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setEndTimeBeforeStartTime() {
		baseTask.updateStatus(now, now.minusDays(2), false);
	}

	@Test
	public void createAlternativeTask() {
		new Task("desc2", Duration.ofHours(3), 2, now, failedTask);
	}

	@Test
	public void createAlternativeTaskWithDep() {
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(baseTask);
		new Task("desc2", Duration.ofHours(3), 2, now, failedTask, dep);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAlternativeTaskWithAutoDep() {
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(failedTask);
		new Task("desc2", Duration.ofHours(3), 2, now, failedTask, dep);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAlternativeTaskWithIndirectAutoDep() {
		baseTask.updateStatus(now, now.plusDays(2), true);
		ArrayList<Task> dep = new ArrayList<Task>();
		dep.add(dependentTask);
		new Task("desc2", Duration.ofHours(3), 2, now, baseTask, dep);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setAlternativeTaskInvalidTaskNotFailed() {
		Task newTask = new Task("desc", Duration.ofHours(3), 2, now);
		new Task("desc2", Duration.ofHours(3), 2, now, newTask);
	}

	@Test
	public void update() {
		baseTask.update(now.plusDays(5));
		assertEquals(now.plusDays(5), baseTask.getLastUpdateTime());
	}
}
