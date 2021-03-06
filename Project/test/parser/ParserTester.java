package parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.TaskManController;

public class ParserTester {

	DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm");
	static TaskManController tmc;
	static List<ResourceType> resourceTypeList;
	static List<Planning> planningsList;

	private Planning getPlan(LocalDateTime time) {
		List<Planning> planInSet = planningsList.stream()
				.filter((p) -> (p.getTimeSpan().getBegin().equals(time)))
				.collect(Collectors.toList());
		assertEquals(1, planInSet.size());
		return planInSet.get(0);
	}

	// run setup only once
	@BeforeClass
	public static void setUp() {
		try {
			tmc = new Parser().parse("./InputParserTester.tman");
			resourceTypeList = new ArrayList<ResourceType>(
					tmc.getAllResourceTypes());
			planningsList = new ArrayList<Planning>(tmc.getPlanner()
					.getAllPlannings());
		} catch (FileNotFoundException | RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSystemTime() {
		assertEquals(
				LocalDateTime.parse("2014-04-01 09:00", dateTimeFormatter),
				tmc.getTime());
	}

	@Test
	public void testSixResourceTypesMade() {
		assertEquals(6, tmc.getAllResourceTypes().size());
	}

	@Test
	public void testResourceTypeCarMade() {
		assertEquals("car", resourceTypeList.get(0).getName());
		assertEquals(0, resourceTypeList.get(0).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(0).getRequiredResourceTypes()
				.size());
	}

	@Test
	public void testResourceTypeWhiteBoardMade() {
		assertEquals("white board", resourceTypeList.get(1).getName());
		assertEquals(0, resourceTypeList.get(1).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(1).getRequiredResourceTypes()
				.size());
	}

	@Test
	public void testResourceTypeDemoKitMade() {
		assertEquals("demo kit", resourceTypeList.get(2).getName());
		assertEquals(1, resourceTypeList.get(2).getConflictedResourceTypes()
				.size());
		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(2).getConflictedResourceTypes());
		assertEquals("white board", conflictedRTlist.get(0).getName());
		assertEquals(0, resourceTypeList.get(2).getRequiredResourceTypes()
				.size());
	}

	@Test
	public void testResourceTypeConferenceRoomMade() {
		assertEquals("conference room", resourceTypeList.get(3).getName());
		assertEquals(1, resourceTypeList.get(3).getConflictedResourceTypes()
				.size());
		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(3).getConflictedResourceTypes());
		assertEquals("car", conflictedRTlist.get(0).getName());
		assertEquals(1, resourceTypeList.get(3).getRequiredResourceTypes()
				.size());
		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(3).getRequiredResourceTypes());
		assertEquals("demo kit", requiredRTlist.get(0).getName());
	}

	@Test
	public void testResourceTypeDistributedTestingSetupMade() {
		assertEquals("distributed testing setup", resourceTypeList.get(4)
				.getName());
		assertEquals(0, resourceTypeList.get(4).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(4).getRequiredResourceTypes()
				.size());
	}

	@Test
	public void testResourceTypeDataCenterMade() {
		assertEquals("data center", resourceTypeList.get(5).getName());
		assertEquals(0, resourceTypeList.get(5).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(5).getRequiredResourceTypes()
				.size());
		assertEquals(LocalTime.of(12, 00), resourceTypeList.get(5)
				.getDailyAvailability().getBegin());
		assertEquals(LocalTime.of(17, 00), resourceTypeList.get(5)
				.getDailyAvailability().getEnd());
	}

	@Test
	public void testThreeResourcesOfResourceTypeCarMade() {
		assertEquals(3, resourceTypeList.get(0).getAllResources().size());
	}

	@Test
	public void testResourceCar1Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(0).getAllResources());
		assertEquals("Car 1", resourceList.get(0).getName());
	}

	@Test
	public void testResourceCar2Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(0).getAllResources());
		assertEquals("Car 2", resourceList.get(1).getName());
	}

	@Test
	public void testResourceCar3Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(0).getAllResources());
		assertEquals("Car 3", resourceList.get(2).getName());
	}

	@Test
	public void testTwoResourcesOfResourceTypeWhiteBoardMade() {
		assertEquals(2, resourceTypeList.get(1).getAllResources().size());
	}

	@Test
	public void testResourceWhiteBoard1Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(1).getAllResources());
		assertEquals("White Board 1", resourceList.get(0).getName());
	}

	@Test
	public void testResourceWhiteBoard2Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(1).getAllResources());
		assertEquals("White Board 2", resourceList.get(1).getName());
	}

	@Test
	public void testTwoResourcesOfResourceTypeDemokitMade() {
		assertEquals(2, resourceTypeList.get(2).getAllResources().size());
	}

	@Test
	public void testResourceDemoKit1Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(2).getAllResources());
		assertEquals("Demo Kit 1", resourceList.get(0).getName());
	}

	@Test
	public void testResourceDemoKit2Made() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(2).getAllResources());
		assertEquals("Demo Kit 2", resourceList.get(1).getName());
	}

	@Test
	public void testTwoResourcesOfResourceTypeConferenceRoomMade() {
		assertEquals(2, resourceTypeList.get(3).getAllResources().size());
	}

	@Test
	public void testResourceBigConferenceRoomMade() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(3).getAllResources());
		assertEquals("The Big Conference Room", resourceList.get(0).getName());
	}

	@Test
	public void testResourceSmallConferenceRoomMade() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(3).getAllResources());
		assertEquals("The Small Conference Room", resourceList.get(1).getName());
	}

	@Test
	public void testOneResourceOfResourceTypeDistributedTestingSetupMade() {
		assertEquals(1, resourceTypeList.get(4).getAllResources().size());
	}

	@Test
	public void testResourceTheDistributedTestFacilityMade() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(4).getAllResources());
		assertEquals("The Distributed Test Facility", resourceList.get(0)
				.getName());
	}

	@Test
	public void testTwoResourcesOfResourceTypeDataCenterMade() {
		assertEquals(2, resourceTypeList.get(5).getAllResources().size());
	}

	@Test
	public void testResourceDataCenterXMade() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(5).getAllResources());
		assertEquals("Data Center X", resourceList.get(0).getName());
	}

	@Test
	public void testResourceDataCenterYMade() {
		List<Resource> resourceList = new ArrayList<Resource>(resourceTypeList
				.get(5).getAllResources());
		assertEquals("Data Center Y", resourceList.get(1).getName());
	}

	@Test
	public void testThreeProjectsAreMade() {
		assertEquals(3, tmc.getAllProjectsActiveOffice().size());
	}

	@Test
	public void testDevelopersMade() {
		assertEquals(3, tmc.getAllDevelopers().size());
		List<Developer> developerList = new ArrayList<Developer>(
				tmc.getAllDevelopers());
		assertEquals("John Deere", developerList.get(0).getName());
		assertEquals("Tom Hawk", developerList.get(1).getName());
		assertEquals("Bob Grylls", developerList.get(2).getName());
	}

	@Test
	public void testProjectxIsMade() {
		Project projectx = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(0);
		assertEquals("project x", projectx.getName());
		assertEquals("a project description", projectx.getDescription());
		assertEquals(projectx.getCreationTime(),
				LocalDateTime.parse(("2014-01-01 09:00"), dateTimeFormatter));
		assertEquals(projectx.getDueTime(),
				LocalDateTime.parse(("2014-02-01 18:00"), dateTimeFormatter));
	}

	@Test
	public void testProjectyIsMade() {
		Project projecty = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1);
		assertEquals("project y", projecty.getName());
		assertEquals("another project description", projecty.getDescription());
		assertEquals(projecty.getCreationTime(),
				LocalDateTime.parse(("2014-01-01 09:00"), dateTimeFormatter));
		assertEquals(projecty.getDueTime(),
				LocalDateTime.parse(("2016-01-01 18:00"), dateTimeFormatter));
	}

	@Test
	public void testProjectzIsMade() {
		Project projectz = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(2);
		assertEquals("project z", projectz.getName());
		assertEquals(projectz.getDescription(),
				"yet another project description");
		assertEquals(projectz.getCreationTime(),
				LocalDateTime.parse(("2015-04-25 09:00"), dateTimeFormatter));
		assertEquals(projectz.getDueTime(),
				LocalDateTime.parse(("2015-04-30 18:00"), dateTimeFormatter));
	}

	@Test
	public void testOneTaskOfProjectxIsMade() {
		Project projectx = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(0);
		assertEquals(1, projectx.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectxIsMade() {
		Project projectx = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(0);
		Task task1 = new ArrayList<>(projectx.getAllTasks()).get(0);

		assertEquals("task description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());
		assertEquals(taskmanager.TaskStatus.FINISHED, task1.getStatus());
		assertEquals(task1.getStartTime(),
				LocalDateTime.parse(("2014-01-01 10:00"), dateTimeFormatter));
		assertEquals(task1.getEndTime(),
				LocalDateTime.parse(("2014-01-02 17:00"), dateTimeFormatter));
		assertNull(task1.getOriginal());

		assertEquals(2, task1.getRequiredResourceTypes().size());
		ResourceType type = (ResourceType) task1.getRequiredResourceTypes()
				.keySet().toArray()[0];
		assertEquals("car", type.getName());
		ResourceType type2 = (ResourceType) task1.getRequiredResourceTypes()
				.keySet().toArray()[1];
		assertEquals("demo kit", type2.getName());

	}

	@Test
	public void testFourTasksOfProjectyAreMade() {
		Project projecty = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1);
		assertEquals(4, projecty.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectyIsMade() {
		Project projecty = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1);
		Task task1 = new ArrayList<>(projecty.getAllTasks()).get(0);

		assertEquals("another task description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());

		assertNull(task1.getOriginal());
		assertEquals(
				LocalDateTime.parse("2014-01-02 17:10", dateTimeFormatter),
				task1.getStartTime());
		assertEquals(
				LocalDateTime.parse("2014-01-02 17:20", dateTimeFormatter),
				task1.getEndTime());
		assertEquals(taskmanager.TaskStatus.FINISHED, task1.getStatus());
	}

	@Test
	public void testTaskTwoOfProjectyIsMade() {
		Project projecty = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1);
		Task task2 = new ArrayList<>(projecty.getAllTasks()).get(1);

		assertEquals("yet another task description", task2.getDescription());
		assertEquals(Duration.ofHours(4), task2.getEstimatedDuration());
		assertEquals(task2.getAcceptableDeviation(), 0.10, 0.001);
		assertEquals(0, task2.getDependencies().size());

		assertNull(task2.getOriginal());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(taskmanager.TaskStatus.FAILED, task2.getStatus());
		assertNotEquals(taskmanager.TaskStatus.FINISHED, task2.getStatus());
	}

	@Test
	public void testTaskThreeOfProjectyIsMade() {
		Project projecty = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1);
		Task task3 = new ArrayList<>(projecty.getAllTasks()).get(2);

		assertEquals("description", task3.getDescription());
		assertEquals(Duration.ofHours(50), task3.getEstimatedDuration());
		assertEquals(0, task3.getAcceptableDeviation(), 0.001);
		assertNull(task3.getOriginal());

		assertEquals(0, task3.getDependencies().size());
		assertEquals(taskmanager.TaskStatus.FAILED, task3.getStatus());
		assertEquals(
				LocalDateTime.parse(("2014-01-02 18:00"), dateTimeFormatter),
				task3.getStartTime());
		assertEquals(
				LocalDateTime.parse(("2014-03-25 18:00"), dateTimeFormatter),
				task3.getEndTime());

	}

	@Test
	public void testTaskFourOfProjectyIsMade() {
		Project projecty = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1);
		Task task4 = new ArrayList<>(projecty.getAllTasks()).get(3);

		assertEquals("description", task4.getDescription());
		assertEquals(Duration.ofHours(4), task4.getEstimatedDuration());
		assertEquals(task4.getAcceptableDeviation(), 0, 0.001);
		assertEquals(0, task4.getDependencies().size());
		assertEquals(task4.getOriginal(), new ArrayList<>(
				new ArrayList<Project>(tmc.getAllProjectsActiveOffice()).get(1)
						.getAllTasks()).get(2));

		assertEquals(
				LocalDateTime.parse("2014-03-26 09:00", dateTimeFormatter),
				(task4.getStartTime()));
		assertNull(task4.getEndTime());
		assertNotEquals(taskmanager.TaskStatus.FAILED, task4.getStatus());
		assertNotEquals(taskmanager.TaskStatus.FINISHED, task4.getStatus());
		assertEquals(taskmanager.TaskStatus.EXECUTING, task4.getStatus());

		assertEquals(1, task4.getRequiredResourceTypes().size());
		ResourceType type = (ResourceType) task4.getRequiredResourceTypes()
				.keySet().toArray()[0];
		assertEquals("data center", type.getName());
	}

	@Test
	public void testTwoTasksOfProjectzAreMade() {
		Project projectz = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(2);
		assertEquals(2, projectz.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectzIsMade() {
		Project projectz = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(2);
		Task task1 = new ArrayList<>(projectz.getAllTasks()).get(0);

		assertEquals("description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());

		assertNull(task1.getOriginal());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(taskmanager.TaskStatus.FAILED, task1.getStatus());
		assertNotEquals(taskmanager.TaskStatus.FINISHED, task1.getStatus());
	}

	@Test
	public void testTaskTwoOfProjectzIsMade() {
		Project projectz = new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(2);
		Task task2 = new ArrayList<>(projectz.getAllTasks()).get(1);

		assertEquals("description", task2.getDescription());
		assertEquals(Duration.ofHours(500), task2.getEstimatedDuration());
		assertEquals(task2.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task2.getDependencies().size());
		assertNull(task2.getOriginal());

		assertNull(task2.getOriginal());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(taskmanager.TaskStatus.FAILED, task2.getStatus());
		assertNotEquals(taskmanager.TaskStatus.FINISHED, task2.getStatus());
	}

	@Test
	public void testFourPlanningAreMade() {
		assertEquals(4, tmc.getPlanner().getAllPlannings().size());
	}

	@Test
	public void testPlanningOneisMade() {
		Planning plan = getPlan(LocalDateTime.of(2014, 01, 01, 10, 00));
		// endTime is finish time of task because task is finished

		List<Developer> developers = new ArrayList<Developer>(
				plan.getDevelopers());
		assertEquals("John Deere", developers.get(0).getName());
		assertEquals("Tom Hawk", developers.get(1).getName());

		List<Resource> resources = new ArrayList<Resource>(plan.getResources());
		assertEquals(3, resources.size());
		List<String> ressourceNames = new ArrayList<String>();
		ressourceNames.add("Demo Kit 2");
		ressourceNames.add("Car 1");
		ressourceNames.add("Car 2");
		assertTrue(ressourceNames.contains(resources.get(0).getName()));
		assertTrue(ressourceNames.contains(resources.get(1).getName()));
		assertTrue(ressourceNames.contains(resources.get(2).getName()));

		assertTrue(tmc.getPlanner().taskHasPlanning(
				new ArrayList<>(new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1)
						.getAllTasks()).get(2)));

	}

	@Test
	public void testPlanningTwoIsMade() {
		Planning plan = getPlan(LocalDateTime.of(2014, 01, 02, 17, 10));
		// endTime is finish time of task because task is finished

		List<Developer> developers = new ArrayList<Developer>(
				plan.getDevelopers());
		assertEquals("Tom Hawk", developers.get(0).getName());

		assertEquals(0, plan.getResources().size());

		assertTrue(tmc.getPlanner().taskHasPlanning(
				new ArrayList<>(new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1)
						.getAllTasks()).get(3)));

	}

	@Test
	public void testPlanningThreeisMade() {
		Planning plan = getPlan(LocalDateTime.of(2014, 01, 02, 18, 00));
		// endTime is finish time of task because task is failed

		List<Developer> developers = new ArrayList<Developer>(
				plan.getDevelopers());
		assertEquals("Tom Hawk", developers.get(0).getName());

		assertEquals(0, plan.getResources().size());

		assertTrue(tmc.getPlanner().taskHasPlanning(
				new ArrayList<>(new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(1)
						.getAllTasks()).get(3)));

	}

	@Test
	public void testPlanningFourisMade() {
		Planning plan = getPlan(LocalDateTime.of(2014, 03, 26, 9, 00));

		// end time depends on the worktime schedule

		List<Developer> developers = new ArrayList<Developer>(
				plan.getDevelopers());
		assertEquals("Bob Grylls", developers.get(0).getName());

		List<Resource> resources = new ArrayList<Resource>(plan.getResources());
		assertEquals(1, resources.size());
		assertEquals("Data Center Y", resources.get(0).getName());

		assertTrue(tmc.getPlanner().taskHasPlanning(
				new ArrayList<>(new ArrayList<>(tmc.getAllProjectsActiveOffice()).get(0)
						.getAllTasks()).get(0)));
	}
}
