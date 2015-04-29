package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import utility.WorkTime;

public class ProjectTester {

	private LocalDateTime time;
	private TaskManController controller;
	private Project project;
	private Task baseTask;
	private Task dependentTask;


	@Before
	public void setUp() {
		time = LocalDateTime.of(2015, 03, 06, 8, 00);
		controller = new TaskManController(time);
		project = controller.getProjectExpert().createProject("project", "desc",
				time.plusDays(4));
	}
	
	private Task createStandardTask(Duration taskDuration) {
		Task task = Task.builder("desc", taskDuration, 0.5).build(project);
<<<<<<< HEAD
		Developer dev = controler.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev,controler.getPlanner())
				.build();
=======
		Developer dev = controller.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev)
				.build(controller.getPlanner());
>>>>>>> cb5c296b169d7b136b79ff425838b7655cd21bc9
		return task;
	}

	private Task createDependentTask(Duration taskDuration, Task dependency) {
		Task task = Task.builder("desc", taskDuration, 0.5)
				.addDependencies(dependency).build(project);
		Developer dev = controller.getDeveloperExpert().createDeveloper("dev");
		LocalDateTime depFinishTime = WorkTime.getFinishTime(time,
				dependency.getDuration());
<<<<<<< HEAD
		Planning.builder(depFinishTime, task, dev,controler.getPlanner())
				.build();
=======
		Planning.builder(depFinishTime, task, dev)
				.build(controller.getPlanner());
>>>>>>> cb5c296b169d7b136b79ff425838b7655cd21bc9
		return task;
	}

	private Task createAlternativeTask(Duration taskDuration, Task original) {
		Task task = Task.builder("desc", taskDuration, 0.5)
				.setOriginalTask(original).build(project);
<<<<<<< HEAD
		Developer dev = controler.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev,controler.getPlanner())
				.build();
=======
		Developer dev = controller.getDeveloperExpert().createDeveloper("dev");
		Planning.builder(time, task, dev)
				.build(controller.getPlanner());
