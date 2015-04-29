package useCase;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskManager.Developer;
import taskManager.Planning;
import taskManager.Project;
import taskManager.ProjectExpert;
import taskManager.Task;
import taskManager.TaskManController;
import taskManager.TaskStatus;

public class UseCase4UpdateTaskStatusTester {

	private ProjectExpert controller;
	private TaskManController taskManController;
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

		now = LocalDateTime.of(2015, 03, 02, 00, 00);

		taskManController = new TaskManController(now);
		controller = taskManController.getProjectExpert();
		controller.createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 01, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));

		project1 = controller.getAllProjects().get(0);

		task1 = Task.builder("Task 1", Duration.ofHours(8), 0.4)
				.build(project1);
		Developer dev1 = taskManController.getDeveloperExpert()
				.createDeveloper("dev1");
		Planning.builder(now, task1, dev1)
				.build(taskManController.getPlanner());
		task2 = Task.builder("Task 2", Duration.ofHours(8), 0.4)
				.build(project1);
		Developer dev2 = taskManController.getDeveloperExpert()
				.createDeveloper("dev2");
		Planning.builder(now, task2, dev2)
				.build(taskManController.getPlanner());

		// task 3 has dependency on task2
		task3 = Task.builder("Task 3", Duration.ofHours(8), 0.4)
				.addDependencies(task2).build(project1);
		Developer dev3 = taskManController.getDeveloperExpert()
				.createDeveloper("dev3");
		Planning.builder(now, task3, dev3)
				.build(taskManController.getPlanner());
		// task 4 had dependency on task 2
		task4 = Task.builder("Task 4", Duration.ofHours(8), 0.4)
				.addDependencies(task2).build(project1);
		Developer dev4 = taskManController.getDeveloperExpert()
				.createDeveloper("dev4");
		Planning.builder(now, task4, dev4)
				.build(taskManController.getPlanner());
	}

	@Test
	public void updateTaskStatusSuccess() {
		// initial status
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task4.getStatus());

		// Available -> failed
		taskManController.setExecuting(task1, now);
		taskManController.setFailed(task1, now.plusHours(11));
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		// Available -> Finished && Unavailable -> Available
		taskManController.setExecuting(task2, now);
		taskManController.setFinished(task2, now.plusHours(11));
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());

	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusUnavailableToExecuting() {
		// Unavailable -> Executing
		taskManController.setExecuting(task4, now);
	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusFailedToUnfailed() {
		// task 1 failed
		taskManController.setExecuting(task1, now);
		taskManController.setFailed(task1, now.plusHours(11));

		// task 1 failed -> unfailed throws illegalState
		taskManController.setFinished(task1, now.plusHours(15));
	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusFinishedToFailed() {
		// task 2 finished
		taskManController.setExecuting(task2, now);
		taskManController.setFinished(task2, now.plusHours(11));
		// task 2 finished -> failed throws illegalState
		taskManController.setFailed(task2, now.plusHours(15));
	}

}
