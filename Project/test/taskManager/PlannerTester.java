package taskManager;

import static org.junit.Assert.*;

import java.awt.List;
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

public class PlannerTester {


	public TaskManController tmController;
	public Planner planningExpert;
	public LocalDateTime time1;
	public LocalDateTime time2;
	public Project project;
	public Task task1;
	public Task task2;
	public DeveloperExpert developerExpert; 
	private ResourceExpert resourceExpert;
	private ArrayList<ResourceType> resourceTypeList;
	private ResourceType resourceType;
	private ProjectExpert projectExpert;
	private Developer developer1;
	private Developer developer2;
	@Before
	public void setUp() {
		//2 default times
		this.time1 =  LocalDateTime.of(2015, 03, 10, 11, 00);
		this.time2 =  LocalDateTime.of(2015, 03, 10, 15, 00);
		tmController = new TaskManController(time1);
		//create planning expert 
		this.planningExpert = tmController.getPlanner();
		//create some resources
		resourceExpert = tmController.getResourceExpert();
		resourceExpert.resourceTypeBuilder("type").build();
		resourceTypeList = new ArrayList<ResourceType>(
						resourceExpert.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);	
		resourceType.createResource("resource");
		resourceType.createResource("resource2");
				
		//create a project with a task
	
		projectExpert = tmController.getProjectExpert();
		projectExpert.createProject("name", "des", time2.plusDays(13));
		project = projectExpert.getAllProjects().get(0);
		project.taskBuilder("a task", Duration.ofHours(1), 1).build();
		project.taskBuilder("a task", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 1).build();
		task1 = project.getAllTasks().get(0);
		task2 = project.getAllTasks().get(1);
		
		//create some developers
		developerExpert = tmController.getDeveloperExpert();				
		developerExpert.createDeveloper("person1");
		developerExpert.createDeveloper("person2");
		
		developer1 = (Developer) developerExpert.getAllDevelopers().toArray()[0];
		developer2 = (Developer) developerExpert.getAllDevelopers().toArray()[1];
		
		
		
	}



