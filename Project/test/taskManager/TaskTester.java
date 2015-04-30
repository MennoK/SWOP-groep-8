package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import utility.TimeInterval;

public class TaskTester {

	private LocalDateTime now;

	private Task baseTask;
	private Task dependentTask;
	private Task finishedTask;
	private Task failedTask;
	private Task level2DependentTask;
	private Project project;

	@Before
	public void setUp() throws Exception {
		now = LocalDateTime.of(2015, 03, 03, 8, 0);
		project = new Project("proj", "descr", now, now.plusYears(1));

		Task.builder("a task", Duration.ofHours(8), 0.2).build(project);
		baseTask = project.getAllTasks().get(0);
		baseTask.setStatus(TaskStatus.AVAILABLE);

		Task.builder("a dependent task", Duration.ofHours(8), 0.2)
				.addDependencies(baseTask).build(project);
		dependentTask = project.getAllTasks().get(1);

		Task.builder("a finished task", Duration.ofHours(8), 0.2)
				.build(project);
		finishedTask = project.getAllTasks().get(2);
		finishedTask.setStatus(TaskStatus.AVAILABLE);
		finishedTask.setExecuting(now);
		finishedTask.setFinished(now.plusHours(2));

		Task.builder("a failed task", Duration.ofHours(8), 0.2).build(project);
		failedTask = project.getAllTasks().get(3);
		failedTask.setStatus(TaskStatus.AVAILABLE);
		failedTask.setExecuting(now);
		failedTask.setFailed(now.plusHours(2));

		Task.builder("a task dependent on all kind of tasks",
				Duration.ofHours(8), 0.2).addDependencies(finishedTask)
				.addDependencies(failedTask).addDependencies(dependentTask)
				.build(project);
		level2DependentTask = project.getAllTasks().get(4);
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
		Task.builder("bla", Duration.ofHours(5 * 8), 0.5).build(project);
		assertEquals(now.plusDays(6).plusHours(9), project.getAllTasks().get(0)
				.getEstimatedFinishTime());
	}

