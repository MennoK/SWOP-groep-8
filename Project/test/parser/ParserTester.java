package parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.BeforeClass;
import org.junit.Test;

import taskManager.Project;
import taskManager.ProjectController;
import taskManager.Task;

public class ParserTester {

	DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm");
	static ProjectController projectController;

	// run setup only once
	@BeforeClass
	public static void setUp() {
		projectController = new ProjectController(null);
		try {
			new Parser().parse("./input.tman", projectController);
		} catch (FileNotFoundException | RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testThreeProjectsAreMade() {
		assertEquals(3, projectController.getAllProjects().size());
	}

	@Test
	public void testProjectxIsMade() {
		Project projectx = projectController.getAllProjects().get(0);
		assertEquals("project x", projectx.getName());
		assertEquals("a project description", projectx.getDescription());
		assertEquals(projectx.getCreationTime(),
				LocalDateTime.parse(("2014-01-01 09:00"), dateTimeFormatter));
		assertEquals(projectx.getDueTime(),
				LocalDateTime.parse(("2014-02-01 18:00"), dateTimeFormatter));
	}

	@Test
	public void testProjectyIsMade() {
		Project projecty = projectController.getAllProjects().get(1);
		assertEquals("project y", projecty.getName());
		assertEquals("another project description", projecty.getDescription());
		assertEquals(projecty.getCreationTime(),
				LocalDateTime.parse(("2015-01-01 09:00"), dateTimeFormatter));
		assertEquals(projecty.getDueTime(),
				LocalDateTime.parse(("2016-01-01 18:00"), dateTimeFormatter));
	}

	@Test
	public void testProjectzIsMade() {
		Project projectz = projectController.getAllProjects().get(2);
		assertEquals("project z", projectz.getName());
		assertEquals(projectz.getDescription(),
				"yet another project description");
		assertEquals(projectz.getCreationTime(),
				LocalDateTime.parse(("2015-04-25 09:00"), dateTimeFormatter));
		assertEquals(projectz.getDueTime(),
				LocalDateTime.parse(("2015-04-30 18:00"), dateTimeFormatter));
	}

	@Test
	public void testOneTaskOfProjectxIsMade() {
		Project projectx = projectController.getAllProjects().get(0);
		assertEquals(1, projectx.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectxIsMade() {
		Project projectx = projectController.getAllProjects().get(0);
		Task task1 = projectx.getAllTasks().get(0);

		assertEquals("task description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());
		assertEquals(taskManager.TaskStatus.FINISHED, task1.getStatus());
		assertEquals(task1.getStartTime(),
				LocalDateTime.parse(("2014-01-01 10:00"), dateTimeFormatter));
		assertEquals(task1.getEndTime(),
				LocalDateTime.parse(("2014-01-02 17:00"), dateTimeFormatter));
		assertNull(task1.getOriginal());
	}

	@Test
	public void testFourTasksOfProjectyAreMade() {
		Project projecty = projectController.getAllProjects().get(1);
		assertEquals(4, projecty.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectyIsMade() {
		Project projecty = projectController.getAllProjects().get(1);
		Task task1 = projecty.getAllTasks().get(0);

		assertEquals("another task description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());

		assertNull(task1.getOriginal());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task1.getStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task1.getStatus());
	}

	@Test
	public void testTaskTwoOfProjectyIsMade() {
		Project projecty = projectController.getAllProjects().get(1);
		Task task2 = projecty.getAllTasks().get(1);

		assertEquals("yet another task description", task2.getDescription());
		assertEquals(Duration.ofHours(100), task2.getEstimatedDuration());
		assertEquals(task2.getAcceptableDeviation(), 0.10, 0.001);
		assertEquals(0, task2.getDependencies().size());

		assertNull(task2.getOriginal());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task2.getStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task2.getStatus());
	}

	@Test
	public void testTaskThreeOfProjectyIsMade() {
		Project projecty = projectController.getAllProjects().get(1);
		Task task3 = projecty.getAllTasks().get(2);

		assertEquals("description", task3.getDescription());
		assertEquals(Duration.ofHours(50), task3.getEstimatedDuration());
		assertEquals(0, task3.getAcceptableDeviation(), 0.001);
		assertNull(task3.getOriginal());

		assertEquals(2, task3.getDependencies().size());
		assertEquals(taskManager.TaskStatus.FAILED, task3.getStatus());
		assertEquals(
				LocalDateTime.parse(("2015-01-01 09:00"), dateTimeFormatter),
				task3.getStartTime());
		assertEquals(
				LocalDateTime.parse(("2015-01-30 18:00"), dateTimeFormatter),
				task3.getEndTime());
	}

	@Test
	public void testTaskFourOfProjectyIsMade() {
		Project projecty = projectController.getAllProjects().get(1);
		Task task4 = projecty.getAllTasks().get(3);

		assertEquals("description", task4.getDescription());
		assertEquals(Duration.ofHours(50), task4.getEstimatedDuration());
		assertEquals(task4.getAcceptableDeviation(), 0, 0.001);
		assertEquals(2, task4.getDependencies().size());
		assertEquals(task4.getOriginal(), projectController
				.getAllProjects().get(1).getAllTasks().get(2));

		assertNull(task4.getStartTime());
		assertNull(task4.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task4.getStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task4.getStatus());
	}

	@Test
	public void testTwoTasksOfProjectzAreMade() {
		Project projectz = projectController.getAllProjects().get(2);
		assertEquals(2, projectz.getAllTasks().size());
	}

	@Test
	public void testTaskOneOfProjectzIsMade() {
		Project projectz = projectController.getAllProjects().get(2);
		Task task1 = projectz.getAllTasks().get(0);

		assertEquals("description", task1.getDescription());
		assertEquals(Duration.ofHours(500), task1.getEstimatedDuration());
		assertEquals(task1.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task1.getDependencies().size());

		assertNull(task1.getOriginal());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task1.getStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task1.getStatus());
	}

	@Test
	public void testTaskTwoOfProjectzIsMade() {
		Project projectz = projectController.getAllProjects().get(2);
		Task task2 = projectz.getAllTasks().get(1);

		assertEquals("description", task2.getDescription());
		assertEquals(Duration.ofHours(500), task2.getEstimatedDuration());
		assertEquals(task2.getAcceptableDeviation(), 0.50, 0.001);
		assertEquals(0, task2.getDependencies().size());
		assertNull(task2.getOriginal());

		assertNull(task2.getOriginal());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(taskManager.TaskStatus.FAILED, task2.getStatus());
		assertNotEquals(taskManager.TaskStatus.FINISHED, task2.getStatus());
	}
}
