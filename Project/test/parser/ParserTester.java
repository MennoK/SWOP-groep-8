package parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import taskManager.Developer;
import taskManager.DeveloperExpert;
import taskManager.Planning;
import taskManager.Project;
import taskManager.Resource;
import taskManager.ResourceType;
import taskManager.Task;
import taskManager.TaskManController;
import taskManager.Planning.PlanningBuilder;

public class ParserTester {

	DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm");
	static TaskManController taskManController;
	static List<ResourceType> resourceTypeList;
	static List<Planning> planningsList;

	// run setup only once
	@BeforeClass
	public static void setUp() {
		taskManController = new TaskManController(LocalDateTime.of(2010, 03,
				05, 00, 00));

		try {
			new Parser().parse("./input2.tman", taskManController);
			resourceTypeList = new ArrayList<ResourceType>(taskManController
					.getResourceExpert().getAllResourceTypes());
			planningsList = new ArrayList<Planning>(taskManController.getPlanner().getAllPlannings());
		} catch (FileNotFoundException | RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSystemTime() {
		assertEquals(LocalDateTime.parse("2014-04-01 09:00", dateTimeFormatter), taskManController.getTime());
	}

	@Test
	public void testSixResourceTypesMade() {
		assertEquals(6, taskManController.getResourceExpert()
				.getAllResourceTypes().size());
	}

	@Test
	public void testResourceTypeCarMade() {
		assertEquals("car", resourceTypeList.get(0).getName());
		assertEquals(0, resourceTypeList.get(0).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(0).getRequiredResourceTypes()
				.size());
		assertEquals(null, resourceTypeList.get(0).getDailyAvailability());
	}

	@Test
	public void testResourceTypeWhiteBoardMade() {
		assertEquals("white board", resourceTypeList.get(1).getName());
		assertEquals(0, resourceTypeList.get(1).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(1).getRequiredResourceTypes()
				.size());
		assertEquals(null, resourceTypeList.get(1).getDailyAvailability());
	}

	@Test
	public void testResourceTypeDemoKitMade() {
		assertEquals("demo kit", resourceTypeList.get(2).getName());
		assertEquals(1, resourceTypeList.get(2).getConflictedResourceTypes()
				.size());
		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(2).getConflictedResourceTypes());
		assertEquals("car", conflictedRTlist.get(0).getName());
		assertEquals(0, resourceTypeList.get(2).getRequiredResourceTypes()
				.size());
		assertEquals(null, resourceTypeList.get(2).getDailyAvailability());
	}

	@Test
	public void testResourceTypeConferenceRoomMade() {
		assertEquals("conference room", resourceTypeList.get(3).getName());
		assertEquals(1, resourceTypeList.get(3).getConflictedResourceTypes()
				.size());
		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(3).getConflictedResourceTypes());
		assertEquals("demo kit", conflictedRTlist.get(0).getName());
		assertEquals(1, resourceTypeList.get(3).getRequiredResourceTypes()
				.size());
		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(3).getRequiredResourceTypes());
		assertEquals("white board", requiredRTlist.get(0).getName());
		assertEquals(null, resourceTypeList.get(3).getDailyAvailability());
	}

	@Test
	public void testResourceTypeDistributedTestingSetupMade() {
		assertEquals("distributed testing setup", resourceTypeList.get(4)
				.getName());
		assertEquals(0, resourceTypeList.get(4).getConflictedResourceTypes()
				.size());
		assertEquals(0, resourceTypeList.get(4).getRequiredResourceTypes()
				.size());
		assertEquals(null, resourceTypeList.get(4).getDailyAvailability());
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
		assertEquals(3, taskManController.getProjectExpert().getAllProjects()
				.size());
	}

	@Test
	public void testDevelopersMade() {
		DeveloperExpert developerExpert = taskManController
				.getDeveloperExpert();
		assertEquals(3, developerExpert.getAllDevelopers().size());
		List<Developer> developerList = new ArrayList<Developer>(
				developerExpert.getAllDevelopers());
		assertEquals("John Deere", developerList.get(0).getName());
		assertEquals("Tom Hawk", developerList.get(1).getName());
		assertEquals("Bob Grylls", developerList.get(2).getName());
	}

	@Test
	public void testProjectxIsMade() {
		Project projectx = taskManController.getProjectExpert()
				.getAllProjects().get(0);
		assertEquals("project x", projectx.getName());
		assertEquals("a project description", projectx.getDescription());
		assertEquals(projectx.getCreationTime(),
				LocalDateTime.parse(("2014-01-01 09:00"), dateTimeFormatter));
		assertEquals(projectx.getDueTime(),
				LocalDateTime.parse(("2014-02-01 18:00"), dateTimeFormatter));
	}

	@Test
	public void testProjectyIsMade() {
		Project projecty = taskManController.getProjectExpert()
				.getAllProjects().get(1);
		assertEquals("project y", projecty.getName());
		assertEquals("another project description", projecty.getDescription());
		assertEquals(projecty.getCreationTime(),
				LocalDateTime.parse(("2014-01-01 09:00"), dateTimeFormatter));
		assertEquals(projecty.getDueTime(),
				LocalDateTime.parse(("2016-01-01 18:00"), dateTimeFormatter));
	}

	@Test
	public void testProjectzIsMade() {
		Project projectz = taskManController.getProjectExpert()
				.getAllProjects().get(2);
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
		Project projectx = taskManController.getProjectExpert()
				.getAllProjects().get(0);
		assertEquals(1, projectx.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectxIsMade() {
		Project projectx = taskManController.getProjectExpert()
				.getAllProjects().get(0);
		Task task1 = projectx.getAllTasks().get(0);

		assertEquals("task description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());
		assertEquals(taskManager.TaskStatus.FINISHED, task1.getCalculatedStatus());
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
		Project projecty = taskManController.getProjectExpert()
				.getAllProjects().get(1);
		assertEquals(4, projecty.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectyIsMade() {
		Project projecty = taskManController.getProjectExpert()
				.getAllProjects().get(1);
		Task task1 = projecty.getAllTasks().get(0);

		assertEquals("another task description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());

		assertNull(task1.getOriginal());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task1.getCalculatedStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task1.getCalculatedStatus());
	}

	@Test
	public void testTaskTwoOfProjectyIsMade() {
		Project projecty = taskManController.getProjectExpert()
				.getAllProjects().get(1);
		Task task2 = projecty.getAllTasks().get(1);

		assertEquals("yet another task description", task2.getDescription());
		assertEquals(Duration.ofHours(100), task2.getEstimatedDuration());
		assertEquals(task2.getAcceptableDeviation(), 0.10, 0.001);
		assertEquals(0, task2.getDependencies().size());

		assertNull(task2.getOriginal());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task2.getCalculatedStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task2.getCalculatedStatus());
	}

	@Test
	public void testTaskThreeOfProjectyIsMade() {
		Project projecty = taskManController.getProjectExpert()
				.getAllProjects().get(1);
		Task task3 = projecty.getAllTasks().get(2);

		assertEquals("description", task3.getDescription());
		assertEquals(Duration.ofHours(50), task3.getEstimatedDuration());
		assertEquals(0, task3.getAcceptableDeviation(), 0.001);
		assertNull(task3.getOriginal());

		assertEquals(2, task3.getDependencies().size());
		assertEquals(taskManager.TaskStatus.FAILED, task3.getCalculatedStatus());
		assertEquals(
				LocalDateTime.parse(("2014-01-02 09:00"), dateTimeFormatter),
				task3.getStartTime());
		assertEquals(
				LocalDateTime.parse(("2014-03-25 18:00"), dateTimeFormatter),
				task3.getEndTime());
		
	}

	@Test
	public void testTaskFourOfProjectyIsMade() {
		Project projecty = taskManController.getProjectExpert()
				.getAllProjects().get(1);
		Task task4 = projecty.getAllTasks().get(3);

		assertEquals("description", task4.getDescription());
		assertEquals(Duration.ofHours(50), task4.getEstimatedDuration());
		assertEquals(task4.getAcceptableDeviation(), 0, 0.001);
		assertEquals(2, task4.getDependencies().size());
		assertEquals(task4.getOriginal(), taskManController.getProjectExpert()
				.getAllProjects().get(1).getAllTasks().get(2));

		assertNull(task4.getStartTime());
		assertNull(task4.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task4.getCalculatedStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task4.getCalculatedStatus());

		assertEquals(1, task4.getRequiredResourceTypes().size());
		ResourceType type = (ResourceType) task4.getRequiredResourceTypes()
				.keySet().toArray()[0];
		assertEquals("data center", type.getName());
	}

	@Test
	public void testTwoTasksOfProjectzAreMade() {
		Project projectz = taskManController.getProjectExpert()
				.getAllProjects().get(2);
		assertEquals(2, projectz.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectzIsMade() {
		Project projectz = taskManController.getProjectExpert()
				.getAllProjects().get(2);
		Task task1 = projectz.getAllTasks().get(0);

		assertEquals("description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());

		assertNull(task1.getOriginal());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task1.getCalculatedStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task1.getCalculatedStatus());
	}

	@Test
	public void testTaskTwoOfProjectzIsMade() {
		Project projectz = taskManController.getProjectExpert()
				.getAllProjects().get(2);
		Task task2 = projectz.getAllTasks().get(1);

		assertEquals("description", task2.getDescription());
		assertEquals(Duration.ofHours(500), task2.getEstimatedDuration());
		assertEquals(task2.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task2.getDependencies().size());
		assertNull(task2.getOriginal());

		assertNull(task2.getOriginal());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task2.getCalculatedStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task2.getCalculatedStatus());
	}

	@Test
	public void testThreePlanningisMade() {
		assertEquals(3, taskManController.getPlanner().getAllPlannings().size());
	}

	@Test
	public void testPlanningOneisMade() {
		LocalDateTime time = LocalDateTime.parse(("2014-01-01 09:00"), dateTimeFormatter);
		assertEquals(time, planningsList.get(0).getStartTime());
		assertEquals(time.plusHours(500), planningsList.get(0).getEndTime());
		
		List<Developer> developers = new ArrayList<Developer>(planningsList.get(0).getDevelopers());
		assertEquals("John Deere", developers.get(0).getName());
		assertEquals("Tom Hawk", developers.get(1).getName());
		//TODO add other 
	}

	@Test
	public void testPlanningTwoisMade() {
		LocalDateTime time = LocalDateTime.parse(("2014-01-02 17:00"), dateTimeFormatter);
		assertEquals(time, planningsList.get(1).getStartTime());
		assertEquals(time.plusHours(50), planningsList.get(1).getEndTime());
		
		List<Developer> developers = new ArrayList<Developer>(planningsList.get(1).getDevelopers());
		assertEquals("Tom Hawk", developers.get(0).getName());
		//TODO add other 

	}

	@Test
	public void testPlanningThreeisMade() {
		LocalDateTime time = LocalDateTime.parse(("2014-03-01 09:00"), dateTimeFormatter);
		assertEquals(time, planningsList.get(2).getStartTime());
		assertEquals(time.plusHours(50), planningsList.get(2).getEndTime());
		
		List<Developer> developers = new ArrayList<Developer>(planningsList.get(2).getDevelopers());
		assertEquals("Bob Grylls", developers.get(0).getName());
		//TODO add other 

	}
}