	@Test
	public void getId() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));

		Task.builder("new task 1", Duration.ofHours(8), 0.2).build(project);
		Task.builder("new task 2", Duration.ofHours(8), 0.2).build(project);

		assertEquals(project.getAllTasks().get(0).getId() + 1, project
				.getAllTasks().get(1).getId());
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
		baseTask.setExecuting(now);
		baseTask.setFinished(now.plusHours(2));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.EARLY);
	}

	@Test
	public void finishedWithADelay() {
		baseTask.setExecuting(now);
		baseTask.setFinished(now.plusDays(3));
		assertEquals(baseTask.getFinishStatus(),
				TaskFinishedStatus.WITH_A_DELAY);
	}

	@Test
	public void finishedOnTimeEarly() {
		baseTask.setExecuting(now);
		baseTask.setFinished(now.plusHours(7));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test
	public void finishedOnTimeExact() {
		baseTask.setExecuting(now);
		baseTask.setFinished(now.plusHours(8));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test
	public void finishedOnTimeLate() {
		baseTask.setExecuting(now);
		baseTask.setFinished(now.plusHours(9));
		assertEquals(baseTask.getFinishStatus(), TaskFinishedStatus.ON_TIME);
	}

	@Test(expected = IllegalStateException.class)
	public void taskIsNotFinishedYet() {
		baseTask.getFinishStatus();
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDuration() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		Task.builder("bla", Duration.ofHours(-1), 0.5).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void zeroDuration() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		Task.builder("bla", Duration.ofHours(0), 0.5).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDeviation() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		Task.builder("bla", Duration.ofHours(5 * 8), -2).build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setEndTimeBeforeStartTime() {
		baseTask.setExecuting(now);
		baseTask.setFinished(now.minusDays(2));
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
		baseTask.setExecuting(now);
		baseTask.setFinished(now.plusDays(2));
		Task.builder("desc2", Duration.ofHours(3), 2)
				.addDependencies(dependentTask).setOriginalTask(baseTask)
				.build(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setAlternativeTaskInvalidTaskNotFailed() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		Task.builder("bla", Duration.ofHours(5 * 8), 3).build(project);
		;
		Task.builder("desc2", Duration.ofHours(3), 2)
				.setOriginalTask(project.getAllTasks().get(0)).build(project);

	}

	@Test
	public void update() {
		baseTask.handleTimeChange(now.plusDays(5));
		assertEquals(now.plusDays(5), baseTask.getLastUpdateTime());
	}

	@Test
	public void addResourceType() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));
		ResourceType.builder("resourcetype").build(tmc);

		List<ResourceType> list = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		list.get(0).createResource("res1");

		Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(list.get(0), 1).build(project);
		assertEquals(1, project.getAllTasks().get(0).getRequiredResourceTypes()
				.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addAlreadyPresentResourceType() {
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));
		ResourceType.builder("resourcetype").build(tmc);
		List<ResourceType> list = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		list.get(0).createResource("res1");
		baseTask.addResourceType(list.get(0), 1);
		baseTask.addResourceType(list.get(0), 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addResourceTypeWithInvalidQuantity() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));
		ResourceType.builder("resourcetype").build(tmc);

		List<ResourceType> list = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		list.get(0).createResource("res1");

		Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(list.get(0), -1).build(project);
		assertEquals(1, project.getAllTasks().get(0).getRequiredResourceTypes()
				.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addResourceTypeWithNotEnoughResources() {
		Project project = new Project("proj", "descr", LocalDateTime.of(2014,
				03, 03, 8, 0), LocalDateTime.of(2016, 03, 03, 8, 0));
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));
		ResourceType.builder("resourcetype").build(tmc);

		List<ResourceType> listOfResourceTypes = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		listOfResourceTypes.get(0).createResource("res1");

		Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(listOfResourceTypes.get(0), 2)
				.build(project);
		assertEquals(1, project.getAllTasks().get(0).getRequiredResourceTypes()
				.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createTaskLongerThanAvailabilityResource() {
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));
		TimeInterval dailyAvailability = new TimeInterval(LocalTime.of(12, 0),
				LocalTime.of(17, 0));
		ResourceType.builder("resourcetype")
				.addDailyAvailability(dailyAvailability).build(tmc);
		List<ResourceType> listOfResourceTypes = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		listOfResourceTypes.get(0).createResource("res1");

		Task.builder("A task", Duration.ofHours(6), 1)
				.addRequiredResourceType(listOfResourceTypes.get(0), 1)
				.build(project);
	}

	@Test
	public void createTaskShorterThanAvailabilityResource() {
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));
		TimeInterval dailyAvailability = new TimeInterval(LocalTime.of(12, 0),
				LocalTime.of(17, 0));
		ResourceType.builder("resourcetype")
				.addDailyAvailability(dailyAvailability).build(tmc);
		List<ResourceType> listOfResourceTypes = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		listOfResourceTypes.get(0).createResource("res1");

		Task.builder("A task", Duration.ofHours(2), 1)
				.addRequiredResourceType(listOfResourceTypes.get(0), 1)
				.build(project);
	}

	@Test(expected = IllegalStateException.class)
	public void createTaskWithIncorrectRequiredResources() {
		TaskManController tmc = new TaskManController(LocalDateTime.of(2000,
				03, 05, 00, 00));

		ResourceType.builder("resourcetype").build(tmc);
		List<ResourceType> listOfResourceTypes = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		listOfResourceTypes.get(0).createResource("res1");

		ResourceType.builder("resourcetype")
				.addRequiredResourceTypes(listOfResourceTypes.get(0))
				.build(tmc);
		listOfResourceTypes = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		Task.builder("A task", Duration.ofHours(2), 1)
				.addRequiredResourceType(listOfResourceTypes.get(1), 1)
				.build(project);
	}

}
