package taskmanager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.TaskStatus;
import utility.TimeSpan;

public class TaskManControllerTester extends TaskManTester {

	private Project project;

	@Before
	public void setUp() {
		super.setUp();
		project = createStandardProject(time.plusDays(5));
		tmc.createDeveloper("dev");
	}

	@Test
	public void possibleStartTimesTest() {
		Task task = createTask(project, Duration.ofHours(8));
		assertTrue(tmc.getPossibleStartTimes(task).contains(time));
		assertTrue(tmc.getPossibleStartTimes(task).contains(time.plusHours(1)));
		assertTrue(tmc.getPossibleStartTimes(task).contains(time.plusHours(2)));
	}

	@Test
	public void SelectedRessourceTest() {
		// Add 2 cars to the system
		ResourceType car = ResourceType.builder("car").build(
				tmc.getActiveOffice());
		Resource redCar = car.createResource("red car");
		Resource greenCar = car.createResource("green car");

		// create a task requiring a car
		Task task = createRessourceTask(project, Duration.ofHours(8), car);

		// check the system proposes one of the 2 cars
		Set<Resource> selectedResource = tmc.selectResources(task,
				new TimeSpan(time, task.getDuration()));
		assertTrue(selectedResource.size() == 1);
		assertTrue(selectedResource.contains(redCar)
				|| selectedResource.contains(greenCar));
	}

	@Test
	public void noRessourcesToSelectTest() {
		Task task = createTask(project, Duration.ofHours(8));
		assertTrue(tmc.selectResources(task,
				new TimeSpan(time, task.getDuration())).isEmpty());
	}

