package taskmanager;

import static org.junit.Assert.*;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.exception.ConlictingPlanningException;

public class PlanningTester extends TaskManTester {

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
		// create some resources
		resourceType = ResourceType.builder("type")
				.build(tmc.getActiveOffice());
		resource1 = resourceType.createResource("resource");
		resource2 = resourceType.createResource("resource2");

		// create a project with a task
		project = tmc.createProject("name", "des",
				time.plusHours(3).plusDays(13));
		task1 = Task.builder("task 1", Duration.ofHours(1), 1).build(project);
		task2 = Task.builder("task 2", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);

		// create some developers
		developer1 = tmc.createDeveloper("person1");
		developer2 = tmc.createDeveloper("person2");
	}

	@Test
	public void createSimplePlanning() {
		// create planning for task1 (needs no resources)
		Planning plan1 = Planning
				.builder(time, task1, developer1, tmc.getPlanner())
				.addDeveloper(developer2).build();
		// check if 1 planning exist
		assertEquals(1, tmc.getPlanner().getAllPlannings().size());

		// create planning for task2 (needs 1 resource)
		Planning plan2 = Planning
				.builder(time.plusHours(3), task2, developer1, tmc.getPlanner())
				.addDeveloper(developer2).addResources(resource1)
				.addResources(resource2).build();

		// check if 2 planing exist
		assertEquals(2, tmc.getPlanner().getAllPlannings().size());

		// check if the planing are made correctly
		assertEquals(time, plan1.getTimeSpan().getBegin());
		assertEquals(time.plus(task1.getDuration()), plan1.getTimeSpan()
				.getEnd());
		assertTrue(plan1.getDevelopers().contains(developer1));
		assertTrue(plan1.getDevelopers().contains(developer2));
		assertTrue(plan1.getResources().isEmpty());

		assertEquals(time.plusHours(3), plan2.getTimeSpan().getBegin());
		assertEquals(time.plusHours(3).plus(task2.getDuration()), plan2
				.getTimeSpan().getEnd());
		assertTrue(plan2.getDevelopers().contains(developer1));
		assertTrue(plan2.getDevelopers().contains(developer2));
		assertTrue(plan2.getResources().contains(resource1));
		assertTrue(plan2.getResources().contains(resource2));
	}

	@Test(expected = ConlictingPlanningException.class)
	public void createPlanningInvalidResources() {
		Task.builder("task 3", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);
		Task task3 = project.getAllTasks().get(2);
		Planning.builder(time, task3, developer1, tmc.getPlanner())
				.addResources(resource1).build();
		Planning.builder(time, task2, developer2, tmc.getPlanner())
				.addResources(resource1).build();
	}

	@Test(expected = ConlictingPlanningException.class)
	public void createPlanningInvalidDeveloper() {
		Planning.builder(time, task1, developer1, tmc.getPlanner()).build();
		Planning.builder(time, task2, developer1, tmc.getPlanner())
				.addResources(resource1).build();
	}

}
