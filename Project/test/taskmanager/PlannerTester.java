package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
		Set<Task> unplannedTasks = new HashSet<>();
		unplannedTasks.add(task2);
		assertEquals(unplannedTasks,
				planner.getUnplannedTasks(new HashSet<Task>(project
						.getAllTasks())));
		assertEquals(
				1,
				planner.getUnplannedTasks(
						new HashSet<Task>(project.getAllTasks())).size());

		Task.builder("task3", Duration.ofHours(3), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		unplannedTasks.add(task3);

		assertEquals(unplannedTasks,
				planner.getUnplannedTasks(new HashSet<Task>(project
						.getAllTasks())));
		assertEquals(
				2,
				planner.getUnplannedTasks(
						new HashSet<Task>(project.getAllTasks())).size());

	}

	@Test
	public void testGetPossibleStartTimes() {
		// CASE1: everything is available
		Set<LocalDateTime> possibleStartTimes111214 = new LinkedHashSet<>();
		possibleStartTimes111214.add(time1);
		possibleStartTimes111214.add(time1.plusHours(1));
		possibleStartTimes111214.add(time1.plusHours(3));
		assertEquals(
				possibleStartTimes111214,
				planner.getPossibleStartTimes(task1, time1,
						tmc.getAllDevelopers()));
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).addResources(resource1).build();

		// CASE2: task1 + allDevs are planned for time1 until time1+1
		Set<LocalDateTime> possibleStartTimes121415 = new LinkedHashSet<>();
		possibleStartTimes121415.add(time1.plusHours(1));
		possibleStartTimes121415.add(time1.plusHours(3));
		possibleStartTimes121415.add(time1.plusHours(4));
		assertEquals(
				possibleStartTimes121415,
				planner.getPossibleStartTimes(task2, time1,
						tmc.getAllDevelopers()));
		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();

		// CASE3: task1 + allDevs are planned for time1 until time1+1 AND task2
		// + resource + all devs are planned for time1+3
		// subcase: 1 timeslot is available between planning of task 1 and task
		// 2
		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		Set<LocalDateTime> possibleStartTimes121617 = new LinkedHashSet<>();
		possibleStartTimes121617.add(time1.plusHours(5));
		possibleStartTimes121617.add(time1.plusHours(6));
		possibleStartTimes121617.add(WorkTime.getFinishTime(time1,
				Duration.ofHours(6)));

		assertEquals(
				possibleStartTimes121617,
				planner.getPossibleStartTimes(task3, time1,
						tmc.getAllDevelopers()));

		// CASE4: some developpers planned, some available -> same test as
		// before for task3, task4 is planned on time1+1 and has 1 developer
		// planned, 1 still available so result should be the same
		assertEquals(
				possibleStartTimes121617,
				planner.getPossibleStartTimes(task3, time1,
						tmc.getAllDevelopers()));

		// CASE5: some resources planned, some available ->
		Task.builder("task5 ", Duration.ofHours(1), 2)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task.builder("task6 ", Duration.ofHours(1), 2)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task task5 = project.getAllTasks().get(3);
		Task task6 = project.getAllTasks().get(4);

		Planning.builder(time1.plusHours(2), task5, developer1, planner)
				.addDeveloper(developer2).addResources(resource1).build();

		assertEquals(
				possibleStartTimes121617,
				planner.getPossibleStartTimes(task6, time1,
						tmc.getAllDevelopers()));

		// CASE6: task with more then 1 developer required
		Task.builder("task6 ", Duration.ofHours(1), 2)
				.amountOfRequiredDevelopers(2).build(project);
		Task task7 = project.getAllTasks().get(5);
		Set<LocalDateTime> possibleStartTimes161709 = new LinkedHashSet<>();
		possibleStartTimes161709.add(time1.plusHours(5));
		possibleStartTimes161709.add(time1.plusHours(6));
		possibleStartTimes161709.add(WorkTime.getFinishTime(time1,
				Duration.ofHours(6)));
		assertEquals(
				possibleStartTimes161709,
				planner.getPossibleStartTimes(task7, time1,
						tmc.getAllDevelopers()));
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

		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
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
		Set<Task> conflictSet = new LinkedHashSet<>();
		conflictSet.add(task1);
		assertEquals(conflictSet, planner.getConflictingTasks(task2,
				time1.minusHours(1), allTasks));

		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();

		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		assertNotEquals(conflictSet, planner.getConflictingTasks(task3,
				time1.plusHours(1), allTasks));

		Task.builder("task4", Duration.ofHours(4), 2).build(project);
		Task task4 = project.getAllTasks().get(3);
		conflictSet.add(task2);
		assertEquals(conflictSet,
				planner.getConflictingTasks(task4, time1, allTasks));

	}

	public Set<Resource> getAvailableResourcesOfType(ResourceType resourceType) {
		Set<Resource> resourceList = new LinkedHashSet<>();
		return resourceList;
	}

	@Test
	public void testResolveConflictingTasks() {

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
		Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task task3 = project.getAllTasks().get(2);
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
		Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		Task.builder("task 4", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 2).build(project);
		Task task4 = project.getAllTasks().get(3);
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
		Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task task3 = project.getAllTasks().get(2);
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
		Set<Planning> conflictingPlanningsFromException = new HashSet<>();
		conflictingPlannings.addAll(planner.getAllPlannings());
		PlanningBuilder builder = null;
		try {
			builder = Planning.builder(time1, task2, developer1, planner)
					.addResources(resource1);
		} catch (ConlictingPlanningException e) {
			builder = e.getPlanningBuilder();
			conflictingPlanningsFromException = e.getConflictingPlannings();
		}

		assertEquals(conflictingPlannings,
				planner.getConflictingPlanningsForBuilder(builder));
		assertEquals(conflictingPlannings, conflictingPlanningsFromException);
	}

	@Test
	public void testResourceDailyAvailableIsAvailable() {
		TimeInterval available13to17 = new TimeInterval(LocalTime.of(13, 0),
				LocalTime.of(17, 0));
		TimeInterval available8to12 = new TimeInterval(LocalTime.of(8, 0),
				LocalTime.of(12, 0));
		TimeInterval available9to12 = new TimeInterval(LocalTime.of(9, 0),
				LocalTime.of(12, 0));

		ResourceType.builder("available13to17")
				.addDailyAvailability(available13to17)
				.build(tmc.getActiveOffice());
		ResourceType.builder("available8to12")
				.addDailyAvailability(available8to12)
				.build(tmc.getActiveOffice());
		ResourceType.builder("available9to12")
				.addDailyAvailability(available9to12)
				.build(tmc.getActiveOffice());
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		resourceTypeList.get(1).createResource("a resource");
		resourceTypeList.get(2).createResource("a resource");
		resourceTypeList.get(3).createResource("a resource");

		Task.builder("testTask for available13to17 ", Duration.ofHours(3), 1)
				.addRequiredResourceType(resourceTypeList.get(1), 1)
				.build(project);
		Task.builder("testTask for available8to12 ", Duration.ofHours(3), 1)
				.addRequiredResourceType(resourceTypeList.get(2), 1)
				.build(project);
		Task.builder("testTask for available9to12 ", Duration.ofHours(3), 1)
				.addRequiredResourceType(resourceTypeList.get(3), 1)
				.build(project);

		TimeSpan timeSpan = new TimeSpan(time1.minusHours(1),
				Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(2), timeSpan));

		timeSpan = new TimeSpan(time1, Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(2), timeSpan));

		timeSpan = new TimeSpan(time1.plusHours(2), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(2), timeSpan));

		timeSpan = new TimeSpan(time1.plusHours(3), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(2), timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(3), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(3), timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(2), Duration.ofHours(3));
		assertTrue(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(3), timeSpan));

		timeSpan = new TimeSpan(time1.plusHours(1), Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(3), timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(3), Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(4), timeSpan));

		timeSpan = new TimeSpan(time1.minusHours(1), Duration.ofHours(3));
		assertFalse(planner.resourceDailyAvailableIsAvailable(project
				.getAllTasks().get(4), timeSpan));
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
		Set<Task> conflictSet = new LinkedHashSet<>();
		conflictSet.add(task1);
		assertEquals(conflictSet, planner.getConflictingTasks(task2,
				time1.minusHours(1), allTasks));

		tmc.getActiveOffice().saveSystem();
		Set<Task> conflictSetOriginal = new LinkedHashSet<>(conflictSet);

		Planning.builder(time1.plusHours(3), task2, developer1, planner)
				.addDeveloper(developer2).build();
		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task.builder("task4", Duration.ofHours(4), 2).build(project);
		conflictSet.add(task2);

		tmc.getActiveOffice().loadSystem();

		assertEquals(conflictSetOriginal, planner.getConflictingTasks(task2,
				time1.minusHours(1), allTasks));
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