	@Test
	public void collateralSetStatus() {
		ResourceType car = ResourceType.builder("car").build(
				tmc.getActiveOffice());
		Resource redCar = car.createResource("red car");
		Task task1 = createPlannedRessourceTask(project, Duration.ofHours(8),
				car, redCar, time.plusDays(5));
		Task task2 = createPlannedRessourceTask(project, Duration.ofHours(8),
				car, redCar, time.plusDays(10));
		Task task3 = createPlannedRessourceTask(project, Duration.ofHours(8),
				car, redCar, time.plusDays(15));
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
		tmc.setExecuting(task1, time);
		assertEquals(TaskStatus.EXECUTING, task1.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		tmc.advanceTime(time.plusHours(1));
		tmc.setFinished(task1, time.plusHours(1));
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
		tmc.setExecuting(task2, time.plusHours(1));
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.EXECUTING, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		tmc.advanceTime(time.plusHours(2));
		tmc.setFailed(task2, time.plusHours(2));
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.FAILED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
	}

	@Test
	public void getAllDelegatablePlannableTasksTest() {

		BranchOffice activeOffice = tmc.getActiveOffice();
		BranchOffice office2 = tmc.createBranchOffice("Wonderland");
		Project project2 = office2.getProjectExpert().createProject(
				"a project in wonderland", "capture the flag", time,
				time.plusDays(99));

		Task task1 = Task.builder("task1", Duration.ofHours(1), 1).build(
				project);
		Task task2 = Task.builder("task2", Duration.ofHours(1), 1).build(
				project);

		Task task3 = Task.builder("task3", Duration.ofHours(1), 1).build(
				project2);
		Task task4 = createPlannedTask(project, Duration.ofHours(8));

		activeOffice.getDelegatedTaskExpert().addDelegatedTask(task3, office2);
		assertTrue(activeOffice.getDelegatedTaskExpert().getAllDelegatedTasks()
				.contains(task3));
		office2.getDelegatedTaskExpert().addDelegatedTask(task2, activeOffice);
		assertTrue(office2.getDelegatedTaskExpert().getAllDelegatedTasks()
				.contains(task2));

		assertEquals(2, tmc.getAllDelegatablePlannableTasks().size());
		assertTrue(tmc.getAllDelegatablePlannableTasks().contains(task1));
		assertFalse(tmc.getAllDelegatablePlannableTasks().contains(task2));
		assertTrue(tmc.getAllDelegatablePlannableTasks().contains(task3));
		assertFalse(tmc.getAllDelegatablePlannableTasks().contains(task4));
	}

	@Test
	public void delegateTaskTest() {
		// delegate a simple task from activeOffice to office 2
		BranchOffice activeOffice = tmc.getActiveOffice();
		BranchOffice office2 = tmc.createBranchOffice("Wonderland");
		BranchOffice office3 = tmc.createBranchOffice("Not Wonderland");
		Task taskToDelegate = Task.builder("delegate", Duration.ofHours(1), 1)
				.build(project);
		tmc.delegate(taskToDelegate, office2);

		assertTrue(office2.getDelegatedTaskExpert().getAllDelegatedTasks()
				.contains(taskToDelegate));
		assertEquals(1, office2.getDelegatedTaskExpert().getAllDelegatedTasks()
				.size());

		// delegate a task that has been delegated to active office to office 2
		Task taskToDelegate2 = Task
				.builder("delegate2", Duration.ofHours(1), 1).build(project);
		;
		activeOffice.getDelegatedTaskExpert().addDelegatedTask(taskToDelegate2,
				office3);
		assertEquals(1, activeOffice.getDelegatedTaskExpert()
				.getAllDelegatedTasks().size());
		assertTrue(activeOffice.getDelegatedTaskExpert().getAllDelegatedTasks()
				.contains(taskToDelegate2));
		tmc.delegate(taskToDelegate2, office2);

		assertFalse(activeOffice.getDelegatedTaskExpert()
				.getAllDelegatedTasks().contains(taskToDelegate2));
		assertTrue(office2.getDelegatedTaskExpert().getAllDelegatedTasks()
				.contains(taskToDelegate2));
		assertEquals(office3, office2.getDelegatedTaskExpert()
				.officeForDelegatedTask(taskToDelegate2));
		assertEquals(2, office2.getDelegatedTaskExpert().getAllDelegatedTasks()
				.size());
		assertEquals(0, activeOffice.getDelegatedTaskExpert()
				.getAllDelegatedTasks().size());
	}

	@Test
	public void logoutTest() {
		assertNotNull(tmc.getActiveDeveloper());
		assertNotNull(tmc.getActiveOffice());
		tmc.logOut();
		assertNull(tmc.getActiveDeveloper());
		assertNull(tmc.getActiveOffice());
	}

	@Test
	public void ActiveOfficeTests() {
		Task task = createPlannedTask(project, Duration.ofHours(2));
		Task taskExecuting = createPlannedTask(project, Duration.ofHours(2));
		tmc.setExecuting(taskExecuting, time);
		tmc.logOut();
		try {
			tmc.setExecuting(task, time);
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.setFinished(taskExecuting, time);
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.setFailed(taskExecuting, time);
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.getAllDelegatablePlannableTasks();
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.delegate(task, here);
			;
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.getPossibleStartTimes(task);
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.selectResources(task, new TimeSpan(time, time.plusHours(1)));
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
		try {
			tmc.getAllTasks();
			fail("Expected not logged in exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void unmodifialbleSetTest() {
		try {
			tmc.getAllDelegatablePlannableTasks().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllDelegatedTasks().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllDelegatedTasksTo(here).clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllDevelopers().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllOffices().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllProjectsActiveOffice().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllProjectsAllOffices().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllResourceTypes().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		try {
			tmc.getAllTasks().clear();
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testGetAllProjects() {
		assertEquals(1, tmc.getAllProjectsActiveOffice().size());
		assertEquals(1, tmc.getAllProjectsAllOffices().size());
		tmc.logIn(tmc.createBranchOffice("newLocation"));
		tmc.logIn(tmc.createDeveloper("newName"));
		tmc.createProject("newproj", "ugh", time.plusHours(32));
		assertEquals(1, tmc.getAllProjectsActiveOffice().size());
		assertEquals(2, tmc.getAllProjectsAllOffices().size());
	}

	@Test
	public void testGetAllTasks() {
		assertEquals(0, tmc.getAllTasks().size());
		createStandardProject(time.plusHours(24));
		Task task1 = createPlannedTask(project, Duration.ofHours(5), dev);
		Task task2 = createTask(project, Duration.ofHours(8));
		Developer dev2 = tmc.createDeveloper("other guy");
		Task task3 = createPlannedTask(project, Duration.ofHours(5), dev2);
		assertEquals(1, tmc.getAllTasks().size());
		assertTrue(tmc.getAllTasks().contains(task1));
		assertFalse(tmc.getAllTasks().contains(task2));
		assertFalse(tmc.getAllTasks().contains(task3));
	}

	@Test
	public void getResponsibleBranch() {
		assertEquals(this.here, tmc.getResponsibleBranch(project));
	}

	@Test
	public void getResponsibleBranchForTask() {
		Task task = this.createTask(project, Duration.ofHours(5));
		BranchOffice there = tmc.createBranchOffice("Celestijnenlaan 200a");
		assertEquals(this.here, tmc.getResponsibleBranch(task));
		tmc.delegate(task, there);
		assertEquals(there, tmc.getResponsibleBranch(task));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getResponsibleBranchForProjectNotInSystem() {
		tmc.saveSystem();
		Project project = createStandardProject(time.plusHours(1));
		tmc.loadSystem();
		tmc.getResponsibleBranch(project);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getResponsibleBranchForTaskNotInSystem() {
		tmc.saveSystem();
		Project project = createStandardProject(time.plusHours(1));
		Task task = createTask(project, Duration.ofHours(8));
		tmc.loadSystem();
		tmc.getResponsibleBranch(task);
	}
}
