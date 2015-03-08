package Test;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import TaskManager.TaskManClock;
import TaskManager.Project;
import TaskManager.ProjectController;

public class ProjectControllerTester {

	private ProjectController projectController;

	@Before
	public void setUp() {
		TaskManClock taskManClock = new TaskManClock(LocalDateTime.now());
		projectController = new ProjectController(taskManClock);
	}

	@Test
	public void testCreateProject(){
		projectController.createProject("name", "description", LocalDate.of(2015, 03, 05), LocalDate.of(2015, 03, 06));
		assertEquals(projectController.getAllProjects().size(), 1);
	}

	@Test
	public void testAddProjectValidProjects(){
		Project project1 = new Project("name1", "descr",  LocalDate.of(2015, 03, 05), LocalDate.of(2015, 03, 06));
		Project project2 = new Project("name2", "descr",  LocalDate.of(2015, 03, 06), LocalDate.of(2015, 03, 07));
		projectController.addProject(project1);
		projectController.addProject(project2);

		ArrayList<Project> projects = new ArrayList<Project>();
		projects.add(project1);
		projects.add(project2);
		assertEquals(projectController.getAllProjects().size(),2);
		assertEquals(projectController.getAllProjects(),projects);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddProjectThatIsAlreadyInList(){
		Project project1 = new Project("name1", "descr",  LocalDate.of(2015, 03, 05), LocalDate.of(2015, 03, 06));
		projectController.addProject(project1);
		projectController.addProject(project1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddProjectNullProject(){
		Project project = null;
		projectController.addProject(project);
	}


}
