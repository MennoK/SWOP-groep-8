package useCase;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskManager.Project;
import taskManager.ProjectExpert;

public class UseCase3CreateTaskTester {

	private ProjectExpert controller;
	private LocalDateTime now;

	@Before
	public void setUp() {
		// create a controller
		now = LocalDateTime.of(2015, 03, 07, 01, 00);
		controller = new ProjectExpert(now);

	}

	@Test
	public void createTask() {

		// create a project
		controller.createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 01, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));
		Project project1 = controller.getAllProjects().get(0);

		// create a simple task
		project1.new TaskBuilder("simple descr", Duration.ofHours(20), 50)
				.build();
		assertEquals(1, project1.getAllTasks().size());
		assertEquals("simple descr", project1.getAllTasks().get(0)
				.getDescription());
		assertEquals(Duration.ofHours(20), project1.getAllTasks().get(0)
				.getEstimatedDuration());
		assertEquals(50,
				project1.getAllTasks().get(0).getAcceptableDeviation(), 0.001);

		// create a new task dependent on the simple task
		project1.new TaskBuilder("task with dependency", Duration.ofHours(20),
				50).addDependencies(project1.getAllTasks().get(0)).build();

		assertEquals(2, project1.getAllTasks().size());
		assertEquals(project1.getAllTasks().get(0),
				project1.getAllTasks().get(1).getDependencies().get(0));

		// create an alternative task for a failed task
		// first we let the simple task fail
		project1.getAllTasks()
				.get(0)
				.updateStatus(LocalDateTime.of(2015, 03, 07, 02, 00),
						LocalDateTime.of(2015, 03, 07, 05, 00), true);

		// we create an alternative task for the simple task
		project1.new TaskBuilder("alternative task", Duration.ofHours(20), 50)
				.setOriginalTask(project1.getAllTasks().get(0)).build();

		assertEquals(3, project1.getAllTasks().size());
		assertEquals("alternative task", project1.getAllTasks().get(2)
				.getDescription());

		// task with dependency is now dependent on alternative task
		assertEquals(project1.getAllTasks().get(2),
				project1.getAllTasks().get(1).getDependencies().get(0));

	}
}
