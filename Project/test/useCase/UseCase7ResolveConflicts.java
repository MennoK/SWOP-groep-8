package useCase;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.Project;
import taskmanager.Task;
import taskmanager.exception.ConlictingPlanningException;

public class UseCase7ResolveConflicts extends UseCaseTestBasis {

	private Project project;

	private Developer developer;

	private Task originalTask;
	private Task plannedConflictingTask;

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 04, 22, 9, 0));

		project = tmc.createProject("proj", "proj description",
				now.plusYears(1));

		developer = tmc.createDeveloper("Jimmy Doge");

		plannedConflictingTask = Task.builder("Already Planned Task",
				Duration.ofHours(8), 0.2).build(project);

		originalTask = Task.builder("a task", Duration.ofHours(8), 0.2).build(
				project);

		tmc.getPlanner().createPlanning(now, plannedConflictingTask, developer)
				.build();

	}

	@Test
	public void testGetConflictingPlanningsForBuilder() {

		try {
			// Conflicts with PlannedConflictingTask
			tmc.getPlanner()
					.createPlanning(now.plusHours(1), originalTask, developer)
					.build();
		} catch (ConlictingPlanningException conflict) {
			// Replan the conflicting task to solve the conflict
			tmc.getPlanner()
					.createPlanning(now.plusMonths(4), plannedConflictingTask,
							developer).build();
		}
		try {
			// now planning works
			tmc.getPlanner()
					.createPlanning(now.plusHours(1), originalTask, developer)
					.build();
		} catch (ConlictingPlanningException conflict) {
			fail("There should be no conflict anymore\n"
					+ conflict.getConflictingPlannings());
		}

		assertEquals(2, tmc.getPlanner().getAllPlannings().size());
	}

}
