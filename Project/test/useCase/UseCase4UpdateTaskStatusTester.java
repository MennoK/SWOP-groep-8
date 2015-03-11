package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import parser.TaskStatus;

import taskManager.Project;
import taskManager.ProjectController;
import taskManager.Task;
import taskManager.TaskManClock;
import taskManager.exception.LoopingDependencyException;

public class UseCase4UpdateTaskStatusTester {
	private ProjectController controller;
	private Project project1;
	private Task task1;
	private Task task2;
	private Task task3;
	@Before
	public void setUp() throws LoopingDependencyException{
		TaskManClock clock = new TaskManClock(LocalDateTime.of(2015, 03, 07,01,00));
		controller = new ProjectController(clock);
		project1 = new Project("Project 1", "Description 1", LocalDateTime.of(2015, 03, 01, 00 ,00), LocalDateTime.of(2015, 03, 10,00,00));
		controller.addProject(project1);
		task1 = new Task("Task 1", Duration.ofHours(8), 0.4);
		task2 = new Task("Task 2", Duration.ofHours(8), 0.4);
		ArrayList<Task> dependency = new ArrayList<>();
		dependency.add(task2);
		task3 = new Task("Task 3", Duration.ofHours(8), 0.4, dependency);

		project1.addTask(task1);
		project1.addTask(task2);
		project1.addTask(task3); 
		
	}
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

	
}
