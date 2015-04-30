package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import taskManager.Developer;
import taskManager.Planner;
import taskManager.Planning;
import taskManager.Project;
import taskManager.Resource;
import taskManager.ResourceType;
import taskManager.Task;
import taskManager.TaskManController;
import taskManager.exception.ConlictingPlanningException;

public class PlanningTester {

	public TaskManController tmc;
	public Planner planner;
	public LocalDateTime time1;
	public LocalDateTime time2;
	public Project project;
	public Task task1;
	public Task task2;
	private ArrayList<ResourceType> resourceTypeList;
	private ResourceType resourceType;
	private Developer developer1;
	private Developer developer2;
	private ArrayList<Resource> resources;
	private HashSet<Resource> resource;
	private HashSet<Resource> resource2;

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

		resources = new ArrayList<Resource>(resourceType.getAllResources());
		resource = new HashSet<Resource>();
		resource.add(resources.get(0));
		resource2 = new HashSet<Resource>();
		resource2.add(resources.get(1));
		// create a project with a task

		tmc.createProject("name", "des", time2.plusDays(13));
		project = tmc.getAllProjects().get(0);
		Task.builder("task 1", Duration.ofHours(1), 1).build(project);
		Task.builder("task 2", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		task1 = project.getAllTasks().get(0);
		task2 = project.getAllTasks().get(1);

		// create some developers
		tmc.createDeveloper("person1resource2 = resources.get(1);");
		tmc.createDeveloper("person2");

		developer1 = (Developer) tmc.getAllDevelopers().toArray()[0];
		developer2 = (Developer) tmc.getAllDevelopers().toArray()[1];

	}

	@Test
	public void createSimplePlanning() {
		// create planning for task1 (needs no resources)
		Planning.builder(time1, task1, developer1, planner)
				.addDeveloper(developer2).build();
		// check if 1 planning exist
		assertEquals(1, planner.getAllPlannings().size());

		// create planning for task2 (needs 1 resource)

		Map<ResourceType, Set<Resource>> resources = new LinkedHashMap<ResourceType, Set<Resource>>();
		resources.put(resourceType, resourceType.getAllResources());
		Planning.builder(time2, task2, developer1, planner)
				.addDeveloper(developer2)
				.addAllResources(resourceType.getAllResources()).build();

		// check if 2 plannings exist
		assertEquals(2, planner.getAllPlannings().size());

		// check if the plannings are made correctly
		ArrayList<Planning> planningList = new ArrayList<Planning>();
		planningList.addAll(planner.getAllPlannings());
		assertEquals(this.time1, planningList.get(0).getTimeSpan().getBegin());
		assertEquals(time1.plus(task1.getDuration()), planningList.get(0)
				.getTimeSpan().getEnd());
		assertEquals(tmc.getAllDevelopers(), planningList.get(0)
				.getDevelopers());
		assertTrue(planningList.get(0).getResources().isEmpty());

		assertEquals(this.time2, planningList.get(1).getTimeSpan().getBegin());
		assertEquals(time2.plus(task2.getDuration()), planningList.get(1)
				.getTimeSpan().getEnd());
		assertEquals((tmc.getAllDevelopers()), planningList.get(1)
				.getDevelopers());
	}

	@Test(expected = ConlictingPlanningException.class)
	public void createPlanningInvalidResources() {
		Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task task3 = project.getAllTasks().get(2);
		Planning.builder(time1, task3, developer1, planner)
				.addAllResources(resource).build();
		Planning.builder(time1, task2, developer2, planner)
				.addAllResources(resource).build();
	}

	@Test(expected = ConlictingPlanningException.class)
	public void createPlanningInvalidDeveloper() {
		Planning.builder(time1, task1, developer1, planner).build();
		Planning.builder(time1, task2, developer1, planner)
				.addAllResources(resource).build();
	}

}
