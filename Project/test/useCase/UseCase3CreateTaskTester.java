package useCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Project;
import taskmanager.ResourceType;
import taskmanager.Task;

public class UseCase3CreateTaskTester extends UseCaseTestBasis {

	private ResourceType resType;
	private Project project;

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 03, 07, 01, 00));

		// set up a project
		project = tmc.createProject("Project 1", "Description 1", now
				.minusDays(6).minusHours(1), now.plusDays(3).minusHours(1));

		// set up some available resources
		resType = ResourceType.builder("resourcetype").build(
				tmc.getActiveOffice());
		resType.createResource("res1");
	}

	@Test
	public void createTask() {
		// create a simple task
		Task task1 = Task.builder("simple descr", Duration.ofHours(20), 50)
				.build(project);
		assertEquals(1, project.getAllTasks().size());
		assertEquals("simple descr", task1.getDescription());
		assertEquals(Duration.ofHours(20), task1.getEstimatedDuration());
		assertEquals(50, task1.getAcceptableDeviation(), 0.001);

		// create a new task dependent on the simple task
		Task task2 = Task
				.builder("task with dependency", Duration.ofHours(20), 50)
				.addDependencies(task1).build(project);

		assertEquals(2, project.getAllTasks().size());
		assertTrue(project.getAllTasks().contains(task2));
		assertTrue(task2.getDependencies().contains(task1));

		// create an alternative task for a failed task
		// first we let the simple task fail
		Developer dev = tmc.createDeveloper("dev");
		tmc.getPlanner().createPlanning(now, task1, dev).build();
		tmc.setExecuting(task1, now.plusHours(1));
		tmc.setFailed(task1, now.plusHours(4));

		// we create an alternative task for the simple task
		Task task3 = Task.builder("alternative task", Duration.ofHours(20), 50)
				.setOriginalTask(task1).build(project);

		assertEquals(3, project.getAllTasks().size());
		assertTrue(project.getAllTasks().contains(task3));

		// task with dependency is now dependent on alternative task
		assertTrue(task2.getDependencies().contains(task3));

		// task with required resourceType
		Task task4 = Task.builder("desc", Duration.ofHours(2), 2)
				.addRequiredResourceType(resType, 1).build(project);
		assertEquals(1, task4.getRequiredResourceTypes().size());
		assertTrue(project.getAllTasks().contains(task4));
	}
}
