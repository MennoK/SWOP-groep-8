package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import parser.TaskManInitFileChecker.IntPair;


import taskManager.DeveloperExpert;
import taskManager.Project;
import taskManager.ProjectExpert;
import taskManager.Resource;
import taskManager.ResourceExpert;
import taskManager.ResourceType;
import taskManager.ResourceType.ResourceTypeBuilder;
import taskManager.Task;
import taskManager.TimeInterval;
import taskManager.Task.TaskBuilder;
import taskManager.TaskManController;

/**
 * The Parser class implements a YAML parser for TaskMan
 * We are using the snakeYAML library
 * 
 * @author Groep 8
 */

public class Parser {

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm");
	private DateTimeFormatter timeFormatter = DateTimeFormatter
			.ofPattern("HH:mm");

	private List<TimeInterval> timeIntervals = new  ArrayList<TimeInterval>();

	/**
	 * This method parses the input file (needs absolute path) after it has
	 * checked if the given file is in valid format for TaskMan.
	 * 
	 * @param pathToFile
	 * @param projectController
	 * @throws FileNotFoundException
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public void parse(String pathToFile, TaskManController controller)
			throws FileNotFoundException, RuntimeException {

		// check if the given input file is valid for taskman
		TaskManInitFileChecker checker = new TaskManInitFileChecker(
				new FileReader(pathToFile));
		checker.checkFile();

		// create new yaml
		InputStream input = new FileInputStream(new File(pathToFile));
		Yaml yaml = new Yaml();
		Map<String, Object> objects = (Map<String, Object>) yaml.load(input);

		// create system time
		constructSystemTime((CharSequence) objects.get("systemTime"),controller);

		// create daily availability
		constructDailyAvailabilities((List<LinkedHashMap<String, Object>>) objects.get("dailyAvailability"));

		// create all resource types
		constructResourceTypes(	(List<LinkedHashMap<String, Object>>) objects.get("resourceTypes"),controller.getResourceExpert());

		// create all resources
		constructResources(	(List<LinkedHashMap<String, Object>>) objects.get("resources"),controller.getResourceExpert());

		// create all developers
		constructDevelopers((List<LinkedHashMap<String, Object>>) objects.get("developers"),controller.getDeveloperExpert());

		// create all projects
		constructProjects((List<LinkedHashMap<String, Object>>) objects.get("projects"),controller.getProjectExpert());

		// create all tasks
		constructTasks((List<LinkedHashMap<String, Object>>) objects.get("tasks"),controller.getProjectExpert(),controller.getResourceExpert());

		//construct plannings
		constructPlannings((List<LinkedHashMap<String, Object>>) objects.get("plannings"),controller);

	}


	private void constructSystemTime(CharSequence time, TaskManController controller) {
		LocalDateTime systemTime = LocalDateTime.parse(time, dateTimeFormatter);
		controller.advanceTime(systemTime);
	}

	/**
	 * Constructs the daily availabilities 
	 */
	private void constructDailyAvailabilities(
			List<LinkedHashMap<String, Object>> dailyAvailabilities) {

		for(LinkedHashMap<String, Object> dailyAvailability : dailyAvailabilities){

			LocalTime startTime = LocalTime.parse((CharSequence) dailyAvailability.get("startTime"), timeFormatter);
			LocalTime endTime = LocalTime.parse((CharSequence) dailyAvailability.get("endTime"), timeFormatter);
			timeIntervals.add(new TimeInterval(startTime, endTime));
		}
	}

	/**
	 * Construct the resource types 
	 */
	private void constructResourceTypes(
			List<LinkedHashMap<String, Object>> resourceTypes,
			ResourceExpert resourceExpert) {

		for(LinkedHashMap<String, Object> resourceType : resourceTypes){

			String name = (String) resourceType.get("name");

			ResourceTypeBuilder builder = resourceExpert.resourceTypeBuilder(name);
			List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

			if (resourceType.get("requires") != null) {
				ArrayList<Integer> requiredResourceTypes = (ArrayList<Integer>) resourceType.get("requires");
				for (Integer resourceTypeNr : requiredResourceTypes) {
					builder.addRequiredResourceTypes(resourceTypeList.get(resourceTypeNr - 1));
				}
			}

			if (resourceType.get("conflictsWith") != null) {
				ArrayList<Integer> conflictedResourceTypes = (ArrayList<Integer>) resourceType.get("conflictsWith");
				for (Integer resourceTypeNr : conflictedResourceTypes) {
					builder.addConflictedResourceTypes(resourceTypeList.get(resourceTypeNr - 1));
				}
			}

			if (resourceType.get("dailyAvailability") != null) {
				int dailyAvailability = (int) resourceType.get("dailyAvailability");
				builder.addDailyAvailability(timeIntervals.get(dailyAvailability));
			}

			builder.build();
		}
	}

