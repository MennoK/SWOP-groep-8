package useCase;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import taskManager.Developer;
import taskManager.Planning;
import taskManager.Project;
import taskManager.ProjectFinishingStatus;
import taskManager.ProjectStatus;
import taskManager.Task;
import taskManager.TaskManController;
import taskManager.TaskStatus;

public class UseCase1ShowProjectsTester {

	private TaskManController tmc;
	private Developer jef;
	private Developer jos;
	private Project project1;
	private Project project2;
	private Project project0;
	private Project project3;

	private Task task1;
	private Task task2;
	private Task task3;
	private Task task4;

	private LocalDateTime now;

	@Before
	public void setUp() {

		// create a controller, 3 projects and 3 tasks:
		// project0 has 0 tasks
		// project1 has 1 task (finished)
		// project2 has 2 tasks (1 task is dependent on the other)

		now = LocalDateTime.of(2015, 03, 10, 11, 00);

		tmc = new TaskManController(now);

		jef = tmc.getDeveloperExpert().createDeveloper("Jef");
		jos = tmc.getDeveloperExpert().createDeveloper("Jos");

		tmc.getProjectExpert().createProject("Project 1", "Desc 1",
				now.plusDays(2));
		tmc.getProjectExpert().createProject("Project 2", "Desc 2",
				now.plusHours(3));
		tmc.getProjectExpert().createProject("Project 0", "Desc 3",
				now.plusDays(2));
		tmc.getProjectExpert().createProject("Project 3", "Desc 3",
				now.plusHours(5));

		project0 = tmc.getAllProjects().get(2);
		project1 = tmc.getAllProjects().get(0);
		project2 = tmc.getAllProjects().get(1);
		project3 = tmc.getAllProjects().get(3);

		task1 = Task.builder("Task 1", Duration.ofHours(5), 0.4)
				.build(project1);
		Planning.builder(now, task1, jef, tmc.getPlanner()).build();
		tmc.setExecuting(task1, now);
		tmc.setFinished(task1, now.plusDays(1));
		task2 = Task.builder("Task 2", Duration.ofHours(2), 0.4)
				.build(project2);
		Planning.builder(now.plusHours(5), task2, jos, tmc.getPlanner())
				.build();
		task3 = Task.builder("Task 3", Duration.ofHours(3), 0.4)
				.addDependencies(project2.getAllTasks().get(0)).build(project2);
		task4 = Task.builder("task4", Duration.ofHours(2), 0.4).build(project3);
	}

	@Test
	public void showProjects() {

		// List all projects
		List<Project> allProjectsActuals = new ArrayList<Project>();
		allProjectsActuals = tmc.getAllProjects();
		assertEquals(project1, allProjectsActuals.get(0));
		assertEquals(project2, allProjectsActuals.get(1));
		assertEquals(project0, allProjectsActuals.get(2));

		// show projects name description, creation time and due time
		assertEquals("Project 1", project1.getName());
		assertEquals("Desc 1", project1.getDescription());
		assertEquals(now, project1.getCreationTime());
		assertEquals(now.plusDays(2), project1.getDueTime());

		assertEquals("Project 2", project2.getName());
		assertEquals("Desc 2", project2.getDescription());
		assertEquals(now, project2.getCreationTime());
		assertEquals(now.plusHours(3), project2.getDueTime());

		assertEquals("Project 0", project0.getName());
		assertEquals("Desc 3", project0.getDescription());
		assertEquals(now, project0.getCreationTime());
		assertEquals(now.plusDays(2), project0.getDueTime());

		// show details of the projects: over_time/on_time and hours short
		// project 1 is finished -> ON_TIME
		// project 2 is ongoing and should be OVER_TIME should be 5 hours delay
		// (2 dependent tasks)

		assertEquals(ProjectFinishingStatus.OVER_TIME,
				project2.finishedOnTime());
		assertEquals(Duration.ofHours(3), project2.getCurrentDelay());
		assertEquals(ProjectFinishingStatus.ON_TIME, project1.finishedOnTime());
		assertEquals(ProjectFinishingStatus.ON_TIME, project3.finishedOnTime());

		Task.builder("task5", Duration.ofHours(1), 0.4).addDependencies(task4)
				.build(project3);

		// project 3 has 2 dependent tasks -> should still finish on time
		assertEquals(ProjectFinishingStatus.ON_TIME, project3.finishedOnTime());
		// Show their status: project with zero tasks in ongoing
		assertEquals(ProjectStatus.ONGOING, project0.getStatus());
		assertEquals(ProjectStatus.FINISHED, project1.getStatus());
		assertEquals(ProjectStatus.ONGOING, project2.getStatus());

		// show tasks of each project
		assertEquals(0, project0.getAllTasks().size());
		assertEquals(1, project1.getAllTasks().size());
		assertEquals(task1, project1.getAllTasks().get(0));
		assertEquals(2, project2.getAllTasks().size());

		assertEquals(task2, project2.getAllTasks().get(0));
		assertEquals(task3, project2.getAllTasks().get(1));

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
