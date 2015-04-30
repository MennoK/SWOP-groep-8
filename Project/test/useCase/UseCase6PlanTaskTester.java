package useCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import utility.TimeSpan;
import taskManager.Developer;
import taskManager.DeveloperExpert;
import taskManager.Planner;
import taskManager.Planning;
import taskManager.Project;
import taskManager.ResourceType;
import taskManager.Task;
import taskManager.TaskManController;
import taskManager.exception.ConlictingPlanningException;

public class UseCase6PlanTaskTester {

	public TaskManController tmc;
	public Planner planner;
	public LocalDateTime time1;
	public LocalDateTime time2;
	public Project project;
	public Task task1;
	public Task task2;
	public DeveloperExpert developerExpert;
	public Set<Developer> developers;
	private ArrayList<ResourceType> resourceTypeList;
	private ResourceType resourceType;
	private ArrayList<Developer> developerList;

	@Before
	public void setUp() {
		// 2 default times
		this.time1 = LocalDateTime.of(2015, 03, 10, 11, 00);
		this.time2 = LocalDateTime.of(2015, 03, 10, 15, 00);
		tmc = new TaskManController(time1);
		// create planning expert
		this.planner = tmc.getPlanner();
		// create some resources
		ResourceType.builder("type").build(tmc);
		resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);
		resourceType.createResource("resource");
		resourceType.createResource("resource2");

		// create a project with 2 tasks
		tmc.createProject("name", "des", time2.plusDays(13));
		project = tmc.getAllProjects().get(0);
		Task.builder("a task", Duration.ofHours(1), 1).build(project);
		Task.builder("a task", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		task1 = project.getAllTasks().get(0);
		task2 = project.getAllTasks().get(1);

		// create some developers
		tmc.createDeveloper("person1");
		tmc.createDeveloper("person2");
		developers = new LinkedHashSet<>(tmc.getAllDevelopers());
		developerList = new ArrayList<Developer>(tmc.getAllDevelopers());
	}

	@Test
	public void planTask() {
		// user gets list with all unplanned tasks (task1 and task2)

		Set<Task> unplannedTasks = new LinkedHashSet<>(project.getAllTasks());
		project.getAllTasks();
		assertEquals(unplannedTasks, tmc.getUnplannedTasks());
		// user selects task2 and receives 3 possible start times
		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<>();
		possibleStartTimes.add(time1);
		possibleStartTimes.add(time1.plusHours(1));
		possibleStartTimes.add(time1.plusHours(2));
		assertEquals(possibleStartTimes,
				planner.getPossibleStartTimes(task2, time1, developers));
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		// user selects time1
		// the system shows possible available resources
		assertEquals(resourceType.getAllResources(),
				planner.resourcesOfTypeAvailableFor(resourceType, task2,
						timeSpan));
		// user selects a resource
		// system shows developers
		assertEquals(developers,
				planner.developersAvailableFor(developers, task2, timeSpan));
		// user selects a developer

		// system makes reservation
		Planning.builder(time1, task2, developerList.get(0), planner).build();

		ArrayList<Planning> planningList = new ArrayList<Planning>();
		planningList.addAll(planner.getAllPlannings());
		assertEquals(this.time1, planningList.get(0).getTimeSpan().getBegin());
		assertEquals(time1.plus(task2.getDuration()), planningList.get(0)
				.getTimeSpan().getEnd());
		assertTrue(planningList.get(0).getDevelopers()
				.contains(developerList.get(0)));
		assertEquals(1, planningList.get(0).getDevelopers().size());
		assertTrue(planningList.get(0).getResources().isEmpty());

	}

	@Test(expected = ConlictingPlanningException.class)
	public void extensionUserSelectsTime() {
		Planning.builder(time1, task1, developerList.get(0), planner).build();
		// the user selects a time for task2 that will conflict with task1
		Planning.builder(time1, task2, developerList.get(0), planner).build();
		// use case resolve conflict starts
	}

	@Test
	public void resolveConflict() {
		Planning.builder(time1, task1, developerList.get(0), planner).build();
		// use case resolveconflict starts
		// user chooses to move conflicting task
		// step 4 of use case plan task for the task that must be moved:
		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<>();
		possibleStartTimes.add(time1);
		possibleStartTimes.add(time1.plusHours(1));
		possibleStartTimes.add(time1.plusHours(2));
		assertEquals(possibleStartTimes,
				planner.getPossibleStartTimes(task1, time1, developers));

		// user selects time1 +2
		task1.getPlanning().setTimeSpan(
				new TimeSpan(time1.plusHours(2), task1.getDuration()));

		// resolve conflict ends -> back to original planning of the task

		Planning.builder(time1, task2, developerList.get(0), planner).build();

		ArrayList<Planning> planningList = new ArrayList<Planning>();
		planningList.addAll(planner.getAllPlannings());
		assertEquals(this.time1.plusHours(2), planningList.get(0).getTimeSpan()
				.getBegin());
		assertEquals(this.time1, planningList.get(1).getTimeSpan().getBegin());
	}

}
