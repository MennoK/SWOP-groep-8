package useCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.*;

public class UseCase1ShowProjectsTester extends UseCaseTestBasis {

	private Developer jos;

	private Project project1;
	private Project project2;
	private Project project0;
	private Project project3;

	private Task task1;
	private Task task2;
	private Task task3;

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 03, 10, 11, 00));

		// create a controller, 3 projects and 3 tasks:
		// project0 has 0 tasks
		// project1 has 1 task (finished)
		// project2 has 2 tasks (1 task is dependent on the other)

		jos = tmc.createDeveloper("Jos");

		project0 = tmc.createProject("Project 0", "Desc 0", now.plusDays(2));
		project1 = tmc.createProject("Project 1", "Desc 1", now.plusDays(2));
		project2 = tmc.createProject("Project 2", "Desc 2", now.plusHours(3));
		project3 = tmc.createProject("Project 3", "Desc 3", now.plusHours(5));

		task1 = Task.builder("Task 1", Duration.ofHours(5), 0.4)
				.build(project1);
		tmc.getPlanner().createPlanning(now, task1, activeUser).build();
		tmc.setExecuting(task1, now);
		tmc.setFinished(task1, now.plusDays(1));
		task2 = Task.builder("Task 2", Duration.ofHours(2), 0.4)
				.build(project2);
		tmc.getPlanner().createPlanning(now.plusHours(5), task2, jos).build();
		task3 = Task.builder("Task 3", Duration.ofHours(3), 0.4)
				.addDependencies(task2).build(project2);
		Task.builder("task4", Duration.ofHours(2), 0.4).build(project3);
	}

	@Test
	public void showProjects() {

		// List all projects
		assertEquals(4, tmc.getAllProjects().size());
		assertTrue(tmc.getAllProjects().contains(project0));
		assertTrue(tmc.getAllProjects().contains(project1));
		assertTrue(tmc.getAllProjects().contains(project2));
		assertTrue(tmc.getAllProjects().contains(project3));

		// show projects name description, creation time and due time
		assertEquals("Project 0", project0.getName());
		assertEquals("Desc 0", project0.getDescription());
		assertEquals(now, project0.getCreationTime());
		assertEquals(now.plusDays(2), project0.getDueTime());

		assertEquals("Project 1", project1.getName());
		assertEquals("Desc 1", project1.getDescription());
		assertEquals(now, project1.getCreationTime());
		assertEquals(now.plusDays(2), project1.getDueTime());

		assertEquals("Project 2", project2.getName());
		assertEquals("Desc 2", project2.getDescription());
		assertEquals(now, project2.getCreationTime());
		assertEquals(now.plusHours(3), project2.getDueTime());

		assertEquals("Project 3", project3.getName());
		assertEquals("Desc 3", project3.getDescription());
		assertEquals(now, project3.getCreationTime());
		assertEquals(now.plusHours(5), project3.getDueTime());

		// show details of the projects: over_time/on_time and hours short
		// project 1 is finished -> ON_TIME
		// project 2 is ongoing and should be OVER_TIME should be 5 hours delay
		// (2 dependent tasks)

		assertEquals(ProjectFinishingStatus.OVER_TIME,
				project2.finishedOnTime());
		assertEquals(Duration.ofHours(3), project2.getCurrentDelay());
		assertEquals(ProjectFinishingStatus.ON_TIME, project1.finishedOnTime());
		assertEquals(ProjectFinishingStatus.ON_TIME, project3.finishedOnTime());

		// project 3 has 2 dependent tasks -> should still finish on time
		assertEquals(ProjectFinishingStatus.ON_TIME, project3.finishedOnTime());
		// Show their status: project with zero tasks in ongoing
		assertEquals(ProjectStatus.ONGOING, project0.getStatus());
		assertEquals(ProjectStatus.FINISHED, project1.getStatus());
		assertEquals(ProjectStatus.ONGOING, project2.getStatus());

		// show tasks of each project
		assertEquals(0, project0.getAllTasks().size());
		assertEquals(1, project1.getAllTasks().size());
		assertTrue(project1.getAllTasks().contains(task1));
		assertEquals(2, project2.getAllTasks().size());
		assertTrue(project2.getAllTasks().contains(task2));
		assertTrue(project2.getAllTasks().contains(task3));

		// show tasks details: description, duration, deviation, alternativefor
		// and dependency list
		assertEquals("Task 1", task1.getDescription());
		assertEquals(Duration.ofHours(5), task1.getEstimatedDuration());
		assertEquals(0.4, task1.getAcceptableDeviation(), 0.001);

		assertEquals("Task 2", task2.getDescription());
		assertEquals(Duration.ofHours(2), task2.getEstimatedDuration());
		assertEquals(0.4, task2.getAcceptableDeviation(), 0.001);

		assertEquals("Task 3", task3.getDescription());
		assertEquals(Duration.ofHours(3), task3.getEstimatedDuration());
		assertEquals(0.4, task3.getAcceptableDeviation(), 0.001);
		assertEquals(1, task3.getDependencies().size());

		// show task status
		assertEquals(TaskStatus.FINISHED, task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task3.getStatus());
	}

}
