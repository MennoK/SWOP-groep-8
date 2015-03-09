package UseCaseTests;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.TaskManClock;

public class UseCase3CreateTaskTester {
	private ProjectController controller;
	private Project project1;
	@Before
	public void setUp(){
		TaskManClock clock = new TaskManClock(LocalDateTime.of(2015, 03, 07,01,00));
		controller = new ProjectController(clock);
		project1 = new Project("Project 1", "Description 1", LocalDateTime.of(2015, 03, 01, 00,00), LocalDateTime.of(2015, 03, 10,00,00));
		controller.addProject(project1);
		
	}
	@Test
	public void testCreateTask() {
		assertEquals(project1, controller.getAllProjects().get(0));
		//User has received filled in the create task form and has chosen to add the task to project 1
		project1.createTask("task1",  Duration.ofHours(8), 0.4);
		assertEquals(1, controller.getAllProjects().get(0).getAllTasks().size());
		assertEquals("task1", controller.getAllProjects().get(0).getAllTasks().get(0).getDescription());
		//create a second task
		project1.createTask("task2",  Duration.ofHours(8), 0.4);
		assertEquals(2, controller.getAllProjects().get(0).getAllTasks().size());
		assertEquals("task1", controller.getAllProjects().get(0).getAllTasks().get(0).getDescription());
		assertEquals("task2", controller.getAllProjects().get(0).getAllTasks().get(1).getDescription());
	}
	//TODO
	@Test
	public void testCreateTaskWithDependency() {
		project1.createTask("task1",  Duration.ofHours(8), 0.4);
		project1.createTask("task dependent on task1",Duration.ofHours(8), 0.4);
		
	}
	
}
