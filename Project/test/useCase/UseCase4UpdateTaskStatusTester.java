package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


import taskManager.*;
import taskManager.exception.InvalidTimeException;
import taskManager.exception.LoopingDependencyException;

public class UseCase4UpdateTaskStatusTester {
	
	private ProjectController controller;
	private Project project1;
	private Task task1;
	private Task task2;
	private Task task3;
	
	private LocalDateTime now;
	
	@Before
	public void setUp() throws LoopingDependencyException{
		//create a contoller and a project with 3 tasks
		//task 3 is dependent on task 1
		
		now = LocalDateTime.of(2015, 03, 07,01,00);
		controller = new ProjectController(now);
		controller.createProject("Project 1", "Description 1", LocalDateTime.of(2015, 03, 01, 00 ,00), LocalDateTime.of(2015, 03, 10,00,00));
		
		project1 = controller.getAllProjects().get(0);
		
		project1.createTask("Task 1", Duration.ofHours(8), 0.4, now);
		project1.createTask("Task 2", Duration.ofHours(8), 0.4, now);
		task1 = project1.getAllTasks().get(0);
		task2 = project1.getAllTasks().get(1);
		
		ArrayList<Task> dependency = new ArrayList<>();
		dependency.add(task2);
		//task 3 has dependency on task2
		project1.createTask("Task 3", Duration.ofHours(8), 0.4, dependency);
		task3 = project1.getAllTasks().get(2);
			
	}
	
	@Test
	public void updateTaskStatusSuccess() throws InvalidTimeException{
		//initial status
		assertEquals(TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		
		//task 1 failed
		task1.updateStatus(LocalDateTime.of(2015, 03, 02, 00 ,00), LocalDateTime.of(2015, 03, 02, 11 ,00), true);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		
		task1.updateStatus(LocalDateTime.of(2015, 03, 02, 00 ,00), LocalDateTime.of(2015, 03, 02, 11 ,00), false);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
		
		task2.updateStatus(LocalDateTime.of(2015, 03, 02, 00 ,00), LocalDateTime.of(2015, 03, 02, 11 ,00), false);
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
		
	}
	
	@Test(expected=IllegalStateException.class)
	public void updateTaskStatusExceptionExpected() throws InvalidTimeException{
		task2.updateStatus(LocalDateTime.of(2015, 03, 02, 00 ,00), LocalDateTime.of(2015, 03, 02, 11 ,00), false);
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
		
		task2.updateStatus(LocalDateTime.of(2015, 03, 02, 00 ,00), LocalDateTime.of(2015, 03, 02, 11 ,00), true);
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
	}
/*	
	@Test
	public void testUpdateTaskStatusFinishedtestNoDependencies() {
		//User has selected task1
		
		task1.setEndTime(LocalDateTime.of(2017, 03, 01, 00 ,00));
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
	}
	@Test
	public void testUpdateTaskStatusFinishedtestWithDependencies() {
		//User has selected task1
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
		task2.setEndTime(LocalDateTime.of(2017, 03, 01, 00 ,00));
		assertEquals(TaskStatus.FINISHED, task2.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task3.getStatus());
	}
	//TODO: implement
	@Test
	public void testUpdateTaskStatusFailedNoDependencies() {
		//User has selected task1
		
		task1.setFailed(true);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
	}
	//TODO: implement
	@Test
	public void testUpdateTaskStatusFailedWithDependencies() {
		//User has selected task1
		
		task1.setFailed(true);
		assertEquals(TaskStatus.FAILED, task1.getStatus());
	}
*/
	
}