	/**
	 * Construct the resources 
	 */
	private void constructResources(List<LinkedHashMap<String, Object>> resources,
			ResourceExpert resourceExpert) {

		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		for(LinkedHashMap<String, Object> resource : resources){
			String name = (String) resource.get("name");
			int resourceTypeNumber = (int) (resource.get("type"));
			ResourceType resourceTypeOfResource= resourceTypeList.get(resourceTypeNumber);

			resourceTypeOfResource.createResource(name);
		}
	}

	/**
	 * Constructs the developers
	 */
	private void constructDevelopers(List<LinkedHashMap<String, Object>> developers,
			DeveloperExpert developerExpert) {
		for (LinkedHashMap<String, Object> developer : developers) {
			//get the developer name
			String name = (String) developer.get("name");
			developerExpert.createDeveloper(name);
		}	
	}

	/**
	 * Construct the projects
	 */
	private void constructProjects(
			List<LinkedHashMap<String, Object>> projects,
			ProjectExpert controller) {
		for (LinkedHashMap<String, Object> project : projects) {
			// get all arguments needed for a project: name, description,
			// creation time and due time
			String name = (String) project.get("name");
			String description = (String) project.get("description");
			LocalDateTime creationTime = LocalDateTime.parse(
					(CharSequence) project.get("creationTime"),
					dateTimeFormatter);
			LocalDateTime dueTime = LocalDateTime.parse(
					(CharSequence) project.get("dueTime"), dateTimeFormatter);

			// create a new project object
			controller.createProject(name, description, creationTime, dueTime);
		}
	}

	/**
	 * Constructs the tasks
	 */
	private void constructTasks(List<LinkedHashMap<String, Object>> tasks,
			ProjectExpert projectExpert, ResourceExpert resourceExpert) {
		for (LinkedHashMap<String, Object> task : tasks) {

			// get all arguments needed for a task: project, description,
			// estimated duration and acceptable deviation.
			int projectNumber = (int) (task.get("project"));
			String description = (String) (task.get("description"));
			Duration estimatedDuration = Duration.ofHours((long) (int) task.get("estimatedDuration"));
			double acceptableDeviation = (double) ((int) (task.get("acceptableDeviation")));
			acceptableDeviation /= 100;

			Project projectOfTask = projectExpert.getAllProjects().get(projectNumber);

			TaskBuilder builder = projectOfTask.taskBuilder(description, estimatedDuration, acceptableDeviation);

			//add dependencies if there are any
			if (task.get("prerequisiteTasks") != null) {
				ArrayList<Integer> prerequisiteTasks = (ArrayList<Integer>) task.get("prerequisiteTasks");
				for (Integer taskNr : prerequisiteTasks) {
					builder.addDependencies(projectOfTask.getAllTasks().get(taskNr - 1));				
				}
			}

			//add alternative task if there is any
			if (task.get("alternativeFor") != null) {
				int alternativeTaskNr = (int) task.get("alternativeFor");
				builder.setOriginalTask(projectOfTask.getAllTasks().get(alternativeTaskNr - 1));
			}
			
			//add required resource types
			if (task.get("requiredTypes") != null){
				List<ResourceType> resourceTypeList =  new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());
				for (LinkedHashMap<String, Object> pair : (List<LinkedHashMap<String, Object>>) task.get("requiredTypes")) {
					builder.addRequiredResourceType(resourceTypeList.get((int) pair.get("type")), (int) pair.get("quantity"));
				}
			}

			//build the new task
			builder.build();
			Task newTask = projectOfTask.getAllTasks().get(projectOfTask.getAllTasks().size() - 1);

			// if status is failed or finished, update the status and set start
			// en end time
			if (task.get("status") != null) {
				String status = (String) task.get("status");
				if(!status.equals("executing")){
					LocalDateTime startTime = LocalDateTime.parse((CharSequence) task.get("startTime"),dateTimeFormatter);
					LocalDateTime endTime = LocalDateTime.parse((CharSequence) task.get("endTime"), dateTimeFormatter);
					if (status.equals("failed")) {
						newTask.updateStatus(startTime, endTime, true);
					} else {
						newTask.updateStatus(startTime, endTime, false);
					}
				}
				//TODO new status: executing

			}
		}
	}

	/**
	 * Constructs the projects
	 */
	private void constructPlannings(List<LinkedHashMap<String, Object>> plannings,TaskManController controller) {	
		for(LinkedHashMap<String, Object> planning : plannings){

		}
	}
}
