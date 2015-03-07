package Test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.Task;

import parser.Parser;

public class ParserTester {

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Test
	public void testAllObjectsAreMade(){

		ProjectController projectController = new ProjectController(null);
		try {
			new Parser().parse("./input.tman", projectController);
		} catch (FileNotFoundException | RuntimeException
				| LoopingDependencyException e) {
			e.printStackTrace();
		}

		//There are 3 projects
		assertEquals(projectController.getAllProjects().size(), 3);
		Project projectx = projectController.getAllProjects().get(0);
		Project projecty = projectController.getAllProjects().get(1);
		Project projectz = projectController.getAllProjects().get(2);


		//First project
		assertEquals(projectx.getName(), "project x");
		assertEquals(projectx.getDescription(), "a project description");
		assertEquals(projectx.getCreationTime(), LocalDateTime.parse(("2014-01-01 09:00"),dateTimeFormatter));
		assertEquals(projectx.getDueTime(),  LocalDateTime.parse(("2014-02-01 18:00"),dateTimeFormatter));

		//Second project
		assertEquals(projecty.getName(), "project y");
		assertEquals(projecty.getDescription(), "another project description");
		assertEquals(projecty.getCreationTime(), LocalDateTime.parse(("2015-01-01 09:00"),dateTimeFormatter));
		assertEquals(projecty.getDueTime(),  LocalDateTime.parse(("2016-01-01 18:00"),dateTimeFormatter));

		//Third project
		assertEquals(projectz.getName(), "project z");
		assertEquals(projectz.getDescription(), "yet another project description");
		assertEquals(projectz.getCreationTime(), LocalDateTime.parse(("2015-04-25 09:00"),dateTimeFormatter));
		assertEquals(projectz.getDueTime(),  LocalDateTime.parse(("2015-04-30 18:00"),dateTimeFormatter));


		//1 task of project x
		assertEquals(projectx.getAllTasks().size(),1);
		Task task1 = projectx.getAllTasks().get(0);
		assertEquals(task1.getDescription(), "task description");
		assertEquals(task1.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task1.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task1.getDependencies().size(), 0);

		//4 task of project y
		assertEquals(projecty.getAllTasks().size(),3);
		task1 = projecty.getAllTasks().get(0);
		Task task2 = projecty.getAllTasks().get(1);
		Task task3 = projecty.getAllTasks().get(2);
		//Task task4 = projecty.getAllTasks().get(3);

		assertEquals(task1.getDescription(), "another task description");
		assertEquals(task1.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task1.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task1.getDependencies().size(), 0);

		assertEquals(task2.getDescription(), "yet another task description");
		assertEquals(task2.getEstimatedDuration(), Duration.ofHours(100));
		assertEquals(task2.getAcceptableDeviation(), 10, 0.001);
		assertEquals(task2.getDependencies().size(), 0);

		assertEquals(task3.getDescription(), "description");
		assertEquals(task3.getEstimatedDuration(), Duration.ofHours(50));
		assertEquals(task3.getAcceptableDeviation(), 0, 0.001);
		assertEquals(task3.getDependencies().size(), 2);	
		
/*		assertEquals(task4.getDescription(), "description");
		assertEquals(task4.getEstimatedDuration(), Duration.ofHours(50));
		assertEquals(task4.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task4.getDependencies().size(), 2);*/
	
		//2 task of project z
		assertEquals(projectz.getAllTasks().size(),2);
		task1 = projectz.getAllTasks().get(0);
		task2 = projectz.getAllTasks().get(1);
		
		assertEquals(task1.getDescription(), "description");
		assertEquals(task1.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task1.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task1.getDependencies().size(), 0);

		assertEquals(task2.getDescription(), "description");
		assertEquals(task2.getEstimatedDuration(), Duration.ofHours(500));
		assertEquals(task2.getAcceptableDeviation(), 50, 0.001);
		assertEquals(task2.getDependencies().size(), 0);
		
		
	}
}
