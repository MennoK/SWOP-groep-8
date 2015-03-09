package Test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.Task;
import TaskManager.TaskStatus;

import parser.Parser;

public class ParserTester {

	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	static ProjectController projectController;

	//run setup only once
	@BeforeClass
	public static void setUp(){
		projectController = new ProjectController(null);
		try {
			new Parser().parse("./input.tman", projectController);
		} catch (FileNotFoundException | RuntimeException
				| LoopingDependencyException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testThreeProjectsAreMade(){
		assertEquals(projectController.getAllProjects().size(), 3);
	}

	@Test
	public void testProjectxIsMade(){
		Project projectx = projectController.getAllProjects().get(0);
		assertEquals(projectx.getName(), "project x");
		assertEquals(projectx.getDescription(), "a project description");
		assertEquals(projectx.getCreationTime(), LocalDateTime.parse(("2014-01-01 09:00"),dateTimeFormatter));
		assertEquals(projectx.getDueTime(),  LocalDateTime.parse(("2014-02-01 18:00"),dateTimeFormatter));
	}

	@Test
	public void testProjectyIsMade(){
		Project projecty = projectController.getAllProjects().get(1);
		assertEquals(projecty.getName(), "project y");
		assertEquals(projecty.getDescription(), "another project description");
		assertEquals(projecty.getCreationTime(), LocalDateTime.parse(("2015-01-01 09:00"),dateTimeFormatter));
		assertEquals(projecty.getDueTime(),  LocalDateTime.parse(("2016-01-01 18:00"),dateTimeFormatter));
	}

	@Test
	public void testProjectzIsMade(){
		Project projectz = projectController.getAllProjects().get(2);
		assertEquals(projectz.getName(), "project z");
		assertEquals(projectz.getDescription(), "yet another project description");
		assertEquals(projectz.getCreationTime(), LocalDateTime.parse(("2015-04-25 09:00"),dateTimeFormatter));
		assertEquals(projectz.getDueTime(),  LocalDateTime.parse(("2015-04-30 18:00"),dateTimeFormatter));
	}

	@Test
	public void testOneTaskOfProjectxIsMade(){
		Project projectx = projectController.getAllProjects().get(0);
		assertEquals(projectx.getAllTasks().size(),1);
	}

	@Test
	public void testTaskOneOfProjectxIsMade(){
		Project projectx = projectController.getAllProjects().get(0);
		Task task1 = projectx.getAllTasks().get(0);

		assertEquals(task1.getDescription(), "task description");
		assertEquals(task1.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task1.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task1.getDependencies().size(), 0);
		assertEquals(task1.getStatus(),TaskStatus.FINISHED);
		assertEquals(task1.getStartTime(), LocalDateTime.parse(("2014-01-01 10:00"),dateTimeFormatter));
		assertEquals(task1.getEndTime(), LocalDateTime.parse(("2014-01-02 17:00"),dateTimeFormatter));
		assertNull(task1.getAlternativeFor());
	}

	@Test
	public void testFourTasksOfProjectyAreMade(){
		Project projecty = projectController.getAllProjects().get(1);
		assertEquals(projecty.getAllTasks().size(),4);
	}

	@Test
	public void testTaskOneOfProjectyIsMade(){
		Project projecty = projectController.getAllProjects().get(1);
		Task task1 = projecty.getAllTasks().get(0);

		assertEquals(task1.getDescription(), "another task description");
		assertEquals(task1.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task1.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task1.getDependencies().size(), 0);

		assertNull(task1.getAlternativeFor());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(task1.getStatus(), TaskStatus.FAILED);
		assertNotEquals(task1.getStatus(), TaskStatus.FINISHED);
	}

	@Test
	public void testTaskTwoOfProjectyIsMade(){
		Project projecty = projectController.getAllProjects().get(1);
		Task task2 = projecty.getAllTasks().get(1);

		assertEquals(task2.getDescription(), "yet another task description");
		assertEquals(task2.getEstimatedDuration(), Duration.ofHours(100));
		assertEquals(task2.getAcceptableDeviation(), 10, 0.001);
		assertEquals(task2.getDependencies().size(), 0);

		assertNull(task2.getAlternativeFor());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(task2.getStatus(), TaskStatus.FAILED);
		assertNotEquals(task2.getStatus(), TaskStatus.FINISHED);
	}

	@Test
	public void testTaskThreeOfProjectyIsMade(){
		Project projecty = projectController.getAllProjects().get(1);
		Task task3 = projecty.getAllTasks().get(2);
		
		assertEquals(task3.getDescription(), "description");
		assertEquals(task3.getEstimatedDuration(), Duration.ofHours(50));
		assertEquals(task3.getAcceptableDeviation(), 0, 0.001);
		assertNull(task3.getAlternativeFor());

		assertEquals(task3.getDependencies().size(), 2);
		assertEquals(task3.getStatus(),TaskStatus.FAILED);
		assertEquals(task3.getStartTime(), LocalDateTime.parse(("2015-01-01 09:00"),dateTimeFormatter));
		assertEquals(task3.getEndTime(), LocalDateTime.parse(("2015-01-30 18:00"),dateTimeFormatter));
	}

	@Test
	public void testTaskFourOfProjectyIsMade(){
		Project projecty = projectController.getAllProjects().get(1);
		Task task4 = projecty.getAllTasks().get(3);

		assertEquals(task4.getDescription(), "description");
		assertEquals(task4.getEstimatedDuration(), Duration.ofHours(50));
		assertEquals(task4.getAcceptableDeviation(), 0, 0.001);
		assertEquals(task4.getDependencies().size(), 2);
		assertEquals(task4.getAlternativeFor(), projectController.getAllProjects().get(1).getAllTasks().get(2));
	
		assertNull(task4.getStartTime());
		assertNull(task4.getEndTime());
		assertNotEquals(task4.getStatus(), TaskStatus.FAILED);
		assertNotEquals(task4.getStatus(), TaskStatus.FINISHED);
	}

	@Test
	public void testTwoTasksOfProjectzAreMade(){
		Project projectz = projectController.getAllProjects().get(2);
		assertEquals(projectz.getAllTasks().size(),2);
	}

	@Test
	public void testTaskOneOfProjectzIsMade(){
		Project projectz = projectController.getAllProjects().get(2);
		Task task1 = projectz.getAllTasks().get(0);

		assertEquals(task1.getDescription(), "description");
		assertEquals(task1.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task1.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task1.getDependencies().size(), 0);

		assertNull(task1.getAlternativeFor());
		assertNull(task1.getStartTime());
		assertNull(task1.getEndTime());
		assertNotEquals(task1.getStatus(), TaskStatus.FAILED);
		assertNotEquals(task1.getStatus(), TaskStatus.FINISHED);
	}

	@Test
	public void testTaskTwoOfProjectzIsMade(){
		Project projectz = projectController.getAllProjects().get(2);
		Task task2 = projectz.getAllTasks().get(1);

		assertEquals(task2.getDescription(), "description");
		assertEquals(task2.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task2.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task2.getDependencies().size(), 0);
		assertNull(task2.getAlternativeFor());

		assertNull(task2.getAlternativeFor());
		assertNull(task2.getStartTime());
		assertNull(task2.getEndTime());
		assertNotEquals(task2.getStatus(), TaskStatus.FAILED);
		assertNotEquals(task2.getStatus(), TaskStatus.FINISHED);
	}
}