>>>>>>> cb5c296b169d7b136b79ff425838b7655cd21bc9
		return task;
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
		baseTask = createStandardTask(Duration.ofHours(8));

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
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// finish Task
		controller.setExecuting(baseTask, time);
		controller.setFinished(baseTask, time.plusHours(8));

		// Check the status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testTwoTaskproject() {
		baseTask = createStandardTask(Duration.ofHours(8));
		Task task2 = createStandardTask(Duration.ofHours(8));

		// Check project structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, task2.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(0, task2.getDependencies().size());

		// check status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set baseTask finished
		controller.setExecuting(baseTask, time);
		controller.setFinished(baseTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// set task2 finished
		controller.setExecuting(task2, time);
		controller.setFinished(task2, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test
	public void testAlternativeTaskProject() {
		baseTask = createStandardTask(Duration.ofHours(8));
		// Set baseTask to failed
		controller.setExecuting(baseTask, time);
		controller.setFailed(baseTask, time.plusHours(2));
		// Create alternativeTask
		Task alternativeTask = createAlternativeTask(Duration.ofHours(8),
				baseTask);

		// check structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(baseTask, alternativeTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(0, alternativeTask.getDependencies().size());

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, alternativeTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set alternativeTask to finished
		controller.setExecuting(alternativeTask, time);
		controller.setFinished(alternativeTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testDependentTaskProject() {
		baseTask = createStandardTask(Duration.ofHours(8));
		dependentTask = createDependentTask(Duration.ofHours(8), baseTask);

		// check structure
		assertEquals(2, project.getAllTasks().size());
		assertEquals(null, baseTask.getOriginal());
		assertEquals(null, dependentTask.getOriginal());
		assertEquals(0, baseTask.getDependencies().size());
		assertEquals(1, dependentTask.getDependencies().size());
		assertEquals(baseTask, dependentTask.getDependencies().get(0));

		// check status
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set baseTask to finished
		controller.setExecuting(baseTask, time);
		controller.setFinished(baseTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set dependentTask to finished
		controller.setExecuting(dependentTask, time);
		controller.setFinished(dependentTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test
	public void testProjectWithDependencyAndAlternativeTask() {
		baseTask = createStandardTask(Duration.ofHours(8));
		dependentTask = createDependentTask(Duration.ofHours(8), baseTask);

		// Set baseTask to failed
		controller.setExecuting(baseTask, time);
		controller.setFailed(baseTask, time.plusHours(2));
		// Create alternativeTask
		Task alternativeTask = createAlternativeTask(Duration.ofHours(8),
				baseTask);

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
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, alternativeTask.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		controller.setExecuting(alternativeTask, time);
		controller.setFinished(alternativeTask, time.plusHours(8));

		// check status
		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(TaskStatus.AVAILABLE, dependentTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// set to finished
		controller.setExecuting(dependentTask, time);
		controller.setFinished(dependentTask, time.plusHours(8));

		assertEquals(TaskStatus.FAILED, baseTask.getStatus());
		assertEquals(TaskStatus.FINISHED, alternativeTask.getStatus());
		assertEquals(TaskStatus.FINISHED, dependentTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskThatIsAlreadyInList() {
		baseTask = createStandardTask(Duration.ofHours(8));
		project.addTask(baseTask);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskNullTask() {
		project.addTask(null);
	}

	@Test
	public void testUpdate() {
		baseTask = createStandardTask(Duration.ofHours(8));

		project.handleTimeChange(time.plusHours(5));
		assertEquals(time.plusHours(5), project.getLastUpdateTime());
		assertEquals(time.plusHours(5), baseTask.getLastUpdateTime());
	}

	@Test
	public void testOverTime() {
		// Add a task that will take to long
		Task task = createStandardTask(Duration.ofHours(3 * 8));

		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());

		// Let the task finish to late
		controller.setExecuting(task, time);
		controller.setFinished(task, time.plusDays(10));

		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayOnTime() {
		baseTask = createStandardTask(Duration.ofHours(8));
		project.getCurrentDelay();
	}

	@Test
	public void testDelayToMuchWork() {
		baseTask = createStandardTask(Duration.ofHours(8));
		Task.builder("bla", Duration.ofHours(2 * 8), 0.5)
				.addDependencies(baseTask).build(project);
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(8), project.getCurrentDelay());
	}

	@Test
	public void testDelayBaseTaskDelayed() {
		baseTask = createStandardTask(Duration.ofHours(8));
		Task.builder("bla", Duration.ofHours(8), 0.5)
				.addDependencies(baseTask).build(project);
		assertEquals(ProjectFinishingStatus.ON_TIME, project.finishedOnTime());

		// To delay task finish time with 8 work hours add 3*24.
		// 8 work hours = 24 real hours
		// + 2 days of weekend
		controller.setExecuting(baseTask, time);
		controller.setFinished(baseTask, time.plusHours(8).plusDays(3));
		assertEquals(ProjectFinishingStatus.OVER_TIME, project.finishedOnTime());
		assertEquals(Duration.ofHours(7), project.getCurrentDelay());
	}

	@Test
	public void testGetCurrentDelayTwoTasks() {
		Task.builder("desc", Duration.ofHours(5 * 8), 0.5).build(project);
		Task.builder("desc", Duration.ofHours(4 * 8), 0.5).build(project);
		assertEquals(Duration.ofHours(3 * 8), project.getCurrentDelay());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentDelayNoTask() {
		project.getCurrentDelay();
	}
	
	@Test
	public void testProjectMemento() {
		baseTask = createStandardTask(Duration.ofHours(8));
		
		//sanity
		assertEquals(1, project.getAllTasks().size());
		
		//save memento
		controller.saveSystem();
		
		// finish Tasks
		Task baseTaskTwo = createStandardTask(Duration.ofHours(8));
		controller.setExecuting(baseTask, time);
		controller.setFinished(baseTask, time.plusHours(8));
		controller.setExecuting(baseTaskTwo, time);
		controller.setFinished(baseTaskTwo, time.plusHours(8));
		
		//sanity check
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());
		

		assertEquals(2, project.getAllTasks().size());

		
		//load memento
		controller.loadSystem();
		
		//statuses are different
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(1, project.getAllTasks().size());
	}

}
