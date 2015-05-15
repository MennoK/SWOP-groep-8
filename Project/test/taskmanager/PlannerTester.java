package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Planner;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.Planning.PlanningBuilder;
import taskmanager.exception.ConlictingPlanningException;
import utility.TimeInterval;
import utility.TimeSpan;
import utility.WorkTime;

public class PlannerTester extends TaskManTester {

	public Planner planner;
	public LocalDateTime time1;
	public LocalDateTime time2;
	public Project project;
	public Task task1;
	public Task task2;
	private ResourceType resourceType;
	private Resource resource1;
	private Resource resource2;
	private Developer developer1;
	private Developer developer2;

	@Before
	public void setUp() {
		super.setUp();
		// 2 default times
		time1 = time.plusHours(3);
		time2 = time1.plusHours(3);
		// create planning expert
		planner = tmc.getPlanner();
		// create some resources
		resourceType = ResourceType.builder("type")
				.build(tmc.getActiveOffice());
		resource1 = resourceType.createResource("resource");
		resource2 = resourceType.createResource("resource2");

		// create a project with a task
		project = tmc.createProject("name", "des", time2.plusDays(13));
		task1 = Task.builder("task 1", Duration.ofHours(1), 1).build(project);
		task2 = Task.builder("task 2", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);

		// create some developers
		developer1 = tmc.getActiveDeveloper();
		developer2 = tmc.createDeveloper("person2");
	}

	@Test
	public void testGetUnplannedTasks() {

		// create planning for task1
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		// check if the planning has been created
		assertEquals(1, planner.getAllPlannings().size());

		// check if the method getUnplannedTasks returns task2
		assertEquals(
				1,
				planner.getUnplannedTasks(
						new HashSet<Task>(project.getAllTasks())).size());
		assertTrue(planner.getUnplannedTasks(
				new HashSet<Task>(project.getAllTasks())).contains(task2));

		Task task3 = Task.builder("task3", Duration.ofHours(3), 2).build(
				project);

		assertTrue(planner.getUnplannedTasks(
				new HashSet<Task>(project.getAllTasks())).contains(task2));
		assertTrue(planner.getUnplannedTasks(
				new HashSet<Task>(project.getAllTasks())).contains(task3));
		assertEquals(
				2,
				planner.getUnplannedTasks(
						new HashSet<Task>(project.getAllTasks())).size());

	}

	@Test
	public void testGetPossibleStartTimes() {
		// CASE1: everything is available
		assertTrue(planner.getPossibleStartTimes(task1, time1,
				tmc.getAllDevelopers()).contains(time1));

		assertTrue(planner.getPossibleStartTimes(task1, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(1)));

		assertTrue(planner.getPossibleStartTimes(task1, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(3)));
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).addResources(resource1).build();

		// CASE2: task1 + allDevs are planned for time1 until time1+1

		assertTrue(planner.getPossibleStartTimes(task2, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(1)));
		assertTrue(planner.getPossibleStartTimes(task2, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(3)));
		assertTrue(planner.getPossibleStartTimes(task2, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(4)));

		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();

		// CASE3: task1 + allDevs are planned for time1 until time1+1 AND task2
		// + resource + all devs are planned for time1+3
		// subcase: 1 timeslot is available between planning of task 1 and task
		// 2
		Task task3 = Task.builder("task3 ", Duration.ofHours(2), 2).build(
				project);
		assertTrue(planner.getPossibleStartTimes(task3, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(5)));

		assertTrue(planner.getPossibleStartTimes(task3, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(6)));
		assertTrue(planner.getPossibleStartTimes(task3, time1,
				tmc.getAllDevelopers()).contains(
				(WorkTime.getFinishTime(time1, Duration.ofHours(6)))));

