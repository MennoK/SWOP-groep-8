package UseCaseTests;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.ProjectStatus;
import TaskManager.Task;
import TaskManager.TaskManClock;
import TaskManager.TaskStatus;

public class UseCase1ShowProjectsTester {
	

	private ProjectController controller;
	//1 task
	private Project project1;
	//2 task
	private Project project2;
	// no task
	private Project project0;
	private Task task1;
	private Task task2;
	private Task task3;
	
	private List<Project> allProjectsExpected;
	@Before
	public void setUp(){
		TaskManClock clock = new TaskManClock(LocalDateTime.of(2015, 03, 07,01,00));
		controller = new ProjectController(clock);
		project1 = new Project("Project 1", "Description 1", LocalDateTime.of(2015, 03, 03,00,00), LocalDateTime.of(2015, 03, 10,00,00));
		project2 = new Project("Project 2", "Description 2", LocalDateTime.of(2015, 03, 03,00,00), LocalDateTime.of(2015, 03, 11,00,00));
		project0 = new Project("Project 0", "Description 3", LocalDateTime.of(2015, 03, 03,00,00), LocalDateTime.of(2015, 03, 12,00,00));
		task1 = new Task("Task 1", Duration.ofHours(2), 0.4);
		task2 = new Task("Task 2", Duration.ofHours(2), 0.4);
		task3 = new Task("Task 3", Duration.ofHours(3), 0.4);
		
		
		
		
		project1.addTask(task1);
		project2.addTask(task2);
		project2.addTask(task3); 
		
		controller.addProject(project0);
		controller.addProject(project1);
		controller.addProject(project2);

	}
	
	
	@Test
	public void testShowProject() {
		//
		List <Project> allProjectsActuals = new ArrayList<Project>();
		allProjectsActuals = controller.getAllProjects();
		assertEquals(project0, allProjectsActuals.get(0));
		assertEquals(project1, allProjectsActuals.get(1));
		assertEquals(project2, allProjectsActuals.get(2));
	}

	//TODO: 
	@Test
	public void testGetEstimatedFinishTime() {
		
	}
	@Test
	public void testGetTotalDelayLargerThenZero() {
		//Delay task 1 = 1h
		task1.setStartTime(LocalDateTime.of(2015, 03, 03,08,00));
		task1.setEndTime(LocalDateTime.of(2015, 03, 03,11,00));
		assertEquals(1, project1.getTotalDelay());
		

		//delay task 2 & 3 > 0
		task2.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task2.setEndTime(LocalDateTime.of(2015, 03, 03,13,00));

		task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task3.setEndTime(LocalDateTime.of(2015, 03, 03,16,00));
		
		assertEquals(4, project1.getTotalDelay());
		
		//delay taks 2 = 0 & task 3 > 0
		task2.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task2.setEndTime(LocalDateTime.of(2015, 03, 03,12,00));

		task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task3.setEndTime(LocalDateTime.of(2015, 03, 03,16,00));
		
		assertEquals(3, project1.getTotalDelay());

	}
	@Test
	public void testGetTotalDelayEqualsZero() {
		//Delay task 1 = 0h
		task1.setStartTime(LocalDateTime.of(2015, 03, 03,8,00));
		task1.setEndTime(LocalDateTime.of(2015, 03, 03,10,00));
		assertEquals(0, project1.getTotalDelay());
		
		//delay task 2 & 3 = 0
		task2.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task2.setEndTime(LocalDateTime.of(2015, 03, 03,12,00));
		task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task3.setEndTime(LocalDateTime.of(2015, 03, 03,13,00));
				
		assertEquals(0, project1.getTotalDelay());
				
				
	}

	
	@Test
	public void testGetTotalDelaySmallerThenZero() {
		//Delay task 1 = -1h
		task1.setStartTime(LocalDateTime.of(2015, 03, 03,08,00));
		task1.setEndTime(LocalDateTime.of(2015, 03, 03,9,00));
		assertEquals(-1, project1.getTotalDelay());
		
		//delay task 2 = 1h & 3 = -2h
		task2.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task2.setEndTime(LocalDateTime.of(2015, 03, 03,13,00));
		task3.setStartTime(LocalDateTime.of(2015, 03, 03,10,00));
		task3.setEndTime(LocalDateTime.of(2015, 03, 03,11,00));
				
		assertEquals(-1, project1.getTotalDelay());
				
				
	}
	
	
	@Test
	public void testGetAllTasks() {
		assertEquals(0, project0.getAllTasks().size());
		assertEquals(1, project1.getAllTasks().size());
		assertEquals(task1, project1.getAllTasks().get(0));
		assertEquals(2, project2.getAllTasks().size());

		assertEquals(task2, project2.getAllTasks().get(0));
		assertEquals(task3, project2.getAllTasks().get(1));
	}
	
	
	@Test
	public void testGetTaskStatusAllAvailable() {
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
	}
	@Test
	public void testGetTaskStatusUnavailable() throws LoopingDependencyException {
		task3.addDependency(task2);
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
	}
	@Test
	public void testGetTaskStatusFinished() {
		task1.updateStatus(TaskStatus.FINISHED);
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
	}
}
