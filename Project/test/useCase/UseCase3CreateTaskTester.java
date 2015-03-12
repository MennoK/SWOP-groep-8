package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import taskManager.Project;
import taskManager.ProjectController;
import taskManager.Task;
import taskManager.TaskManClock;
import taskManager.exception.InvalidTimeException;

public class UseCase3CreateTaskTester {

	private ProjectController controller;
	private LocalDateTime now;

	@Before
	public void setUp() {
		// create a controller
		now = LocalDateTime.of(2015, 03, 07, 01, 00);
		controller = new ProjectController(now);

	}

	@Test
	public void createTask() throws InvalidTimeException {
		// create a project
		controller.createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 01, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));
		Project project1 = controller.getAllProjects().get(0);

		// create a simple task
		project1.createTask("simple descr", Duration.ofHours(20), 50, now);
		assertEquals(1, project1.getAllTasks().size());
		assertEquals("simple descr", project1.getAllTasks().get(0)
				.getDescription());
		assertEquals(Duration.ofHours(20), project1.getAllTasks().get(0)
				.getEstimatedDuration());
		assertEquals(50,
				project1.getAllTasks().get(0).getAcceptableDeviation(), 0.001);

		// create a new task dependent on the simple task
		ArrayList<Task> dependencyList = new ArrayList<Task>();
		dependencyList.add(project1.getAllTasks().get(0));
		project1.createTask("task with dependency", Duration.ofHours(20), 50,
				dependencyList);

		assertEquals(2, project1.getAllTasks().size());
		assertEquals(project1.getAllTasks().get(0),
				project1.getAllTasks().get(1).getDependencies().get(0));

		// create an alternative task for a failed task
		// first we let the dependent task fail
		project1.getAllTasks()
				.get(1)
				.updateStatus(LocalDateTime.of(2015, 03, 07, 02, 00),
						LocalDateTime.of(2015, 03, 07, 05, 00), true);
		project1.createTask("alternative task", Duration.ofHours(20), 50,
				project1.getAllTasks().get(1));

		assertEquals(3, project1.getAllTasks().size());
		assertEquals("alternative task", project1.getAllTasks().get(2)
				.getDescription());
	}
}
