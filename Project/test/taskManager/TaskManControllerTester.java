package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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
		ResourceType car = ResourceType.builder("car").build(tmc);
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
	public void getTaskTest() {
		Task task = createPlannedTask(project, Duration.ofHours(8));
		assertEquals(task, tmc.getTask(task.getPlanning()));
	}

	@Test
	public void collateralSetStatus() {
		ResourceType car = ResourceType.builder("car").build(tmc);
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
}
