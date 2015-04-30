package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskManager.Developer;
import taskManager.Planning;
import taskManager.Project;
import taskManager.Task;
import taskManager.TaskManController;
import taskManager.exception.ConlictingPlanningException;

public class UseCase7ResolveConflicts {

	private TaskManController tmc;
	private Project project;
	private LocalDateTime now;

	private Developer developer;

	private Task originalTask;
	private Task plannedConflictingTask;

	@Before
	public void setUp() {

		now = LocalDateTime.of(2015, 04, 22, 9, 0);

		tmc = new TaskManController(now);
		project = tmc.createProject("proj", "proj description",
				now.plusYears(1));

		developer = tmc.createDeveloper("Jimmy Doge");

		plannedConflictingTask = Task.builder("Already Planned Task",
				Duration.ofHours(8), 0.2).build(project);

		originalTask = Task.builder("a task", Duration.ofHours(8), 0.2).build(
				project);

		Planning.builder(now, plannedConflictingTask, developer,
				tmc.getPlanner()).build();

	}

	@Test
	public void testGetConflictingPlanningsForBuilder() {

		try {
			// Conflicts with PlannedConflictingTask
			Planning.builder(now.plusHours(1), originalTask, developer,
					tmc.getPlanner()).build();
		} catch (ConlictingPlanningException conflict) {
			// Replan the conflicting task to solve the conflict
			Planning.builder(now.plusMonths(4), plannedConflictingTask,
					developer, tmc.getPlanner()).build();
		}
		try {
			// now planning works
			Planning.builder(now.plusHours(1), originalTask, developer,
					tmc.getPlanner()).build();
		} catch (ConlictingPlanningException conflict) {
			fail("There should be no conflict anymore\n"
					+ conflict.getConflictingPlannings());
		}

		assertEquals(2, tmc.getPlanner().getAllPlannings().size());
	}

}
