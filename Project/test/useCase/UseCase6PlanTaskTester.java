package useCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import utility.TimeSpan;
import taskmanager.Developer;
import taskmanager.Planner;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.exception.ConlictingPlanningException;

public class UseCase6PlanTaskTester extends UseCaseTestBasis {

	private Planner planner;
	private Project project;
	private Task task1;
	private Task task2;
	private Developer dev1;
	private ResourceType resourceType;

	@Before
	public void setUp() {
		setUpTMC(LocalDateTime.of(2015, 03, 10, 11, 00));
		// create planning expert
		this.planner = tmc.getPlanner();
		// create some resources
		resourceType = ResourceType.builder("type")
				.build(tmc.getActiveOffice());
		resourceType.createResource("resource");
		resourceType.createResource("resource2");

		// create a project with 2 tasks
		project = tmc.createProject("name", "des", now.plusHours(3)
				.plusDays(13));
		task1 = Task.builder("a task", Duration.ofHours(1), 1).build(project);
		task2 = Task.builder("a task", Duration.ofHours(2), 1)
				.addRequiredResourceType(resourceType, 1).build(project);

		// create some tmc.getAllDevelopers()
		dev1 = tmc.createDeveloper("person1");
		tmc.createDeveloper("person2");
	}

	@Test
	public void planTask() {
		// user gets list with all unplanned tasks (task1 and task2)

		Set<Task> unplannedTasks = new LinkedHashSet<>(project.getAllTasks());
		project.getAllTasks();
		assertEquals(unplannedTasks, tmc.getUnplannedTasks());
		// user selects task2 and receives 3 possible start times
		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<>();
		possibleStartTimes.add(now);
		possibleStartTimes.add(now.plusHours(1));
		possibleStartTimes.add(now.plusHours(3));
		assertEquals(
				possibleStartTimes,
				planner.getPossibleStartTimes(task2, now,
						tmc.getAllDevelopers()));
		TimeSpan timeSpan = new TimeSpan(now, task1.getDuration());
		// user selects time1
		// the system shows possible available resources
		assertEquals(resourceType.getAllResources(),
				planner.resourcesOfTypeAvailableFor(resourceType, task2,
						timeSpan));
		// user selects a resource
		// system shows tmc.getAllDevelopers()
		assertEquals(tmc.getAllDevelopers(), planner.developersAvailableFor(
				tmc.getAllDevelopers(), task2, timeSpan));
		// user selects a developer

		// system makes reservation
		Planning newPlanning = tmc.getPlanner()
				.createPlanning(now, task2, dev1).build();

		assertEquals(this.now, newPlanning.getTimeSpan().getBegin());
		assertEquals(now.plus(task2.getDuration()), newPlanning.getTimeSpan()
				.getEnd());
		assertTrue(newPlanning.getDevelopers().contains(dev1));
		assertEquals(1, newPlanning.getDevelopers().size());
		assertTrue(newPlanning.getResources().isEmpty());

	}

	@Test(expected = ConlictingPlanningException.class)
	public void extensionUserSelectsTime() {
		tmc.getPlanner().createPlanning(now, task1, dev1).build();
		// the user selects a time for task2 that will conflict with task1
		tmc.getPlanner().createPlanning(now, task2, dev1).build();
		// use case resolve conflict starts
	}

	@Test
	public void resolveConflict() {
		tmc.getPlanner().createPlanning(now, task1, dev1).build();
		// use case resolveconflict starts
		// user chooses to move conflicting task
		// step 4 of use case plan task for the task that must be moved:
		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<>();
		possibleStartTimes.add(now);
		possibleStartTimes.add(now.plusHours(1));
		possibleStartTimes.add(now.plusHours(3));
		assertEquals(
				possibleStartTimes,
				planner.getPossibleStartTimes(task1, now,
						tmc.getAllDevelopers()));

		// user selects time1 +2
		tmc.getPlanner()
				.getPlanning(task1)
				.setTimeSpan(
						new TimeSpan(now.plusHours(2), task1.getDuration()));

		// resolve conflict ends -> back to original planning of the task

		Planning newPlanning = tmc.getPlanner()
				.createPlanning(now, task2, dev1).build();

		assertEquals(this.now.plusHours(2), tmc.getPlanner().getPlanning(task1)
				.getTimeSpan().getBegin());
		assertEquals(this.now, newPlanning.getTimeSpan().getBegin());
	}

}
