package useCase;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import taskManager.Project;
import taskManager.ProjectController;
import taskManager.TaskManClock;

public class UseCase2CreateProjectTester {

	private ProjectController controller;
	
	@Before
	public void setUp(){
		//create a project controller
		controller = new ProjectController(LocalDateTime.of(2015, 03, 07,01,00));;
	}
	
	@Test
	public void createProject(){
		//create first project
		controller.createProject("Project 1", "Description 1", LocalDateTime.of(2015, 03, 01,00,00), LocalDateTime.of(2015, 03, 10,00,00));
		
		//check if the project is correctly made
		assertEquals(1, controller.getAllProjects().size());
		assertEquals(controller.getAllProjects().size(),1);
		List<Project> projects = controller.getAllProjects();
		assertEquals("Project 1", projects.get(0).getName());
		assertEquals("Description 1", projects.get(0).getDescription());
		assertEquals( LocalDateTime.of(2015, 03, 01, 00,00), projects.get(0).getCreationTime());
		assertEquals( LocalDateTime.of(2015, 03, 10, 00,00), projects.get(0).getDueTime());
	
		//create second project
		controller.createProject("name2", "descr2", LocalDateTime.of(2015, 03, 07,05,00));

		//check if both are projects are made
		assertEquals(controller.getAllProjects().size(),2);
		projects = controller.getAllProjects();
		assertEquals("Project 1", projects.get(0).getName());
		assertEquals("Description 1", projects.get(0).getDescription());
		assertEquals( LocalDateTime.of(2015, 03, 01, 00,00), projects.get(0).getCreationTime());
		assertEquals( LocalDateTime.of(2015, 03, 10, 00,00), projects.get(0).getDueTime());
	
		assertEquals("name2", projects.get(1).getName());
		assertEquals("descr2", projects.get(1).getDescription());
		assertEquals( LocalDateTime.of(2015, 03, 07,01,00), projects.get(1).getCreationTime());
		assertEquals( LocalDateTime.of(2015, 03, 07,05,00), projects.get(1).getDueTime());
	}
}
