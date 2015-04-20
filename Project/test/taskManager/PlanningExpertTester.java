package taskManager;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class PlanningExpertTester {

	public PlanningExpert planningExpert;
	public LocalDateTime time1;
	public LocalDateTime time2;
	public Project project;
	public Task task1;
	public Task task2;
	public DeveloperExpert developerExpert; 
	public Set<Developer> developers;
	private ResourceExpert resourceExpert;
	private ArrayList<ResourceType> resourceTypeList;
	private ResourceType resourceType;
	@Before
	public void setUp() {
		//2 default times
		this.time1 =  LocalDateTime.of(2015, 03, 10, 11, 00);
		this.time2 =  LocalDateTime.of(2015, 03, 10, 15, 00);
		
		//create planning expert 
		this.planningExpert = new PlanningExpert();
		//create some resources
		resourceExpert = new ResourceExpert();
		resourceExpert.createResourceType("type").build();
		resourceTypeList = new ArrayList<ResourceType>(
						resourceExpert.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);	
		resourceType.createResource("resource");
		resourceType.createResource("resource2");
		
				
		//create a project with a task
		project = new Project("project", "a project", time1.minusDays(1), time2.plusDays(1));
		project.createTask("a task", Duration.ofHours(1), 1).build();
		project.createTask("a task", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 1).build();
		task1 = project.getAllTasks().get(0);
		task2 = project.getAllTasks().get(1);
		
		//create some developers
		developerExpert = new DeveloperExpert();
		developerExpert.createDeveloper("person1");
		developerExpert.createDeveloper("person2");
		developers = new LinkedHashSet<>(developerExpert.getAllDevelopers());
		
		
		
	}


	@Test
	public void testGetUnplannedTasks() {
		//create planning for task1
		planningExpert.createPlanning(time1, task1, developers).build(planningExpert);
		assertEquals(1, planningExpert.getAllPlannings().size());
		Set<Task> unplannedTasks = new HashSet<>();
		unplannedTasks.add(task2);
		assertEquals(unplannedTasks, planningExpert.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())));
	}

	@Test
	public void testCreatePlanning() {
		//create planning for task1
		planningExpert.createPlanning(time1, task1, developers).build(planningExpert);
		assertEquals(1, planningExpert.getAllPlannings().size());
		
		//create planning for task2
		
		Map<ResourceType, Set<Resource>> resources = new LinkedHashMap<ResourceType, Set<Resource>>();
		resources.put(resourceType, resourceType.getAllResources());
		planningExpert.createPlanning(time2, task2, developers).addResources(resources).build(planningExpert);
		assertEquals(2, planningExpert.getAllPlannings().size());
		
		ArrayList<Planning> planningList = new ArrayList<Planning>();
		planningList.addAll(planningExpert.getAllPlannings());
		assertEquals(this.time1,planningList.get(0).getStartTime());
		assertEquals(time1.plus(task1.getDuration()),planningList.get(0).getEndTime());
		assertEquals(this.developers,planningList.get(0).getDevelopers());
		assertTrue(planningList.get(0).getResources().isEmpty());
		
		assertEquals(this.time2,planningList.get(1).getStartTime());
		assertEquals(time2.plus(task2.getDuration()),planningList.get(1).getEndTime());
		assertEquals(this.developers,planningList.get(1).getDevelopers());
		assertEquals(resourceType.getAllResources(), planningList.get(1).getResources());
	}

	@Test 
	public void testGetPossibleStartTimes(){
		
		//CASE1: everything is available
		Set<LocalDateTime> possibleStartTimes = new HashSet<>();
		possibleStartTimes.add(time1);
		possibleStartTimes.add(time1.plusHours(1));
		possibleStartTimes.add(time1.plusHours(2));
		assertEquals(possibleStartTimes,planningExpert.getPossibleStartTimes(task1, time1, developers));
		
		planningExpert.createPlanning(time1, task1, developers);
		
		//CASE2: task1 + allDevs are planned for time1 until time1+1
		Set<LocalDateTime> possibleStartTimes2 = new HashSet<>();
		possibleStartTimes.add(time1.plusHours(1));
		possibleStartTimes.add(time1.plusHours(2));
		possibleStartTimes.add(time1.plusHours(3));
		assertEquals(possibleStartTimes2,planningExpert.getPossibleStartTimes(task2, time1, developers));
		
	}
	@Test
	public void testHasConflictWithPlannedTask() {
		planningExpert.createPlanning(time1, task1, developers).build(planningExpert);

		assertTrue(planningExpert.hasConflictWithAPlannedTask(task2, time1));

		assertTrue(planningExpert.hasConflictWithAPlannedTask(task2, time1.minusHours(1)));

		assertFalse(planningExpert.hasConflictWithAPlannedTask(task2, time2));

	}

	@Test
	public void testGetConflictingTasks() {

		planningExpert.createPlanning(time1, task1, developers).build(planningExpert);
		
		Set<Task> conflictSet = new HashSet<>();
		conflictSet.add(task1);
		assertEquals(conflictSet, planningExpert.getConflictingTasks(task2, time1.minusHours(1)));
	}

	@Test
	public void testResolveConflictingTasks() {

	}

}
