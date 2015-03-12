package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskManager.Project;
import taskManager.ProjectController;
import taskManager.ProjectFinishingStatus;
import taskManager.Task;

public class UseCase5AdvanceTime {

	private LocalDateTime now;
	private ProjectController controller;
	private Project project1;
	private Project project2;
	private Task task1;
	private Task task2;
	private Task task3;

	@Before
	public void setUp() {
		// create controller, 2 projects with 3 tasks in total
		now = LocalDateTime.of(2015, 03, 05, 00, 00);
		controller = new ProjectController(now);
		controller.createProject("project1", "description", now.plusDays(5));
		controller.createProject("project2", "description", now.plusDays(3));

		project1 = controller.getAllProjects().get(0);
		project2 = controller.getAllProjects().get(0);

		project1.createTask("task 1 description", Duration.ofHours(20), 20);
		project2.createTask("task 2 description", Duration.ofHours(20), 20);
		project2.createTask("task 3 description", Duration.ofHours(20), 20);

		task1 = project1.getAllTasks().get(0);
		task2 = project2.getAllTasks().get(1);
		task3 = project2.getAllTasks().get(2);

	}

	@Test
	public void advanceTime() {
		controller.advanceTime(now.plusHours(10));

		assertEquals(ProjectFinishingStatus.ON_TIME, project1.finishedOnTime());
		
	}
}
