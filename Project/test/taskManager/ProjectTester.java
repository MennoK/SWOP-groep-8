package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

import taskManager.*;
import taskManager.exception.InvalidTimeException;
import taskManager.exception.LoopingDependencyException;
import taskManager.Project;
import taskManager.ProjectStatus;
import taskManager.Task;

import taskManager.exception.InvalidTimeException;
import taskManager.exception.LoopingDependencyException;

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
		assertEquals( 1,project.getAllTasks().size());
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
		assertEquals(2, project.getAllTasks().size());
		assertEquals(tasks, project.getAllTasks());
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
	public void testProjectStatusIsFinishedNoDependencies() throws NullPointerException, InvalidTimeException{
		// 0 task
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// 1 task
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		task1.setStartTime(LocalDateTime.of(2015, 03, 05,00,00));
		task1.setEndTime(LocalDateTime.now());
		project.addTask(task1);
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

		// 2 tasks
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		task2.setStartTime(LocalDateTime.of(2015, 03, 05,00,00));
		task2.setEndTime(LocalDateTime.now());
		project.addTask(task2);
		assertEquals( ProjectStatus.FINISHED,project.getStatus());		
	}

	@Test
	public void testProjectStatusIsFinishedDependencies() throws LoopingDependencyException, NullPointerException, InvalidTimeException{
		// 1(finished) -> 2(finished)
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		task1.setStartTime(LocalDateTime.of(2015, 03, 05,00,00));
		task2.setStartTime(LocalDateTime.of(2015, 03, 05,00,00));
		task1.setEndTime(LocalDateTime.now());
		task2.setEndTime(LocalDateTime.now());
		task2.addDependency(task1);
		project.addTask(task1);
		project.addTask(task2);
		assertEquals( TaskStatus.FINISHED,task1.getStatus());	
		assertEquals(TaskStatus.FINISHED, task2.getStatus());	
		assertEquals(ProjectStatus.FINISHED, project.getStatus());	

		// 1(failed) -> 2(finished)
		task1.setFailed();
<<<<<<< HEAD
		assertEquals(TaskStatus.FAILED, task1.getStatus());	
		assertEquals(TaskStatus.FINISHED, task2.getStatus());	
		assertEquals(ProjectStatus.FINISHED, project.getStatus());	
=======
		assertEquals(task1.getStatus(), TaskStatus.FAILED);	
		assertEquals(task2.getStatus(), TaskStatus.FINISHED);	
		assertEquals(project.getStatus(), ProjectStatus.FINISHED);	
>>>>>>> b432fc1dc5e5cc4a8c17445e80496036b9813a15

		// 1(failed) -x-> 2(finished) <- 3(finished)
		Task task3 = new Task("testdescriptionTask3", Duration.ofHours(8), 50);
		task3.setStartTime(LocalDateTime.of(2015, 03, 05,00,00));
		task3.setEndTime(LocalDateTime.now());
		task2.addDependency(task3);
		assertEquals( TaskStatus.FAILED, task1.getStatus());	
		assertEquals(TaskStatus.FINISHED, task2.getStatus());	
		assertEquals( TaskStatus.FINISHED, task3.getStatus());	
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

		// 1(failed) 2(failed) 3(finished)
		assertEquals(ProjectStatus.FINISHED, project.getStatus());

		// [1(failed) 3(failed) 4(finished)]-> 2(finished)
		Task task4 = new Task("testdescriptionTask4", Duration.ofHours(8), 50);


	}

	@Test
	public void testProjectStatusIsOngoingNoDependencies(){
		// 1 task
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		project.addTask(task1);
		assertEquals( TaskStatus.AVAILABLE, task1.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// 2 tasks
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50);
		project.addTask(task2);
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}

	@Test
	public void testProjectStatusIsOngoingDependencies() throws LoopingDependencyException, NullPointerException, InvalidTimeException{
		//1(available)-> 2(unavailable)
		ArrayList<Task> dependencies = new ArrayList<>();
		
		Task task1 = new Task("testdescriptionTask1", Duration.ofHours(8), 50);
		dependencies.add(task1);
		Task task2 = new Task("testdescriptionTask2", Duration.ofHours(8), 50, dependencies);
		Task task3 = new Task("testdescriptionTask3",Duration.ofHours(8), 50);
		dependencies.remove(0);
		dependencies.add(task3);
		Task task4 = new Task("testdescriptionTask4",Duration.ofHours(8), 50, dependencies);
		task1.setStartTime(LocalDateTime.of(2015, 03, 07,00,00));
		task2.setStartTime(LocalDateTime.of(2015, 03, 07,00,00));
		task3.setStartTime(LocalDateTime.of(2015, 03, 07,00,00));
		task4.setStartTime(LocalDateTime.of(2015, 03, 07,00,00));
		project.addTask(task1);
		project.addTask(task2);
		

		assertEquals(TaskStatus.AVAILABLE,task1.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE,task2.getStatus());
		assertEquals(ProjectStatus.ONGOING, project.getStatus());

		// 1(finished)->2(available)

		task1.setEndTime(LocalDateTime.now());
<<<<<<< HEAD
		assertEquals(TaskStatus.FINISHED,task1.getStatus());
		assertEquals(TaskStatus.AVAILABLE, task2.getStatus());
		assertEquals(ProjectStatus.ONGOING,project.getStatus());

		// 3(failed)-> 4(unavailable)
		task3.setFailed();
		assertEquals(TaskStatus.FAILED, task3.getStatus());
		assertEquals(TaskStatus.UNAVAILABLE, task4.getStatus() );
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
=======
		assertEquals(task1.getStatus(), TaskStatus.FINISHED);
		assertEquals(task2.getStatus(), TaskStatus.AVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);

		// 1(failed)-> 2(unavailable)
		task1.setFailed();
		assertEquals(task1.getStatus(), TaskStatus.FAILED);
		assertEquals(task2.getStatus(), TaskStatus.UNAVAILABLE);
		assertEquals(project.getStatus(), ProjectStatus.ONGOING);
>>>>>>> b432fc1dc5e5cc4a8c17445e80496036b9813a15
	}
	
	@Test
	public void testProjectWithNoTasksIsOngoing(){
		assertEquals(ProjectStatus.ONGOING, project.getStatus());
	}
	
	
	

}
