package Test;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.PrimitiveIterator.OfDouble;

import org.junit.Before;
import org.junit.Test;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectStatus;
import TaskManager.Task;
import TaskManager.TaskStatus;

public class ProjectTester {

	private Project project;

	@Before
	public void setUp(){
		project = new Project("testname", "testdescription", LocalDateTime.of(2015, 03, 05,00,00), LocalDateTime.of(2015, 03, 06,00,00));
	}

	@Test
	public void testDueTimeSetterAfterCreationTime(){
		project.setDueTime(LocalDateTime.of(2015, 03, 07,00,00));
		assertEquals(project.getDueTime(),LocalDateTime.of(2015, 03, 07,00,00));
	}

	@Test
	public void testDueTimeSetterOnCreationTime(){
		project.setDueTime(LocalDateTime.of(2015, 03, 05,00,00));
		assertEquals(project.getDueTime(), LocalDateTime.of(2015, 03, 05,00,00));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDueTimeSetterBeforeCreationTime(){
		project.setDueTime(LocalDateTime.of(2015,03,04,00,00));		
	}

	@Test
	public void testCreateTask(){
		project.createTask("desc", Duration.ofHours(5), 20);
		assertEquals(project.getAllTasks().size(), 1);
	}

	@Test
	public void testAddTaskValidTasks(){
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		Task task2 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		project.addTask(task1);
		project.addTask(task2);

		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(task1);
		tasks.add(task2);
		assertEquals(project.getAllTasks().size(),2);
		assertEquals(project.getAllTasks(),tasks);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddTaskThatIsAlreadyInList(){
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		project.addTask(task1);
		project.addTask(task1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddTaskNullTask(){
		Task task1 = null;
		project.addTask(task1);

	}

	@Test
	public void testProjectStatusIsFinishedNoDependencies(){
		// 0 task
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);

		// 1 task
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		task1.setEndTime(LocalDateTime.now());
		project.addTask(task1);
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);

		// 2 tasks
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		task2.setEndTime(LocalDateTime.now());
		project.addTask(task2);
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);		
	}

	@Test
	public void testProjectStatusIsFinishedDependencies() throws LoopingDependencyException{
		// 1(finished) -> 2(finished)
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		task1.setEndTime(LocalDateTime.now());
		task2.setEndTime(LocalDateTime.now());
		task2.addDependency(task1);
		project.addTask(task1);
		project.addTask(task2);
		assertEquals(task1.getStatus(), TaskStatus.FINISHED);	
		assertEquals(task2.getStatus(), TaskStatus.FINISHED);	
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);	

		// 1(failed) -> 2(finished)
		task1.setFailed(true);
		assertEquals(task1.getStatus(), TaskStatus.FAILED);	
		assertEquals(task2.getStatus(), TaskStatus.FINISHED);	
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);	

		// 1(failed) -x-> 2(finished) <- 3(finished)
		Task task3 = new Task("testdescriptionTask3", Duration.ofHours(8), 50);
		task3.setEndTime(LocalDateTime.now());
		task2.addDependency(task3);
		assertEquals(task1.getStatus(), TaskStatus.FAILED);	
		assertEquals(task2.getStatus(), TaskStatus.FINISHED);	
		assertEquals(task3.getStatus(), TaskStatus.FINISHED);	
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);

		// 1(failed) 2(failed) 3(finished)
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);

		// [1(failed) 3(failed) 4(finished)]-> 2(finished)
		Task task4 = new Task("testdescriptionTask4", Duration.ofHours(8), 50);


	}

	@Test
	public void testProjectStatusIsOngoingNoDependencies(){
		// 1 task
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		project.addTask(task1);
		assertEquals(task1.getStatus(), TaskStatus.AVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);

		// 2 tasks
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		project.addTask(task2);
		assertEquals(task2.getStatus(), TaskStatus.AVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);
	}

	@Test
	public void testProjectStatusIsOngoingDependencies() throws LoopingDependencyException{
		//1(available)-> 2(unavailable)
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		project.addTask(task1);
		project.addTask(task2);
		task2.addDependency(task1);

		assertEquals(task1.getStatus(), TaskStatus.AVAILABLE);
		assertEquals(task2.getStatus(), TaskStatus.UNAVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);

		// 1(finished)->2(available)

		task1.setEndTime(LocalDateTime.now());
		assertEquals(task1.getStatus(), TaskStatus.FINISHED);
		assertEquals(task2.getStatus(), TaskStatus.AVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);

		// 1(failed)-> 2(unavailable)
		task1.setFailed(true);
		assertEquals(task1.getStatus(), TaskStatus.FAILED);
		assertEquals(task2.getStatus(), TaskStatus.UNAVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);
	}
	
	@Test
	public void testProjectWithNoTasksIsOngoing(){
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);
	}
	
	

}
