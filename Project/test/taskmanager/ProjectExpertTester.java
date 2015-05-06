package taskmanager;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Project;
import taskmanager.Task;
import taskmanager.TaskManController;

public class ProjectExpertTester {

	private TaskManController taskManController;

	@Before
	public void setUp() {
		taskManController = new TaskManController(LocalDateTime.of(2000, 03,
				05, 00, 00));
	}

	@Test
	public void testCreateProjects() {
		taskManController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(1, taskManController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), taskManController
				.getAllProjects().get(0).getLastUpdateTime());

		taskManController.createProject("name2", "description",
				LocalDateTime.of(2015, 03, 06, 00, 00));

		assertEquals(2, taskManController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), taskManController
				.getAllProjects().get(1).getCreationTime());
	}

	@Test
	public void testAdvanceTime() {
		Project project1 = taskManController.createProject("name",
				"description", LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));

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

}
