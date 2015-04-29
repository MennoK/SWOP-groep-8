package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class ProjectExpertTester {

	private ProjectExpert projectController;
	private TaskManController taskManController;

	@Before
	public void setUp() {
		taskManController = new TaskManController(LocalDateTime.of(2000, 03, 05,
				00, 00));

		projectController = taskManController.getProjectExpert();
	}

	@Test
	public void testCreateProjects() {
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(1, projectController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), projectController
				.getAllProjects().get(0).getLastUpdateTime());

		projectController.createProject("name2", "description",
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(2, projectController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), projectController
				.getAllProjects().get(1).getCreationTime());
	}

	@Test
	public void testCannotHaveNullProject() {
		assertFalse(projectController.canHaveProject(null));
	}

	@Test
	public void testCannotHaveSameProject() {
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));
		assertFalse(projectController.canHaveProject(projectController
				.getAllProjects().get(0)));
	}

	@Test
	public void testAdvanceTime() {
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));

		Project project1 = projectController.getAllProjects().get(0);
		Task.builder("descr", Duration.ofHours(20), 20).build(project1);

		taskManController.advanceTime(LocalDateTime.of(2001, 03, 06, 00, 00));

		assertEquals(LocalDateTime.of(2001, 03, 06, 00, 00),
				taskManController.getTime());
		assertEquals(LocalDateTime.of(2001, 03, 06, 00, 00),
				project1.getLastUpdateTime());
		assertEquals(LocalDateTime.of(2001, 03, 06, 00, 00), project1
				.getAllTasks().get(0).getLastUpdateTime());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAdvanceTimeWithInvalidTime() {
		LocalDateTime newTime = LocalDateTime.of(1999, 03, 05, 00, 00);
		taskManController.advanceTime(newTime);
	}
	
	@Test
	public void mementoCanRemoveProjects() {
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(1, projectController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), projectController
				.getAllProjects().get(0).getLastUpdateTime());
		
		projectController.save();

		projectController.createProject("name2", "description",
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(2, projectController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), projectController
				.getAllProjects().get(1).getCreationTime());
		
		projectController.load();
		
		assertEquals(1, projectController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), projectController
				.getAllProjects().get(0).getLastUpdateTime());
		
	}
	
	@Test
	public void mementoRollsBackTime() {		
		LocalDateTime time = taskManController.getTime();
		
		taskManController.saveSystem();
		
		taskManController.advanceTime(LocalDateTime.of(2020, 03, 06, 00, 00));
		
		taskManController.loadSystem();
		
		assertEquals(time, taskManController.getTime());
	}
}
