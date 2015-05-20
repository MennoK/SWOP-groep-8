package useCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.BranchOffice;
import taskmanager.Developer;
import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;

public class UseCase9DelegateTask extends UseCaseTestBasis {

	private Project project;
	private BranchOffice office2;

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 03, 06, 8, 00));
		project = tmc.createProject("project", "desc", now.plusDays(4));
		office2 = tmc.createBranchOffice("Wonderland");
	}

	@Test
	public void delegateTask() {
		Task taskToDelegate = Task.builder("delegate", Duration.ofHours(1), 1)
				.build(project);
		tmc.delegate(taskToDelegate, office2);

		assertTrue(tmc.getAllDelegatedTasksTo(office2).contains(taskToDelegate));
		assertEquals(1, tmc.getAllDelegatedTasksTo(office2).size());
	}
}