	@Test
	public void testGetUnplannedTasks() {


		//create planning for task1
		planningExpert.createPlanning(time1, task1, developer1).addDeveloper(developer2).build(planningExpert);
		
		//check if the planning has been created
		assertEquals(1, planningExpert.getAllPlannings().size());
		
		//check if the method getUnplannedTasks returns task2
		Set<Task> unplannedTasks = new HashSet<>();
		unplannedTasks.add(task2);
		assertEquals(unplannedTasks, planningExpert.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())));
		assertEquals(1, planningExpert.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())).size());
		
		project.taskBuilder("task3", Duration.ofHours(3), 2).build();
		Task task3 = project.getAllTasks().get(2);
		unplannedTasks.add(task3);

		assertEquals(unplannedTasks, planningExpert.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())));
		assertEquals(2, planningExpert.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())).size());
		
	}

	@Test
	public void testCreatePlanning() {
		//create planning for task1 (needs no resources) 
		planningExpert.createPlanning(time1, task1, developer1).addDeveloper(developer2).build(planningExpert);
		// check if 1 planning exist
		assertEquals(1, planningExpert.getAllPlannings().size());
		
		//create planning for task2 (needs 1 resource)
		
		Map<ResourceType, Set<Resource>> resources = new LinkedHashMap<ResourceType, Set<Resource>>();
		resources.put(resourceType, resourceType.getAllResources());
		planningExpert.createPlanning(time2, task2, developer1).addDeveloper(developer2).addResources(resourceType, resourceType.getAllResources()).build(planningExpert);
		
		//check if 2 plannings exist
		assertEquals(2, planningExpert.getAllPlannings().size());
		
		//check if the plannings are made correctly
		ArrayList<Planning> planningList = new ArrayList<Planning>();
		planningList.addAll(planningExpert.getAllPlannings());
		assertEquals(this.time1,planningList.get(0).getStartTime());
		assertEquals(time1.plus(task1.getDuration()),planningList.get(0).getEndTime());
		assertEquals(this.developerExpert.getAllDevelopers(),planningList.get(0).getDevelopers());
		assertTrue(planningList.get(0).getResources().isEmpty());
		
		assertEquals(this.time2,planningList.get(1).getStartTime());
		assertEquals(time2.plus(task2.getDuration()),planningList.get(1).getEndTime());
		assertEquals((this.developerExpert.getAllDevelopers()),planningList.get(1).getDevelopers());
		assertEquals(resourceType.getAllResources(), planningList.get(1).getResources().get(resourceType));
		

	}

	@Test 
	public void testGetPossibleStartTimes(){
		//CASE1: everything is available
		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<>();
		possibleStartTimes.add(time1);
		possibleStartTimes.add(time1.plusHours(1));
		possibleStartTimes.add(time1.plusHours(2));
		assertEquals(possibleStartTimes,planningExpert.getPossibleStartTimes(task1, time1, this.developerExpert.getAllDevelopers()));
		planningExpert.createPlanning(time1, task1, developer1).addDeveloper(developer2).build(planningExpert);

		//CASE2: task1 + allDevs are planned for time1 until time1+1
		Set<LocalDateTime> possibleStartTimes2 = new LinkedHashSet<>();
		possibleStartTimes2.add(time1.plusHours(1));
		possibleStartTimes2.add(time1.plusHours(2));
		possibleStartTimes2.add(time1.plusHours(3));
		assertEquals(possibleStartTimes2,planningExpert.getPossibleStartTimes(task2, time1, this.developerExpert.getAllDevelopers()));
		planningExpert.createPlanning(time1.plusHours(3), task2, developer1).addDeveloper(developer2).build(planningExpert);
	
		//CASE3:  task1 + allDevs are planned for time1 until time1+1 AND task2 + resource + all devs are planned for time1+3
		
		//subcase: 1 timeslot is available between planning of task 1 and task 2
		project.taskBuilder("task3 ", Duration.ofHours(2), 2).build();
		Task task3 = project.getAllTasks().get(2);
		Set<LocalDateTime> possibleStartTimes3 = new LinkedHashSet<>();
		possibleStartTimes3.add(time1.plusHours(1));
		possibleStartTimes3.add(time1.plusHours(5));
		possibleStartTimes3.add(time1.plusHours(6));

		assertEquals(possibleStartTimes3,planningExpert.getPossibleStartTimes(task3, time1, this.developerExpert.getAllDevelopers()));
		
		//subcase: 2 timeslots are available between planning of task 1 and task 2

		project.taskBuilder("task4 ", Duration.ofHours(1), 2).build();
		Task task4 = project.getAllTasks().get(3);
		Set<LocalDateTime> possibleStartTimes4 = new LinkedHashSet<>();
		possibleStartTimes4.add(time1.plusHours(1));
		possibleStartTimes4.add(time1.plusHours(2));
		possibleStartTimes4.add(time1.plusHours(5));

		assertEquals(possibleStartTimes4,planningExpert.getPossibleStartTimes(task4, time1,  this.developerExpert.getAllDevelopers()));
	}
	@Test
	public void testHasConflictWithPlannedTask() {
		//create planning for task 1 so that it can conflict with task 2 at certain times
		planningExpert.createPlanning(time1, task1,  developer1).addDeveloper(developer2).build(planningExpert);

		assertTrue(planningExpert.hasConflictWithAPlannedTask(task2, time1));

		assertTrue(planningExpert.hasConflictWithAPlannedTask(task2, time1.minusHours(1)));

		assertFalse(planningExpert.hasConflictWithAPlannedTask(task2, time1.minusHours(2)));

		assertFalse(planningExpert.hasConflictWithAPlannedTask(task2, time1.plusHours(1)));
		
		assertFalse(planningExpert.hasConflictWithAPlannedTask(task2, time2));

		//task 2 is planned at time1 + 3
		planningExpert.createPlanning(time1.plusHours(3), task2,  developer1).addDeveloper(developer2).build(planningExpert);
		
		project.taskBuilder("task3 ", Duration.ofHours(2), 2).build();
		Task task3 = project.getAllTasks().get(2);
		assertTrue(planningExpert.hasConflictWithAPlannedTask(task3, time1));

		assertFalse(planningExpert.hasConflictWithAPlannedTask(task3, time1.plusHours(1)));

		assertTrue(planningExpert.hasConflictWithAPlannedTask(task3, time1.plusHours(2)));
	}

	@Test
	public void testGetConflictingTasks() {

		//create a planning for task 1 and see if it will be returned when it would conflict with task 2 
		planningExpert.createPlanning(time1, task1,  developer1).addDeveloper(developer2).build(planningExpert);
		
		Set<Task> allTasks = new LinkedHashSet<>(project.getAllTasks());
		Set<Task> conflictSet = new LinkedHashSet<>();
		conflictSet.add(task1);
		assertEquals(conflictSet, planningExpert.getConflictingTasks(task2, time1.minusHours(1), allTasks));

		planningExpert.createPlanning(time1.plusHours(3), task2,  developer1).addDeveloper(developer2).build(planningExpert);

		project.taskBuilder("task3 ", Duration.ofHours(2), 2).build();
		Task task3 = project.getAllTasks().get(2);
		assertNotEquals(conflictSet, planningExpert.getConflictingTasks(task3, time1.plusHours(1), allTasks));

		project.taskBuilder("task4", Duration.ofHours(4), 2).build();
		Task task4 = project.getAllTasks().get(3);
		conflictSet.add(task2);
		assertEquals(conflictSet, planningExpert.getConflictingTasks(task4, time1, allTasks));

	}

	//TODO: 
	public Set<Resource> getAvailableResourcesOfType(ResourceType resourceType){
		Set<Resource> resourceList = new LinkedHashSet<>();
		return resourceList;
	}
	@Test
	public void testResolveConflictingTasks() {
			
	}

}
