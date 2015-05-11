package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.ProjectStatus;
import taskmanager.Task;
import taskmanager.BranchOffice;
import taskmanager.TaskStatus;

public class UseCase8RunSimulation {

	private LocalDateTime time;
	private Project project;
	private Task baseTask;

	private BranchOffice tmc;

	@Before
	public void setUp() {
		time = LocalDateTime.of(2015, 03, 06, 8, 00);
		tmc = new BranchOffice(time);
		project = tmc.createProject("project", "desc", time.plusDays(4));

		// new developerExpert and create a new developer
		tmc.createDeveloper("Bob");
		ArrayList<Developer> devList = new ArrayList<Developer>();
		devList.addAll(tmc.getAllDevelopers());
	}

	private Task createStandardTask(Duration taskDuration) {
		Task task = Task.builder("desc", taskDuration, 0.5).build(project);
		Developer dev = tmc.createDeveloper("dev");
		Planning.builder(time, task, dev, tmc.getPlanner()).build();
		return task;
	}

	@Test
	public void testProjectMemento() {
		baseTask = createStandardTask(Duration.ofHours(8));

		// sanity
		assertEquals(1, project.getAllTasks().size());

		// save memento
		tmc.saveSystem();

		// finish Tasks
		Task baseTaskTwo = createStandardTask(Duration.ofHours(8));
		tmc.setExecuting(baseTask, time);
		tmc.setFinished(baseTask, time.plusHours(8));
		tmc.setExecuting(baseTaskTwo, time);
		tmc.setFinished(baseTaskTwo, time.plusHours(8));

		// sanity check
		assertEquals(TaskStatus.FINISHED, baseTask.getStatus());
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

		assertEquals(2, project.getAllTasks().size());

		// load memento
		tmc.loadSystem();

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

		tmc.saveSystem();

		tmc.createProject("name2", "description",
				LocalDateTime.of(2015, 05, 06, 00, 00));

		assertEquals(3, tmc.getAllProjects().size());
		assertEquals(LocalDateTime.of(2015, 03, 05, 00, 00), tmc
				.getAllProjects().get(1).getCreationTime());

		tmc.loadSystem();

		assertEquals(2, tmc.getAllProjects().size());
		assertEquals(LocalDateTime.of(2015, 03, 06, 8, 00), tmc
				.getAllProjects().get(0).getLastUpdateTime());

	}

	@Test
	public void mementoRollsBackTime() {
		LocalDateTime time = tmc.getTime();

		tmc.saveSystem();

		tmc.advanceTime(LocalDateTime.of(2020, 03, 06, 00, 00));

		tmc.loadSystem();

		assertEquals(time, tmc.getTime());
	}

	@Test
	public void testMementoSavesDevelopers() {
		tmc.saveSystem();
		tmc.createDeveloper("Bob");
		assertEquals(2, tmc.getAllDevelopers().size());
		tmc.loadSystem();
		assertEquals(1, tmc.getAllDevelopers().size());
	}

}
