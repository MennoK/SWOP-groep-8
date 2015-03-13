package useCase;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import taskManager.Project;
import taskManager.ProjectController;
import taskManager.Task;
import taskManager.TaskStatus;

public class UseCase4UpdateTaskStatusTester {

	private ProjectController controller;
	private Project project1;
	private Task task1;
	private Task task2;
	private Task task3;
	private Task task4;

	private LocalDateTime now;

	@Before
	public void setUp() {
		// create a contoller and a project with 3 tasks
		// task 3 is dependent on task 1

		controller = new ProjectController(now);
		controller.createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 01, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));

		project1 = controller.getAllProjects().get(0);

		project1.createTask("Task 1", Duration.ofHours(8), 0.4);
		project1.createTask("Task 2", Duration.ofHours(8), 0.4);
		task1 = project1.getAllTasks().get(0);
		task2 = project1.getAllTasks().get(1);

		ArrayList<Task> dependency = new ArrayList<>();
		dependency.add(task2);
		// task 3 has dependency on task2
		project1.createTask("Task 3", Duration.ofHours(8), 0.4, dependency);
		// task 4 had depndency on task 2
		project1.createTask("Task 4", Duration.ofHours(8), 0.4, dependency);
		task3 = project1.getAllTasks().get(2);
		task4 = project1.getAllTasks().get(3);

	}

	@Test
	public void updateTaskStatusSuccess() {
		// initial status
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task4.getStatus());

		// Unavailable -> failed
		task4.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), true);
		assertEquals(TaskStatus.FAILED, task4.getStatus());
		// Available -> failed
		task1.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), true);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		// Available -> Finished && Unavailable -> Available
		task2.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), false);
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());

	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusUnavailableToFinished() {
		// unavailable -> Finished throws illegalState
		task3.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), false);

	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusFailedToUnfailed() {
		// task 1 failed
		task1.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), true);

		// task 1 failed -> unfailed throws illegalState
		task1.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), false);

	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusFinishedToFailed() {
		// task 2 finished
		task2.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), false);
		// task 2 finished -> failed throws illegalState
		task2.updateStatus(LocalDateTime.of(2015, 03, 02, 00, 00),
				LocalDateTime.of(2015, 03, 02, 11, 00), true);
	}


}
