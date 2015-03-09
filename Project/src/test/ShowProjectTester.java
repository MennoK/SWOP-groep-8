package test;

import static org.junit.Assert.*;

import TaskManager.TaskManClock;
import org.junit.Before;
import org.junit.Test;

import TaskManager.Project;
import TaskManager.ProjectController;

import java.time.LocalDateTime;

public class ShowProjectTester {

	private ProjectController emptyController;
	
	private ProjectController singleProjectController;
	private Project project11;
	
	private ProjectController dualProjectController;
	private Project project21;
	private Project project22;

	private TaskManClock clock;
	
	@Before
	public void setUp() {

		clock = new TaskManClock(LocalDateTime.now());
		emptyController = new ProjectController(clock);
		singleProjectController = new ProjectController(clock);
		project11 = new Project("p11","Test 11", LocalDateTime.now(), LocalDateTime.MAX );
		singleProjectController.addProject(project11);
		dualProjectController = new ProjectController(clock);
		project21 = new Project("p21","Test 21", LocalDateTime.now(), LocalDateTime.MAX );
		project22 = new Project("p22","Test 22", LocalDateTime.now(), LocalDateTime.MAX );
		dualProjectController.addProject(project21);
		dualProjectController.addProject(project22);
	}
	
	@Test
	public void testGetAllProjects() {
		assertEquals(emptyController.getAllProjects().size(),0);
		
		assertEquals(singleProjectController.getAllProjects().size(),1);
		assertTrue(singleProjectController.getAllProjects().contains(project11));
		
		assertEquals(dualProjectController.getAllProjects().size(),2);
		assertTrue(dualProjectController.getAllProjects().contains(project21));
		assertTrue(dualProjectController.getAllProjects().contains(project22));
	}
}
