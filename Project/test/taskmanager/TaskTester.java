package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Project;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.TaskFinishedStatus;
import taskmanager.BranchOffice;
import taskmanager.TaskStatus;
import utility.TimeInterval;

public class TaskTester {

	private LocalDateTime time;

	private Task baseTask;
	private Task dependentTask;
	private Task finishedTask;
	private Task failedTask;
	private Task level2DependentTask;
	private Project project;

	@Before
	public void setUp() {
		time = LocalDateTime.of(2015, 03, 03, 8, 0);
		project = new Project("proj", "descr", time, time.plusYears(1));

		baseTask = Task.builder("a task", Duration.ofHours(8), 0.2).build(
				project);
		baseTask.setStatus(TaskStatus.AVAILABLE);

		dependentTask = Task
				.builder("a dependent task", Duration.ofHours(8), 0.2)
				.addDependencies(baseTask).build(project);

		finishedTask = Task
				.builder("a finished task", Duration.ofHours(8), 0.2).build(
						project);
		finishedTask.setStatus(TaskStatus.AVAILABLE);
		finishedTask.setExecuting(time);
		finishedTask.setFinished(time.plusHours(2));

		failedTask = Task.builder("a failed task", Duration.ofHours(8), 0.2)
				.build(project);
		failedTask.setStatus(TaskStatus.AVAILABLE);
		failedTask.setExecuting(time);
		failedTask.setFailed(time.plusHours(2));

		level2DependentTask = Task
				.builder("a task dependent on all kind of tasks",
						Duration.ofHours(8), 0.2).addDependencies(finishedTask)
				.addDependencies(failedTask).addDependencies(dependentTask)
				.build(project);
	}

	@Test
	public void getEstimatedFinishTimeAvaillableTask() {
		assertEquals(LocalDateTime.of(2015, 03, 03, 17, 0),
				baseTask.getEstimatedFinishTime());
	}

	@Test
	public void getEstimatedFinishTimeUvaillableTask() {
		assertEquals(LocalDateTime.of(2015, 03, 04, 17, 0),
				dependentTask.getEstimatedFinishTime());
	}

	@Test
	public void getEstimatedFinishTimeLevel2Task() {
		assertEquals(LocalDateTime.of(2015, 03, 05, 17, 0),
				level2DependentTask.getEstimatedFinishTime());
	}

