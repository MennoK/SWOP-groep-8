package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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

import utility.TimeSpan;

public class PlannerTester {


	public TaskManController tmController;
	public Planner planner;
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
	private ArrayList<Resource> resources;
	private HashSet<Resource> resource;
	private HashSet<Resource> resource2;
	
	
	@Before
	public void setUp() {
		//2 default times
		this.time1 =  LocalDateTime.of(2015, 03, 10, 11, 00);
		this.time2 =  LocalDateTime.of(2015, 03, 10, 15, 00);
		tmController = new TaskManController(time1);
		//create planning expert 
		this.planner = tmController.getPlanner();
		//create some resources
		resourceExpert = tmController.getResourceExpert();
		ResourceType.builder("type").build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
						resourceExpert.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);	
		resourceType.createResource("resource");
		resourceType.createResource("resource2");
		
		resources = new ArrayList<Resource>(resourceType.getAllResources());
		resource = new HashSet<Resource>();
		resource.add(resources.get(0));
		resource2 = new HashSet<Resource>();
		resource2.add(resources.get(1));
		//create a project with a task
	
		projectExpert = tmController.getProjectExpert();
		projectExpert.createProject("name", "des", time2.plusDays(13));
		project = projectExpert.getAllProjects().get(0);
		Task.builder("task 1", Duration.ofHours(1), 1).build(project);
		Task.builder("task 2", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 1).build(project);
		task1 = project.getAllTasks().get(0);
		task2 = project.getAllTasks().get(1);
		
		//create some developers
		developerExpert = tmController.getDeveloperExpert();				
		developerExpert.createDeveloper("person1resource2 = resources.get(1);");
		developerExpert.createDeveloper("person2");
		
