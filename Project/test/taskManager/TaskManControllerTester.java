package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import utility.TimeSpan;

public class TaskManControllerTester extends TaskManTester {

	private Project project;
	private Developer dev;

	@Before
	public void setUp() {
		super.setUp();
		project = createStandardProject(time.plusDays(5));
		dev = tmc.getDeveloperExpert().createDeveloper("dev");
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
		ResourceType car = ResourceType.builder("car").build(
				tmc.getResourceExpert());
		Resource redCar = car.createResource("red car");
		Resource greenCar = car.createResource("green car");
		Task task = createRessourceTask(project, Duration.ofHours(8), car);
		Set<Resource> selectedResource = tmc.selectResources(task,
				new TimeSpan(time, task.getDuration()));
		assertTrue(selectedResource.size() == 1);
		assertTrue(selectedResource.contains(redCar)
				|| selectedResource.contains(greenCar));
	}

}
