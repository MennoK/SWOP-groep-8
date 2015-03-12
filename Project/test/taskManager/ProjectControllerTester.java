package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskManager.ProjectController;
import taskManager.exception.InvalidTimeException;

public class ProjectControllerTester {

	private ProjectController projectController;

	@Before
	public void setUp() {
		projectController = new ProjectController(LocalDateTime.of(2000, 03,
				05, 00, 00));
	}

	@Test
	public void testCreateProjects() {
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));
		
		assertEquals(1, projectController.getAllProjects().size());
		
		projectController.createProject("name2", "description",
				LocalDateTime.of(2015, 03, 06, 00, 00));
		
		assertEquals( 2,projectController.getAllProjects().size());
		assertEquals(LocalDateTime.of(2000, 03, 05, 00, 00), projectController.getAllProjects().get(1).getCreationTime());
	}
	
	@Test
	public void testCannotHaveNullProject(){
		assertFalse(projectController.canHaveProject(null));
	}
	
	@Test
	public void testCannotHaveSameProject(){
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));
		assertFalse(projectController.canHaveProject(projectController.getAllProjects().get(0)));
	}
	
	@Test
	public void testAdvanceTime() throws InvalidTimeException {
		projectController.createProject("name", "description",
				LocalDateTime.of(2015, 03, 05, 00, 00),
				LocalDateTime.of(2015, 03, 06, 00, 00));
		
		Project project1 = projectController.getAllProjects().get(0);
		project1.createTask("descr", Duration.ofHours(20), 20);
		
		projectController.advanceTime(LocalDateTime.of(2001, 03, 06, 00, 00));
		
		assertEquals(LocalDateTime.of(2001, 03, 06, 00, 00), projectController.getTime());
		assertEquals(LocalDateTime.of(2001, 03, 06, 00, 00), project1.getLastUpdateTime());
		assertEquals(LocalDateTime.of(2001, 03, 06, 00, 00), project1.getAllTasks().get(0).getLastUpdateTime());
		
	}

	@Test(expected = InvalidTimeException.class)
	public void testAdvanceTimeWithInvalidTime() throws InvalidTimeException {
		LocalDateTime newTime = LocalDateTime.of(1999, 03, 05, 00, 00);
		projectController.advanceTime(newTime);
	}
}
