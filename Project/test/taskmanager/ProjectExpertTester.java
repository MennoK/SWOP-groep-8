package taskmanager;

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Project;

public class ProjectExpertTester extends TaskManTester {

	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void testCreateProjects() {
		createStandardProject(time.plusDays(1));

		assertEquals(1, tmc.getAllProjects().size());
		assertEquals(time, tmc.getTime());

		Project project2 = createStandardProject(time.plusDays(1));

		assertEquals(2, tmc.getAllProjects().size());
		assertEquals(time, project2.getCreationTime());
	}

	@Test
	public void testAdvanceTime() {
		Project project = createStandardProject(time.plusDays(1));
		createTask(project, Duration.ofHours(20));

		tmc.advanceTime(time.plusDays(1));

		assertEquals(time.plusDays(1), tmc.getTime());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAdvanceTimeWithInvalidTime() {
		tmc.advanceTime(time.minusMinutes(1));
	}

}