		developer1 = (Developer) developerExpert.getAllDevelopers().toArray()[0];
		developer2 = (Developer) developerExpert.getAllDevelopers().toArray()[1];
		
		
		
	}
	


	@Test
	public void testGetUnplannedTasks() {


		//create planning for task1
		Planning.builder(time1, task1, developer1).addDeveloper(developer2).build(planner);
		
		//check if the planning has been created
		assertEquals(1, planner.getAllPlannings().size());
		
		//check if the method getUnplannedTasks returns task2
		Set<Task> unplannedTasks = new HashSet<>();
		unplannedTasks.add(task2);
		assertEquals(unplannedTasks, planner.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())));
		assertEquals(1, planner.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())).size());
		
		Task.builder("task3", Duration.ofHours(3), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		unplannedTasks.add(task3);

		assertEquals(unplannedTasks, planner.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())));
		assertEquals(2, planner.getUnplannedTasks(new HashSet<Task>(project.getAllTasks())).size());
		
	}

	@Test
	public void testCreatePlanning() {
		//create planning for task1 (needs no resources) 
		Planning.builder(time1, task1, developer1).addDeveloper(developer2).build(planner);
		// check if 1 planning exist
		assertEquals(1, planner.getAllPlannings().size());
		
		//create planning for task2 (needs 1 resource)
		
		Map<ResourceType, Set<Resource>> resources = new LinkedHashMap<ResourceType, Set<Resource>>();
		resources.put(resourceType, resourceType.getAllResources());
		Planning.builder(time2, task2, developer1).addDeveloper(developer2).addResources(resourceType, resourceType.getAllResources()).build(planner);
		
		//check if 2 plannings exist
		assertEquals(2, planner.getAllPlannings().size());
		
		//check if the plannings are made correctly
		ArrayList<Planning> planningList = new ArrayList<Planning>();
		planningList.addAll(planner.getAllPlannings());
		assertEquals(this.time1,planningList.get(0).getTimeSpan().getBegin());
		assertEquals(time1.plus(task1.getDuration()),planningList.get(0).getTimeSpan().getEnd());
		assertEquals(this.developerExpert.getAllDevelopers(),planningList.get(0).getDevelopers());
		assertTrue(planningList.get(0).getResources().isEmpty());
		
		assertEquals(this.time2,planningList.get(1).getTimeSpan().getBegin());
		assertEquals(time2.plus(task2.getDuration()),planningList.get(1).getTimeSpan().getEnd());
		assertEquals((this.developerExpert.getAllDevelopers()),planningList.get(1).getDevelopers());
		assertEquals(resourceType.getAllResources(), planningList.get(1).getResources().get(resourceType));
		

	}

	@Test 
	public void testGetPossibleStartTimes(){
		//CASE1: everything is available
		Set<LocalDateTime> possibleStartTimes111213 = new LinkedHashSet<>();
		possibleStartTimes111213.add(time1);
		possibleStartTimes111213.add(time1.plusHours(1));
		possibleStartTimes111213.add(time1.plusHours(2));
		assertEquals(possibleStartTimes111213,planner.getPossibleStartTimes(task1, time1, this.developerExpert.getAllDevelopers()));
		Planning.builder(time1, task1, developer1).addDeveloper(developer2).addResources(resourceType, resource).build(planner);

		//CASE2: task1 + allDevs are planned for time1 until time1+1
		Set<LocalDateTime> possibleStartTimes121314 = new LinkedHashSet<>();
		possibleStartTimes121314.add(time1.plusHours(1));
		possibleStartTimes121314.add(time1.plusHours(2));
		possibleStartTimes121314.add(time1.plusHours(3));
		assertEquals(possibleStartTimes121314,planner.getPossibleStartTimes(task2, time1, this.developerExpert.getAllDevelopers()));
		Planning.builder(time1.plusHours(3), task2, developer1).addDeveloper(developer2).build(planner);
	
		//CASE3:  task1 + allDevs are planned for time1 until time1+1 AND task2 + resource + all devs are planned for time1+3
		//subcase: 1 timeslot is available between planning of task 1 and task 2
		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		Set<LocalDateTime> possibleStartTimes121617 = new LinkedHashSet<>();
		possibleStartTimes121617.add(time1.plusHours(5));
		possibleStartTimes121617.add(time1.plusHours(6));
		possibleStartTimes121617.add(time1.plusHours(7));

		assertEquals(possibleStartTimes121617,planner.getPossibleStartTimes(task3, time1, this.developerExpert.getAllDevelopers()));
		
		//subcase: 2 timeslots are available between planning of task 1 and task 2

		Task.builder("task4 ", Duration.ofHours(1), 2).build(project);
		Task task4 = project.getAllTasks().get(3);
		Set<LocalDateTime> possibleStartTimes121316 = new LinkedHashSet<>();
		possibleStartTimes121316.add(time1.plusHours(1));
		possibleStartTimes121316.add(time1.plusHours(2));
		possibleStartTimes121316.add(time1.plusHours(5));

		assertEquals(possibleStartTimes121316,planner.getPossibleStartTimes(task4, time1,  this.developerExpert.getAllDevelopers()));
		Planning.builder(time1.plusHours(1), task4, developer1).build(planner);
		
		//CASE4: some developpers planned, some available -> same test as before for task3, task4 is planned on time1+1 and has 1 developer planned, 1 still available so result should be the same
		assertEquals(possibleStartTimes121617,planner.getPossibleStartTimes(task3, time1, this.developerExpert.getAllDevelopers()));
		
		//CASE5: some resources planned, some available -> 
		Task.builder("task5 ", Duration.ofHours(1), 2).addRequiredResourceType(resourceType, 1).build(project);
		Task.builder("task6 ", Duration.ofHours(1), 2).addRequiredResourceType(resourceType, 1).build(project);
		Task task5 = project.getAllTasks().get(4);
		Task task6 = project.getAllTasks().get(5);
		
		Planning.builder(time1.plusHours(2), task5, developer1).addDeveloper(developer2).addResources(resourceType,resource).build(planner);
	
		assertEquals(possibleStartTimes121617,planner.getPossibleStartTimes(task6, time1, this.developerExpert.getAllDevelopers()));
		
		
		
	}
	@Test
	public void testHasConflictWithPlannedTask() {
		//create planning for task 1 so that it can conflict with task 2 at certain times
		Planning.builder(time1, task1,  developer1).addDeveloper(developer2).build(planner);

		assertTrue(planner.hasConflictWithAPlannedTask(task2, time1));

		assertTrue(planner.hasConflictWithAPlannedTask(task2, time1.minusHours(1)));

		assertFalse(planner.hasConflictWithAPlannedTask(task2, time1.minusHours(2)));

		assertFalse(planner.hasConflictWithAPlannedTask(task2, time1.plusHours(1)));
		
		assertFalse(planner.hasConflictWithAPlannedTask(task2, time2));

		//task 2 is planned at time1 + 3
		Planning.builder(time1.plusHours(3), task2,  developer1).addDeveloper(developer2).build(planner);
		
		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		assertTrue(planner.hasConflictWithAPlannedTask(task3, time1));

		assertTrue(planner.hasConflictWithAPlannedTask(task3, time1.plusHours(1)));

		assertTrue(planner.hasConflictWithAPlannedTask(task3, time1.plusHours(2)));
	}

	@Test
	public void testGetConflictingTasks() {

		//create a planning for task 1 and see if it will be returned when it would conflict with task 2 
		Planning.builder(time1, task1,  developer1).addDeveloper(developer2).build(planner);
		
		Set<Task> allTasks = new LinkedHashSet<>(project.getAllTasks());
		Set<Task> conflictSet = new LinkedHashSet<>();
		conflictSet.add(task1);
		assertEquals(conflictSet, planner.getConflictingTasks(task2, time1.minusHours(1), allTasks));

		Planning.builder(time1.plusHours(3), task2,  developer1).addDeveloper(developer2).build(planner);

		Task.builder("task3 ", Duration.ofHours(2), 2).build(project);
		Task task3 = project.getAllTasks().get(2);
		assertNotEquals(conflictSet, planner.getConflictingTasks(task3, time1.plusHours(1), allTasks));

		Task.builder("task4", Duration.ofHours(4), 2).build(project);
		Task task4 = project.getAllTasks().get(3);
		conflictSet.add(task2);
		assertEquals(conflictSet, planner.getConflictingTasks(task4, time1, allTasks));

	}

	//TODO: 
	public Set<Resource> getAvailableResourcesOfType(ResourceType resourceType){
		Set<Resource> resourceList = new LinkedHashSet<>();
		return resourceList;
	}
	@Test
	public void testResolveConflictingTasks() {
			
	}
	
	@Test
	public void testDeveloperAvailableForSimple(){
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(2, planner.developersAvailableFor(developerExpert.getAllDevelopers(), task1, timeSpan).size());
	}
	
	@Test
	public void testDeveloperAvailableForPlannedTask(){
		Planning.builder(time1.plusHours(1), task1, developer1).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(2, planner.developersAvailableFor(developerExpert.getAllDevelopers(), task1,timeSpan).size());
	}
	
	@Test
	public void testDeveloperUnavailable(){
		Planning.builder(time1, task2, developer1).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(1, planner.developersAvailableFor(developerExpert.getAllDevelopers(), task1,timeSpan).size());
	}
	
	@Test
	public void testDeveloperBothUnavailable(){
		Planning.builder(time1, task2, developer1).addDeveloper(developer2).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(0, planner.developersAvailableFor(developerExpert.getAllDevelopers(), task1,timeSpan).size());
	}
	
	@Test
	public void testDeveloperOneOutOfTwoUnavailable(){
		Planning.builder(time1.plusMinutes(30), task2, developer1).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(1, planner.developersAvailableFor(developerExpert.getAllDevelopers(), task1,timeSpan).size());
	}
	
	@Test
	public void testDeveloperAvailableWithOtherPlanning(){
		Planning.builder(time2, task2, developer1).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(2, planner.developersAvailableFor(developerExpert.getAllDevelopers(), task1,timeSpan).size());
	}
	
	@Test
	public void testResourcesAvailableSimple(){
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(1, planner.resourcesAvailableFor(task2, timeSpan).size());
		Map.Entry<ResourceType, Set<Resource>> map = planner.resourcesAvailableFor(task2, timeSpan).entrySet().iterator().next();
		assertEquals(2, map.getValue().size());
	}
	
	@Test
	public void testResourcesAvailableForPlannedTask(){
		TimeSpan timeSpan = new TimeSpan(time1, task1.getDuration());
		assertEquals(1, planner.resourcesAvailableFor(task2, timeSpan).size());
		Map.Entry<ResourceType, Set<Resource>> map = planner.resourcesAvailableFor(task2, timeSpan).entrySet().iterator().next();
		assertEquals(2, map.getValue().size());
	}
	
	@Test
	public void testResourceUnavailable(){
		Task.builder("task 3", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 1).build(project);
		Task task3 = project.getAllTasks().get(2);		
		Planning.builder(time1, task2, developer1).addResources(resourceType,resource).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task3.getDuration());
		Map.Entry<ResourceType, Set<Resource>> map = planner.resourcesAvailableFor(task3, timeSpan).entrySet().iterator().next();
		assertEquals(1, map.getValue().size());
	}
	
	@Test 
	public void testResourceBothUnavailable(){
		Task.builder("task 3", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 2).build(project);
		Task task3 = project.getAllTasks().get(2);	
		Task.builder("task 4", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 2).build(project);
		Task task4 = project.getAllTasks().get(3);	
		Set<Resource> resourcesset = new LinkedHashSet<Resource>(resources);
		Planning.builder(time1, task3, developer1).addResources(resourceType,resourcesset).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1, task4.getDuration());
		Map.Entry<ResourceType, Set<Resource>> map = planner.resourcesAvailableFor(task4, timeSpan).entrySet().iterator().next();
		assertEquals(0, map.getValue().size());
	}
	
	@Test
	public void testResourceAvailableWithOtherPlanning(){
		Task.builder("task 3", Duration.ofHours(2), 1).addRequiredResourceType(resourceType, 1).build(project);
		Task task3 = project.getAllTasks().get(2);		
		Planning.builder(time1, task2, developer1).addResources(resourceType,resource).build(planner);
		TimeSpan timeSpan = new TimeSpan(time1.plusHours(3), task3.getDuration());
		Map.Entry<ResourceType, Set<Resource>> map = planner.resourcesAvailableFor(task3, timeSpan).entrySet().iterator().next();
		assertEquals(2, map.getValue().size());
	}
	
	@Test
	public void testMementoRollbackRemovesPlannings() {
		
		planner.save();
		
		//create planning for task1
		Planning.builder(time1, task1, developer1).addDeveloper(developer2).build(planner);
		
		//check if the planning has been created
		assertEquals(1, planner.getAllPlannings().size());
		
		planner.load();
		
		assertEquals(0, planner.getAllPlannings().size());
		
	}

}
