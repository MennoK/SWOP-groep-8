package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Project;
import taskmanager.ProjectStatus;
import taskmanager.Task;
import taskmanager.TaskStatus;

public class UseCase8RunSimulation extends UseCaseTestBasis {

	private Project project;
	private Task baseTask;

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 03, 06, 8, 00));
		project = tmc.createProject("project", "desc", now.plusDays(4));
	}

	private Task createStandardTask(Duration taskDuration) {
		Task task = Task.builder("desc", taskDuration, 0.5).build(project);
		Developer dev = tmc.createDeveloper("dev");
		tmc.getPlanner().createPlanning(now, task, dev).build();
		return task;
	}

	@Test
	public void testProjectMemento() {
		baseTask = createStandardTask(Duration.ofHours(8));

		// sanity
		assertEquals(1, project.getAllTasks().size());

		// save memento
		tmc.getActiveOffice().saveSystem();

		// finish Tasks
		Task baseTaskTwo = createStandardTask(Duration.ofHours(8));
		tmc.setExecuting(baseTask, now);
		tmc.setFinished(baseTask, now.plusHours(8));
		tmc.setExecuting(baseTaskTwo, now);
		tmc.setFinished(baseTaskTwo, now.plusHours(8));

		// sanity check
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

		assertEquals(2, project.getAllTasks().size());

		// load memento
		tmc.getActiveOffice().loadSystem();

		// statuses are different
		assertEquals(TaskStatus.AVAILABLE, baseTask.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
		assertEquals(1, project.getAllTasks().size());
	}

	@Test
	public void mementoCanRemoveProjects() {
		tmc.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(2, tmc.getAllProjects().size());
		assertEquals(LocalDateTime.of(2015, 03, 06, 8, 00), tmc
				.getAllProjects().get(0).getLastUpdateTime());

		tmc.getActiveOffice().saveSystem();

		tmc.createProject("name2", "description",
				LocalDateTime.of(2015, 05, 06, 00, 00));

		assertEquals(3, tmc.getAllProjects().size());
		assertEquals(LocalDateTime.of(2015, 03, 05, 00, 00), tmc
				.getAllProjects().get(1).getCreationTime());

		tmc.getActiveOffice().loadSystem();

		assertEquals(2, tmc.getAllProjects().size());
		assertEquals(LocalDateTime.of(2015, 03, 06, 8, 00), tmc
				.getAllProjects().get(0).getLastUpdateTime());

	}

	@Test
	public void mementoRollsBackTime() {
		LocalDateTime time = tmc.getTime();

		tmc.getActiveOffice().saveSystem();

		tmc.advanceTime(LocalDateTime.of(2020, 03, 06, 00, 00));

		tmc.getActiveOffice().loadSystem();

		assertEquals(time, tmc.getTime());
	}

	@Test
	public void testMementoSavesDevelopers() {
		tmc.getActiveOffice().saveSystem();
		tmc.createDeveloper("Bob");
		assertEquals(2, tmc.getAllDevelopers().size());
		tmc.getActiveOffice().loadSystem();
		assertEquals(1, tmc.getAllDevelopers().size());
	}

}
