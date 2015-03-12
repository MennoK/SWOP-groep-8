package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import taskManager.*;
import taskManager.exception.InvalidTimeException;

public class UseCase1ShowProjectsTester {

	private ProjectController controller;
	private Project project1;
	private Project project2;
	private Project project0;
	private Task task1;
	private Task task2;
	private Task task3;

	private LocalDateTime now;

	@Before
	public void setUp() throws InvalidTimeException {
		// create a controller, 3 projects and 3 tasks:
		// project0 has 0 tasks
		// project1 has 1 task (finished)
		// project2 has 2 tasks (1 task is dependent on the other)

		now = LocalDateTime.of(2015, 03, 07, 01, 00);

		controller = new ProjectController(now);
		controller.createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 03, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));
		controller.createProject("Project 2", "Description 2",
				LocalDateTime.of(2015, 03, 03, 00, 00),
				LocalDateTime.of(2015, 03, 11, 00, 00));
		controller.createProject("Project 0", "Description 3",
				LocalDateTime.of(2015, 03, 03, 00, 00),
				LocalDateTime.of(2015, 03, 12, 00, 00));

		project0 = controller.getAllProjects().get(2);
		project1 = controller.getAllProjects().get(0);
		project2 = controller.getAllProjects().get(1);

		project1.createTask("Task 1", Duration.ofHours(2), 0.4, now);
		project2.createTask("Task 2", Duration.ofHours(2), 0.4, now);
		ArrayList<Task> dependencies = new ArrayList<Task>();
		dependencies.add(project2.getAllTasks().get(0));
		project2.createTask("Task 3", Duration.ofHours(3), 0.4, dependencies);

		task1 = project1.getAllTasks().get(0);
		task1.updateStatus(LocalDateTime.of(2015, 03, 04, 00, 00),
				LocalDateTime.of(2015, 03, 05, 00, 00), false);
		task2 = project2.getAllTasks().get(0);
		task3 = project2.getAllTasks().get(1);
	}

	@Test
	public void showProjects() {

		// List all projects
		List<Project> allProjectsActuals = new ArrayList<Project>();
		allProjectsActuals = controller.getAllProjects();
		assertEquals(project1, allProjectsActuals.get(0));
		assertEquals(project2, allProjectsActuals.get(1));
		assertEquals(project0, allProjectsActuals.get(2));

		// Show their status: project with zero tasks in ongoing
		assertEquals(ProjectStatus.ONGOING, project0.getStatus());
		assertEquals(ProjectStatus.FINISHED, project1.getStatus());
		assertEquals(ProjectStatus.ONGOING, project2.getStatus());

		// show details of the projects: delay and estimated finished time
		// TODO

		// show tasks of each project
		assertEquals(0, project0.getAllTasks().size());
		assertEquals(1, project1.getAllTasks().size());
		assertEquals(task1, project1.getAllTasks().get(0));
		assertEquals(2, project2.getAllTasks().size());

		assertEquals(task2, project2.getAllTasks().get(0));
		assertEquals(task3, project2.getAllTasks().get(1));

		// show task status
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());

	}

	/*
	 * //TODO:
	 * 
	 * @Test public void testGetEstimatedFinishTime() {
	 * 
	 * }
	 * 
	 * @Test public void testGetTotalDelayLargerThenZero() { //Delay task 1 = 1h
	 * task1.setStartTime(LocalDateTime.of(2015, 03, 03,8,00));
	 * task1.setEndTime(LocalDateTime.of(2015, 03, 03,11,00)); assertEquals(1,
	 * project1.getTotalDelay());
	 * 
	 * 
	 * //delay task 2 & 3 > 0 task2.setStartTime(LocalDateTime.of(2015, 03,
	 * 03,10,00)); task2.setEndTime(LocalDateTime.of(2015, 03, 03,13,00));
	 * 
	 * task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
	 * task3.setEndTime(LocalDateTime.of(2015, 03, 03,16,00));
	 * 
	 * assertEquals(4, project1.getTotalDelay());
	 * 
	 * //delay taks 2 = 0 & task 3 > 0 task2.setStartTime(LocalDateTime.of(2015,
	 * 03, 03,10,00)); task2.setEndTime(LocalDateTime.of(2015, 03, 03,12,00));
	 * 
	 * task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
	 * task3.setEndTime(LocalDateTime.of(2015, 03, 03,16,00));
	 * 
	 * assertEquals(3, project1.getTotalDelay());
	 * 
	 * }
	 * 
	 * @Test public void testGetTotalDelayEqualsZero() { //Delay task 1 = 0h
	 * task1.setStartTime(LocalDateTime.of(2015, 03, 03,8,00));
	 * task1.setEndTime(LocalDateTime.of(2015, 03, 03,10,00)); assertEquals(0,
	 * project1.getTotalDelay());
	 * 
	 * //delay task 2 & 3 = 0 task2.setStartTime(LocalDateTime.of(2015, 03,
	 * 03,10,00)); task2.setEndTime(LocalDateTime.of(2015, 03, 03,12,00));
	 * task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
	 * task3.setEndTime(LocalDateTime.of(2015, 03, 03,13,00));
	 * 
	 * assertEquals(0, project1.getTotalDelay());
	 * 
	 * 
	 * }
	 * 
	 * 
	 * @Test public void testGetTotalDelaySmallerThenZero() { //Delay task 1 =
	 * -1h task1.setStartTime(LocalDateTime.of(2015, 03, 03,8,00));
	 * task1.setEndTime(LocalDateTime.of(2015, 03, 03,9,00)); assertEquals(-1,
	 * project1.getTotalDelay());
	 * 
	 * //delay task 2 = 1h & 3 = -2h task2.setStartTime(LocalDateTime.of(2015,
	 * 03, 03,10,00)); task2.setEndTime(LocalDateTime.of(2015, 03, 03,13,00));
	 * task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
	 * task3.setEndTime(LocalDateTime.of(2015, 03, 03,11,00));
	 * 
	 * assertEquals(-1, project1.getTotalDelay());
	 * 
	 * 
	 * }
	 */
}
