package useCase;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Project;

public class UseCase2CreateProjectTester extends UseCaseTestBasis {

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 03, 07, 01, 00));
	}

	@Test
	public void createProject() {
		// create first project
		Project project0 = tmc.createProject("Project 1", "Description 1", now
				.minusDays(6).minusHours(1), now.plusDays(3).minusHours(1));

		// check if the project is correctly made
		assertEquals(1, tmc.getAllProjectsActiveOffice().size());
		assertEquals(tmc.getAllProjectsActiveOffice().size(), 1);
		assertEquals("Project 1", project0.getName());
		assertEquals("Description 1", project0.getDescription());
		assertEquals(now.minusDays(6).minusHours(1), project0.getCreationTime());
		assertEquals(now.plusDays(3).minusHours(1), project0.getDueTime());

		// create second project
		Project project1 = tmc.createProject("name2", "descr2",
				now.plusHours(4));

		// check if both are projects are made
		assertEquals(tmc.getAllProjectsActiveOffice().size(), 2);
		assertEquals("Project 1", project0.getName());
		assertEquals("Description 1", project0.getDescription());
		assertEquals(now.minusDays(6).minusHours(1), project0.getCreationTime());
		assertEquals(now.plusDays(3).minusHours(1), project0.getDueTime());

		assertEquals("name2", project1.getName());
		assertEquals("descr2", project1.getDescription());
		assertEquals(now, project1.getCreationTime());
		assertEquals(now.plusHours(4), project1.getDueTime());
	}
}