		// CASE4: some resources planned, some available ->
		Task task5 = Task.builder("task5 ", Duration.ofHours(1), 2)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task task6 = Task.builder("task6 ", Duration.ofHours(1), 2)
				.addRequiredResourceType(resourceType, 1).build(project);
		Planning.builder(time1.plusHours(2), task5, developer1, planner)
				.addDeveloper(developer2).addResources(resource1).build();

		assertTrue(planner.getPossibleStartTimes(task6, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(5)));

		assertTrue(planner.getPossibleStartTimes(task6, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(6)));
		assertTrue(planner.getPossibleStartTimes(task6, time1,
				tmc.getAllDevelopers()).contains(
				(WorkTime.getFinishTime(time1, Duration.ofHours(6)))));

		// CASE5: task with more then 1 developer required
		Task task7 = Task.builder("task6 ", Duration.ofHours(1), 2)
				.amountOfRequiredDevelopers(2).build(project);

		assertTrue(planner.getPossibleStartTimes(task7, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(5)));

		assertTrue(planner.getPossibleStartTimes(task7, time1,
				tmc.getAllDevelopers()).contains(time1.plusHours(6)));
		assertTrue(planner.getPossibleStartTimes(task7, time1,
				tmc.getAllDevelopers()).contains(
				(WorkTime.getFinishTime(time1, Duration.ofHours(6)))));

	}

	@Test
	public void testHasConflictWithPlannedTask() {
		// create planning for task 1 so that it can conflict with task 2 at
		// certain times
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		assertTrue(planner.hasConflictWithAPlannedTask(task2, time1));

		assertTrue(planner.hasConflictWithAPlannedTask(task2,
				time1.minusHours(1)));

		assertFalse(planner.hasConflictWithAPlannedTask(task2,
				time1.minusHours(2)));

		assertFalse(planner.hasConflictWithAPlannedTask(task2,
				time1.plusHours(1)));

		assertFalse(planner.hasConflictWithAPlannedTask(task2, time2));

		// task 2 is planned at time1 + 3
		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();

		Task task3 = Task.builder("task3 ", Duration.ofHours(2), 2).build(
				project);
		assertTrue(planner.hasConflictWithAPlannedTask(task3, time1));

		assertTrue(planner.hasConflictWithAPlannedTask(task3,
				time1.plusHours(1)));

		assertTrue(planner.hasConflictWithAPlannedTask(task3,
				time1.plusHours(2)));
	}

	@Test
	public void testGetConflictingTasks() {

		// create a planning for task 1 and see if it will be returned when it
		// would conflict with task 2
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		Set<Task> allTasks = new LinkedHashSet<>(project.getAllTasks());
		assertTrue(planner.getConflictingTasks(task2, time1.minusHours(1),
				allTasks).contains(task1));
		assertEquals(
				1,
				planner.getConflictingTasks(task2, time1.minusHours(1),
						allTasks).size());

		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();

		Task task3 = Task.builder("task3 ", Duration.ofHours(2), 2).build(
				project);
		assertTrue(planner.getConflictingTasks(task3, time1.minusHours(1),
				allTasks).contains(task1));

		Task task4 = Task.builder("task4", Duration.ofHours(4), 2).build(
				project);
		assertTrue(planner.getConflictingTasks(task4, time1.minusHours(1),
				allTasks).contains(task1));
		assertTrue(planner.getConflictingTasks(task4, time1.minusHours(1),
				allTasks).contains(task2));

	}

	@Test
	public void testDeveloperAvailableForSimple() {
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(
				2,
				planner.developersAvailableFor(tmc.getAllDevelopers(), task1,
						timeSpan).size());
	}

	@Test
	public void testDeveloperAvailableForPlannedTask() {
		Planning.builder(time1.plusHours(1), task1, developer1, planner)
				.build();
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(
				2,
				planner.developersAvailableFor(tmc.getAllDevelopers(), task1,
						timeSpan).size());
	}

	@Test
	public void testDeveloperUnavailable() {
		Planning.builder(time1, task2, developer1, planner).build();
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(
				1,
				planner.developersAvailableFor(tmc.getAllDevelopers(), task1,
						timeSpan).size());
	}

	@Test
	public void testDeveloperBothUnavailable() {
		Planning.builder(time1, task2, developer1, planner)
				.addDeveloper(developer2).build();
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(
				0,
				planner.developersAvailableFor(tmc.getAllDevelopers(), task1,
						timeSpan).size());
	}

	@Test
	public void testDeveloperOneOutOfTwoUnavailable() {
		Planning.builder(time1.plusMinutes(30), task2, developer1, planner)
				.build();
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(
				1,
				planner.developersAvailableFor(tmc.getAllDevelopers(), task1,
						timeSpan).size());
	}

	@Test
	public void testDeveloperAvailableWithOtherPlanning() {
		Planning.builder(time2, task2, developer1, planner).build();
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(
				2,
				planner.developersAvailableFor(tmc.getAllDevelopers(), task1,
						timeSpan).size());
	}

	@Test
	public void testResourcesAvailableSimple() {
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(1, planner.resourcesAvailableFor(task2, timeSpan).size());
		Map.Entry<ResourceType, Set<Resource>> map = planner
				.resourcesAvailableFor(task2, timeSpan).entrySet().iterator()
				.next();
		assertEquals(2, map.getValue().size());
	}

	@Test
	public void testResourcesAvailableForPlannedTask() {
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(1, planner.resourcesAvailableFor(task2, timeSpan).size());
		Map.Entry<ResourceType, Set<Resource>> map = planner
				.resourcesAvailableFor(task2, timeSpan).entrySet().iterator()
				.next();
		assertEquals(2, map.getValue().size());
	}

	@Test
	public void testResourceUnavailable() {
		Task task3 = Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		Planning.builder(time1, task2, developer1, planner)
				.addResources(resource1).build();
		TimeSpan timeSpan = new TimeSpan(time1, task3.getDuration());
		Map.Entry<ResourceType, Set<Resource>> map = planner
				.resourcesAvailableFor(task3, timeSpan).entrySet().iterator()
				.next();
		assertEquals(1, map.getValue().size());
	}

	@Test
	public void testResourceBothUnavailable() {
		Task task3 = Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 2).build(project);
		Task task4 = Task.builder("task 4", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 2).build(project);
		Planning.builder(time1, task3, developer1, planner)
				.addResources(resource1).addResources(resource2).build();
		TimeSpan timeSpan = new TimeSpan(time1, task4.getDuration());
		Map.Entry<ResourceType, Set<Resource>> map = planner
				.resourcesAvailableFor(task4, timeSpan).entrySet().iterator()
				.next();
		assertEquals(0, map.getValue().size());
	}

	@Test
	public void testResourceAvailableWithOtherPlanning() {
		Task task3 = Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		Planning.builder(time1, task2, developer1, planner)
				.addResources(resource1).build();
		TimeSpan timeSpan = new TimeSpan(time1.plusHours(3),
				task3.getDuration());
		Map.Entry<ResourceType, Set<Resource>> map = planner
				.resourcesAvailableFor(task3, timeSpan).entrySet().iterator()
				.next();
		assertEquals(2, map.getValue().size());
	}

	@Test
	public void testGetConflictingPlanningsForBuilder() {

		Planning.builder(time1, task1, developer1, planner).build();
		Set<Planning> conflictingPlannings = new HashSet<>();
		Set<Task> conflictingPlanningsFromException = new HashSet<>();
		conflictingPlannings.addAll(planner.getAllPlannings());
		PlanningBuilder builder = null;
		try {
			builder = Planning.builder(time1, task2, developer1, planner)
					.addResources(resource1);
		} catch (ConlictingPlanningException e) {
			builder = e.getPlanningBuilder();
			conflictingPlanningsFromException = e.getConflictingTasks();
		}

		assertTrue(planner.getConflictingTasksForBuilder(builder).contains(
				task1));
		assertEquals(planner.getAllPlannings().size(), planner
				.getConflictingTasksForBuilder(builder).size());
		assertTrue(conflictingPlanningsFromException.contains(task1));
	}

	@Test
	public void testResourceDailyAvailableIsAvailable() {
		TimeInterval available13to17 = new TimeInterval(LocalTime.of(13, 0),
				LocalTime.of(17, 0));
		TimeInterval available8to12 = new TimeInterval(LocalTime.of(8, 0),
				LocalTime.of(12, 0));
		TimeInterval available9to12 = new TimeInterval(LocalTime.of(9, 0),
				LocalTime.of(12, 0));

		ResourceType type1 = ResourceType.builder("available13to17")
				.addDailyAvailability(available13to17)
				.build(tmc.getActiveOffice());
		ResourceType type2 = ResourceType.builder("available8to12")
				.addDailyAvailability(available8to12)
				.build(tmc.getActiveOffice());
		ResourceType type3 = ResourceType.builder("available9to12")
				.addDailyAvailability(available9to12)
				.build(tmc.getActiveOffice());
		type1.createResource("a resource");
		type2.createResource("a resource");
		type3.createResource("a resource");

		Task task2 = Task
				.builder("testTask for available13to17 ", Duration.ofHours(3),
						1).addRequiredResourceType(type1, 1).build(project);
		Task task3 = Task
				.builder("testTask for available8to12 ", Duration.ofHours(3), 1)
				.addRequiredResourceType(type2, 1).build(project);
		Task task4 = Task
				.builder("testTask for available9to12 ", Duration.ofHours(3), 1)
				.addRequiredResourceType(type3, 1).build(project);

		TimeSpan timeSpan = new TimeSpan(time1.minusHours(1),
				Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(task2, timeSpan));

		timeSpan = new TimeSpan(time1, Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(task2, timeSpan));

		timeSpan = new TimeSpan(time1.plusHours(2), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(task2, timeSpan));

		timeSpan = new TimeSpan(time1.plusHours(3), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(task2, timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(3), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(task3, timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(2), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(task3, timeSpan));

		timeSpan = new TimeSpan(time1.plusHours(1), Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(task3, timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(3), Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(task4, timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(1), Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(task4, timeSpan));
	}

	@Test
	public void testMementoRollbackRemovesPlannings() {

		planner.save();

		// create planning for task1
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		// check if the planning has been created
		assertEquals(1, planner.getAllPlannings().size());

		planner.load();

		assertEquals(0, planner.getAllPlannings().size());

	}

	@Test
	public void testMementoPlanningConflicts() {

		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		Set<Task> allTasks = new LinkedHashSet<>(project.getAllTasks());
		assertTrue(planner.getConflictingTasks(task2, time1.minusHours(1),
				allTasks).contains(task1));
		assertEquals(
				1,
				planner.getConflictingTasks(task2, time1.minusHours(1),
						allTasks).size());

		tmc.saveSystem();

		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();
		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task.builder("task4", Duration.ofHours(4), 2).build(project);

		tmc.loadSystem();

		assertTrue(planner.getConflictingTasks(task2, time1.minusHours(1),
				allTasks).contains(task1));
		assertEquals(
				1,
				planner.getConflictingTasks(task2, time1.minusHours(1),
						allTasks).size());

	}

	@Test
	public void testRemovePlanningWorks() {
		Planning newPlanning = Planning
				.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		assertEquals(1, this.planner.getAllPlannings().size());

		planner.removePlanning(newPlanning);

		assertEquals(0, this.planner.getAllPlannings().size());
	}

	@Test
	public void testTaskHasPlanning() {

		assertFalse(this.planner.taskHasPlanning(task1));

		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();

		assertTrue(this.planner.taskHasPlanning(task1));
	}

}