	@Test
	public void testGetEstimatedFinishTime() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2015,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		Task task = Task.builder("bla", Duration.ofHours(5 * 8), 0.5).build(
				project);
		assertEquals(time.plusDays(6).plusHours(9),
				task.getEstimatedFinishTime());
	}

	@Test
	public void getId() {
		Task task1 = Task.builder("new task 1", Duration.ofHours(8), 0.2)
				.build(project);
		Task task2 = Task.builder("new task 2", Duration.ofHours(8), 0.2)
				.build(project);

		assertEquals(task1.getId() + 1, task2.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createTaskWithDoubleDependency() {
		Task.builder("new task 2", Duration.ofHours(8), 0.2)
				.addDependencies(baseTask).addDependencies(baseTask)
				.build(project);
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
	public void finishedEarly() {
		baseTask.setStatus(TaskStatus.AVAILABLE);
		baseTask.setExecuting(time);
		baseTask.setFinished(time.plusHours(2));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.EARLY);
	}

	@Test
	public void finishedWithADelay() {
		baseTask.setExecuting(time);
		baseTask.setFinished(time.plusDays(3));
		assertEquals(baseTask.getFinishStatus(),
				TaskFinishedStatus.WITH_A_DELAY);
	}

	@Test
	public void finishedOnTimeEarly() {
		baseTask.setExecuting(time);
		baseTask.setFinished(time.plusHours(7));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test
	public void finishedOnTimeExact() {
		baseTask.setExecuting(time);
		baseTask.setFinished(time.plusHours(8));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test
	public void finishedOnTimeLate() {
		baseTask.setExecuting(time);
		baseTask.setFinished(time.plusHours(9));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test(expected = IllegalStateException.class)
	public void taskIsNotFinishedYet() {
		baseTask.getFinishStatus();
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDuration() {
		Task.builder("bla", Duration.ofHours(-1), 0.5).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void zeroDuration() {
		Task.builder("bla", Duration.ofHours(0), 0.5).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDeviation() {
		Task.builder("bla", Duration.ofHours(5 * 8), -2).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setEndTimeBeforeStartTime() {
		baseTask.setExecuting(time);
		baseTask.setFinished(time.minusDays(2));
	}

	@Test
	public void createAlternativeTask() {
		Task.builder("desc2", Duration.ofHours(3), 2)
				.setOriginalTask(failedTask).build(project);
	}

	@Test
	public void createAlternativeTaskWithDep() {
		Task.builder("desc2", Duration.ofHours(3), 2).addDependencies(baseTask)
				.setOriginalTask(failedTask).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAlternativeTaskWithAutoDep() {
		Task.builder("desc2", Duration.ofHours(3), 2)
				.addDependencies(failedTask).setOriginalTask(failedTask)
				.build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAlternativeTaskWithIndirectAutoDep() {
		baseTask.setExecuting(time);
		baseTask.setFinished(time.plusDays(2));
		Task.builder("desc2", Duration.ofHours(3), 2)
				.addDependencies(dependentTask).setOriginalTask(baseTask)
				.build(project);
	}

	@Test
	public void createTaskWithCustomAmountOfDevelopers() {
		Task task = Task.builder("desc", Duration.ofHours(1), 1)
				.amountOfRequiredDevelopers(3).build(project);
		assertEquals(task.getAmountOfRequiredDevelopers(), 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setAlternativeTaskInvalidTaskNotFailed() {
		Task task = Task.builder("bla", Duration.ofHours(5 * 8), 3).build(
				project);
		Task.builder("desc2", Duration.ofHours(3), 2).setOriginalTask(task)
				.build(project);

	}

	@Test
	public void addResourceType() {
		BranchOffice tmc = new BranchOffice("here");
		ResourceType resType = ResourceType.builder("resourcetype").build(tmc);

		resType.createResource("res1");

		Task task = Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(resType, 1).build(project);
		assertEquals(1, task.getRequiredResourceTypes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addAlreadyPresentResourceType() {
		BranchOffice tmc = new BranchOffice("here");
		ResourceType resType = ResourceType.builder("resourcetype").build(tmc);
		resType.createResource("res1");
		// TODO discuss isn't the builder responsible for this?
		baseTask.addResourceType(resType, 1);
		baseTask.addResourceType(resType, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addResourceTypeWithInvalidQuantity() {
		BranchOffice tmc = new BranchOffice("here");
		ResourceType resType = ResourceType.builder("resourcetype").build(tmc);

		resType.createResource("res1");

		Task task = Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(resType, -1).build(project);
		assertEquals(1, task.getRequiredResourceTypes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addResourceTypeWithNotEnoughResources() {
		BranchOffice tmc = new BranchOffice("here");
		ResourceType resType = ResourceType.builder("resourcetype").build(tmc);

		resType.createResource("res1");

		Task task = Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(resType, 2).build(project);
		assertEquals(1, task.getRequiredResourceTypes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createTaskLongerThanAvailabilityResource() {
		BranchOffice tmc = new BranchOffice("here");
		TimeInterval dailyAvailability = new TimeInterval(LocalTime.of(12, 0),
				LocalTime.of(17, 0));
		ResourceType resType = ResourceType.builder("resourcetype")
				.addDailyAvailability(dailyAvailability).build(tmc);
		resType.createResource("res1");

		Task.builder("A task", Duration.ofHours(6), 1)
				.addRequiredResourceType(resType, 1).build(project);
	}

	@Test
	public void createTaskShorterThanAvailabilityResource() {
		BranchOffice tmc = new BranchOffice("here");
		TimeInterval dailyAvailability = new TimeInterval(LocalTime.of(12, 0),
				LocalTime.of(17, 0));
		ResourceType resType = ResourceType.builder("resourcetype")
				.addDailyAvailability(dailyAvailability).build(tmc);
		resType.createResource("res1");

		Task.builder("A task", Duration.ofHours(2), 1)
				.addRequiredResourceType(resType, 1).build(project);
	}

	@Test(expected = IllegalStateException.class)
	public void createTaskWithIncorrectRequiredResources() {
		BranchOffice tmc = new BranchOffice("here");

		ResourceType requirement = ResourceType.builder("resourcetype").build(
				tmc);
		requirement.createResource("res1");

		ResourceType resType = ResourceType.builder("resourcetype")
				.addRequiredResourceTypes(requirement).build(tmc);

		Task.builder("A task", Duration.ofHours(2), 1)
				.addRequiredResourceType(resType, 1).build(project);
	}

}
