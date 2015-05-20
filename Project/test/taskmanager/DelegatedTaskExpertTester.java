package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;


public class DelegatedTaskExpertTester extends TaskManTester {
	
	BranchOffice bruggeOffice;

	@Before
	public void setUp() {
		super.setUp();
		bruggeOffice = this.tmc.createBranchOffice("Brugge");
		this.tmc.logIn(bruggeOffice);
		Developer me = this.tmc.createDeveloper("Jozef Stalin");
		this.tmc.logIn(me);
	}
	
	@Test
	public void delegateTaskTest() {
		Project myProject = this.createStandardProject(time.plusHours(300));
		Task myTask = this.createTask(myProject, Duration.ofHours(5));
		this.tmc.delegate(myTask, here);
		assertTrue(here.getDelegatedTaskExpert().getAllDelegatedTasks().contains(myTask));
		assertTrue(here.getDelegatedTaskExpert().getOriginalOffice(myTask).equals(bruggeOffice));
		this.tmc.logIn(here);
		this.tmc.logIn(dev);
		this.tmc.delegate(myTask, bruggeOffice);
		assertFalse(here.getDelegatedTaskExpert().getAllDelegatedTasks().contains(myTask));
		assertTrue(bruggeOffice.getDelegatedTaskExpert().getAllDelegatedTasks().contains(myTask));
	}
	
	@Test
	public void mementoTest() {
		Project myProject = this.createStandardProject(time.plusHours(300));
		Task myTask = this.createTask(myProject, Duration.ofHours(5));
		here.saveSystem(bruggeOffice);
		this.tmc.delegate(myTask, here);
		here.loadSystem(bruggeOffice);
		assertEquals(0, here.getDelegatedTaskExpert().getAllDelegatedTasks().size());
	}
}
