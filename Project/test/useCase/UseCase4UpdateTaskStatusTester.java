package useCase;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Project;
import taskmanager.Task;
import taskmanager.TaskStatus;

public class UseCase4UpdateTaskStatusTester extends UseCaseTestBasis {

	private Project project1;
	private Task task1;
	private Task task2;
	private Task task3;
	private Task task4;

	@Before
	public void setUp() {
		super.setUpTMC(LocalDateTime.of(2015, 03, 02, 00, 00));
		// create a controller and a project with 3 tasks
		// task 3 is dependent on task 1

		project1 = tmc.createProject("Project 1", "Description 1",
				now.minusDays(1), now.plusDays(8));

		task1 = Task.builder("Task 1", Duration.ofHours(8), 0.4)
				.build(project1);
		Developer dev1 = tmc.createDeveloper("dev1");
		tmc.createPlanning(now, task1, dev1).build();
		task2 = Task.builder("Task 2", Duration.ofHours(8), 0.4)
				.build(project1);
		Developer dev2 = tmc.createDeveloper("dev2");
		tmc.createPlanning(now, task2, dev2).build();

		// task 3 has dependency on task2
		task3 = Task.builder("Task 3", Duration.ofHours(8), 0.4)
				.addDependencies(task2).build(project1);
		Developer dev3 = tmc.createDeveloper("dev3");
		tmc.createPlanning(now, task3, dev3).build();
		// task 4 had dependency on task 2
		task4 = Task.builder("Task 4", Duration.ofHours(8), 0.4)
				.addDependencies(task2).build(project1);
		Developer dev4 = tmc.createDeveloper("dev4");
		tmc.createPlanning(now, task4, dev4).build();
	}

	@Test
	public void updateTaskStatusSuccess() {
		// initial status
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task4.getStatus());

		// Available -> failed
		tmc.setExecuting(task1, now);
		tmc.setFailed(task1, now.plusHours(11));
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		// Available -> Finished && Unavailable -> Available
		tmc.setExecuting(task2, now);
		tmc.setFinished(task2, now.plusHours(11));
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());

	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusUnavailableToExecuting() {
		// Unavailable -> Executing
		tmc.setExecuting(task4, now);
	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusFailedToUnfailed() {
		// task 1 failed
		tmc.setExecuting(task1, now);
		tmc.setFailed(task1, now.plusHours(11));

		// task 1 failed -> unfailed throws illegalState
		tmc.setFinished(task1, now.plusHours(15));
	}

	@Test(expected = IllegalStateException.class)
	public void updateTaskStatusFinishedToFailed() {
		// task 2 finished
		tmc.setExecuting(task2, now);
		tmc.setFinished(task2, now.plusHours(11));
		// task 2 finished -> failed throws illegalState
		tmc.setFailed(task2, now.plusHours(15));
	}

}
