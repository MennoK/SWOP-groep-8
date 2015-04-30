package useCase;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import taskManager.Project;
import taskManager.TaskManController;

public class UseCase2CreateProjectTester {

	private TaskManController tmc;

	@Before
	public void setUp() {
		// create a project controller
		tmc = new TaskManController(LocalDateTime.of(2015, 03, 07, 01, 00));

	}

	@Test
	public void createProject() {
		// create first project
		tmc.getProjectExpert().createProject("Project 1", "Description 1",
				LocalDateTime.of(2015, 03, 01, 00, 00),
				LocalDateTime.of(2015, 03, 10, 00, 00));

		// check if the project is correctly made
		assertEquals(1, tmc.getAllProjects().size());
		assertEquals(tmc.getAllProjects().size(), 1);
		List<Project> projects = tmc.getAllProjects();
		assertEquals("Project 1", projects.get(0).getName());
		assertEquals("Description 1", projects.get(0).getDescription());
		assertEquals(LocalDateTime.of(2015, 03, 01, 00, 00), projects.get(0)
				.getCreationTime());
		assertEquals(LocalDateTime.of(2015, 03, 10, 00, 00), projects.get(0)
				.getDueTime());

		// create second project
		tmc.createProject("name2", "descr2",
				LocalDateTime.of(2015, 03, 07, 05, 00));

		// check if both are projects are made
		assertEquals(tmc.getAllProjects().size(), 2);
		projects = tmc.getAllProjects();
		assertEquals("Project 1", projects.get(0).getName());
		assertEquals("Description 1", projects.get(0).getDescription());
		assertEquals(LocalDateTime.of(2015, 03, 01, 00, 00), projects.get(0)
				.getCreationTime());
		assertEquals(LocalDateTime.of(2015, 03, 10, 00, 00), projects.get(0)
				.getDueTime());

		assertEquals("name2", projects.get(1).getName());
		assertEquals("descr2", projects.get(1).getDescription());
		assertEquals(LocalDateTime.of(2015, 03, 07, 01, 00), projects.get(1)
				.getCreationTime());
		assertEquals(LocalDateTime.of(2015, 03, 07, 05, 00), projects.get(1)
				.getDueTime());
	}
}
