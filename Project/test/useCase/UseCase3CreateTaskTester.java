package useCase;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.BranchOffice;

public class UseCase3CreateTaskTester {

	private BranchOffice tmc;
	private LocalDateTime now;

	@Before
	public void setUp() {
		// create a controller
		now = LocalDateTime.of(2015, 03, 07, 01, 00);

		tmc = new BranchOffice(now);
		ResourceType.builder("resourcetype").build(tmc);

		List<ResourceType> list = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		list.get(0).createResource("res1");

	}

	@Test
	public void createTask() {

		// create a project
		tmc.createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 01, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));
		Project project1 = tmc.getAllProjects().get(0);

		// create a simple task
		Task.builder("simple descr", Duration.ofHours(20), 50).build(project1);
		assertEquals(1, project1.getAllTasks().size());
		assertEquals("simple descr", project1.getAllTasks().get(0)
				.getDescription());
		assertEquals(Duration.ofHours(20), project1.getAllTasks().get(0)
				.getEstimatedDuration());
		assertEquals(50,
				project1.getAllTasks().get(0).getAcceptableDeviation(), 0.001);

		// create a new task dependent on the simple task
		Task.builder("task with dependency", Duration.ofHours(20), 50)
				.addDependencies(project1.getAllTasks().get(0)).build(project1);

		assertEquals(2, project1.getAllTasks().size());
		assertEquals(project1.getAllTasks().get(0),
				project1.getAllTasks().get(1).getDependencies().get(0));

		// create an alternative task for a failed task
		// first we let the simple task fail
		Task task = project1.getAllTasks().get(0);
		Developer dev = tmc.createDeveloper("dev");
		Planning.builder(now, task, dev, tmc.getPlanner()).build();
		tmc.setExecuting(task, LocalDateTime.of(2015, 03, 07, 02, 00));
		tmc.setFailed(task, LocalDateTime.of(2015, 03, 07, 05, 00));

		// we create an alternative task for the simple task
		Task.builder("alternative task", Duration.ofHours(20), 50)
				.setOriginalTask(project1.getAllTasks().get(0)).build(project1);

		assertEquals(3, project1.getAllTasks().size());
		assertEquals("alternative task", project1.getAllTasks().get(2)
				.getDescription());

		// task with dependency is now dependent on alternative task
		assertEquals(project1.getAllTasks().get(2),
				project1.getAllTasks().get(1).getDependencies().get(0));

		// task with required resourceType
		Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(
						new ArrayList<ResourceType>(tmc.getAllResourceTypes())
								.get(0),
						1).build(project1);
		assertEquals(1, project1.getAllTasks().get(3)
				.getRequiredResourceTypes().size());
	}
}
