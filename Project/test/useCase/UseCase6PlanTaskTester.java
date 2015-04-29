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
import taskManager.ProjectExpert;
import taskManager.ResourceExpert;
import taskManager.ResourceType;
import taskManager.Task;
import taskManager.TaskManController;

public class UseCase6PlanTaskTester {

	public TaskManController tmController;
	public Planner planner;
	public LocalDateTime time1;
	public LocalDateTime time2;
	public Project project;
	public Task task1;
	public Task task2;
	public DeveloperExpert developerExpert;
	public Set<Developer> developers;
	private ResourceExpert resourceExpert;
	private ArrayList<ResourceType> resourceTypeList;
	private ResourceType resourceType;
	private ProjectExpert projectExpert;
	private ArrayList<Developer> developerList;

	@Before
	public void setUp() {
		// 2 default times
		this.time1 = LocalDateTime.of(2015, 03, 10, 11, 00);
		this.time2 = LocalDateTime.of(2015, 03, 10, 15, 00);
		tmController = new TaskManController(time1);
		// create planning expert
		this.planner = tmController.getPlanner();
		// create some resources
		resourceExpert = tmController.getResourceExpert();
		ResourceType.builder("type").build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);
		resourceType.createResource("resource");
		resourceType.createResource("resource2");

		// create a project with 2 tasks

		projectExpert = tmController.getProjectExpert();
		projectExpert.createProject("name", "des", time2.plusDays(13));
		project = projectExpert.getAllProjects().get(0);
		Task.builder("a task", Duration.ofHours(1), 1).build(project);
		Task.builder("a task", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		task1 = project.getAllTasks().get(0);
		task2 = project.getAllTasks().get(1);

		// create some developers
		developerExpert = tmController.getDeveloperExpert();
		developerExpert.createDeveloper("person1");
		developerExpert.createDeveloper("person2");
		developers = new LinkedHashSet<>(developerExpert.getAllDevelopers());
		developerList = new ArrayList<Developer>(
				developerExpert.getAllDevelopers());
	}

	@Test
	public void planTask() {
		// user gets list with all unplanned tasks (task1 and task2)

		Set<Task> unplannedTasks = new LinkedHashSet<>(project.getAllTasks());
		project.getAllTasks();
		assertEquals(unplannedTasks, tmController.getUnplannedTasks());
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
		Planning.builder(time1, task2, developerList.get(0)).build(planner);

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
}
