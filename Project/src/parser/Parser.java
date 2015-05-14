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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import taskmanager.Developer;
import taskmanager.Planning;
import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.BranchOffice;
import taskmanager.TaskManController;
import taskmanager.Planning.PlanningBuilder;
import taskmanager.ResourceType.ResourceTypeBuilder;
import taskmanager.Task.TaskBuilder;
import utility.TimeInterval;

/**
 * The Parser class implements a YAML parser for TaskMan We are using the
 * snakeYAML library
 * 
 * @author Groep 8
 */

public class Parser {

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm");
	private DateTimeFormatter timeFormatter = DateTimeFormatter
			.ofPattern("HH:mm");

	private List<TimeInterval> timeIntervals = new ArrayList<TimeInterval>();
	private List<Task> alltasks = new ArrayList<Task>();
	private List<Resource> allresources = new ArrayList<Resource>();

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
	public TaskManController parse(String pathToFile)
			throws FileNotFoundException, RuntimeException {

	/*	// check if the given input file is valid for taskman
		TaskManInitFileChecker checker = new TaskManInitFileChecker(
				new FileReader(pathToFile));
			checker.checkFile();*/

		// create new yaml
		InputStream input = new FileInputStream(new File(pathToFile));
		Yaml yaml = new Yaml();
		Map<String, Object> objects = (Map<String, Object>) yaml.load(input);

		// create system time
		LocalDateTime systemTime = constructSystemTime((CharSequence) objects
				.get("systemTime"));

		//create TaskManController
		TaskManController tmc = new TaskManController(systemTime);

		List<LinkedHashMap<String, Object>> branches = (List<LinkedHashMap<String, Object>>) objects
				.get("branch");

		for(LinkedHashMap<String, Object> branch : branches){
			timeIntervals = new ArrayList<TimeInterval>();
			alltasks = new ArrayList<Task>();
			allresources = new ArrayList<Resource>();

			BranchOffice activeOffice = tmc.createBranchOffice((String) branch.get("location"));

			tmc.logIn(activeOffice);

			// create daily availability
			constructDailyAvailabilities((List<LinkedHashMap<String, Object>>) branch
					.get("dailyAvailability"));

			// create all resource types
			constructResourceTypes(
					(List<LinkedHashMap<String, Object>>) branch
					.get("resourceTypes"),
					tmc,activeOffice);

			// create all resources
			constructResources(
					(List<LinkedHashMap<String, Object>>) branch.get("resources"),
					tmc);

			// create all developers
			constructDevelopers(
					(List<LinkedHashMap<String, Object>>) branch.get("developers"),
					tmc);

			// create all projects
			constructProjects(
					(List<LinkedHashMap<String, Object>>) branch.get("projects"),
					tmc);

			// create all tasks
			constructTasks(
					(List<LinkedHashMap<String, Object>>) branch.get("tasks"),
					(List<LinkedHashMap<String, Object>>) branch.get("plannings"),
					tmc);
		}

		return tmc;
	}

	private LocalDateTime constructSystemTime(CharSequence time) {
		LocalDateTime systemTime = LocalDateTime.parse(time, dateTimeFormatter);
		return systemTime;
	}

	/**
	 * Constructs the daily availabilities
	 */
	private void constructDailyAvailabilities(
			List<LinkedHashMap<String, Object>> dailyAvailabilities) {

		for (LinkedHashMap<String, Object> dailyAvailability : dailyAvailabilities) {

			LocalTime startTime = LocalTime.parse(
					(CharSequence) dailyAvailability.get("startTime"),
					timeFormatter);
			LocalTime endTime = LocalTime.parse(
					(CharSequence) dailyAvailability.get("endTime"),
					timeFormatter);
			timeIntervals.add(new TimeInterval(startTime, endTime));
		}
	}

	/**
	 * Construct the resource types
	 */
	@SuppressWarnings("unchecked")
	private void constructResourceTypes(
			List<LinkedHashMap<String, Object>> resourceTypes,
			TaskManController tmc, BranchOffice activeBranchOffice) {

		for (LinkedHashMap<String, Object> resourceType : resourceTypes) {

			String name = (String) resourceType.get("name");

			ResourceTypeBuilder builder = ResourceType.builder(name);
			List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
					tmc.getAllResourceTypes());

			if (resourceType.get("requires") != null) {
				ArrayList<Integer> requiredResourceTypes = (ArrayList<Integer>) resourceType
						.get("requires");
				for (Integer resourceTypeNr : requiredResourceTypes) {
					builder.addRequiredResourceTypes(resourceTypeList
							.get(resourceTypeNr - 1));
				}
			}

			if (resourceType.get("conflictsWith") != null) {
				ArrayList<Integer> conflictedResourceTypes = (ArrayList<Integer>) resourceType
						.get("conflictsWith");
				for (Integer resourceTypeNr : conflictedResourceTypes) {
					builder.addConflictedResourceTypes(resourceTypeList
							.get(resourceTypeNr - 1));
				}
			}

			if (resourceType.get("dailyAvailability") != null) {
				int dailyAvailability = (int) resourceType
						.get("dailyAvailability");
				builder.addDailyAvailability(timeIntervals
						.get(dailyAvailability));
			}

			builder.build(activeBranchOffice);
		}
	}

	/**
	 * Construct the resources
	 */
	private void constructResources(
			List<LinkedHashMap<String, Object>> resources, TaskManController tmc) {

		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		for (LinkedHashMap<String, Object> resource : resources) {
			String name = (String) resource.get("name");
			int resourceTypeNumber = (int) (resource.get("type"));
			ResourceType resourceTypeOfResource = resourceTypeList
					.get(resourceTypeNumber);

			resourceTypeOfResource.createResource(name);
		}

		for (ResourceType type : tmc.getAllResourceTypes()) {
			for (Resource resource : type.getAllResources()) {
				allresources.add(resource);
			}
		}
	}

	/**
	 * Constructs the developers
	 */
	private void constructDevelopers(
			List<LinkedHashMap<String, Object>> developers,
			TaskManController tmc) {
		for (LinkedHashMap<String, Object> developer : developers) {
			// get the developer name
			String name = (String) developer.get("name");
			tmc.createDeveloper(name);
		}
	}

	/**
	 * Construct the projects
	 */
	private void constructProjects(
			List<LinkedHashMap<String, Object>> projects, TaskManController tmc) {
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
			tmc.createProject(name, description, creationTime, dueTime);
		}
	}

	/**
	 * Constructs the tasks
	 */
	@SuppressWarnings("unchecked")
	private void constructTasks(List<LinkedHashMap<String, Object>> tasks,
			List<LinkedHashMap<String, Object>> plannings,
			TaskManController controller) {

		Set<Integer> taskNrSet = new HashSet<Integer>();
		for (LinkedHashMap<String, Object> planning : plannings) {
			int taskNr = (int) (planning.get("task"));
			taskNrSet.add(taskNr);
		}
		int counter = 0;
		int planningCounter = 0;
		for (LinkedHashMap<String, Object> task : tasks) {
			// get all arguments needed for a task: project, description,
			// estimated duration and acceptable deviation.
			int projectNumber = (int) (task.get("project"));
			String description = (String) (task.get("description"));
			Duration estimatedDuration = Duration.ofHours((long) (int) task
					.get("estimatedDuration"));
			double acceptableDeviation = (double) ((int) (task
					.get("acceptableDeviation")));
			acceptableDeviation /= 100;

			Project projectOfTask = controller.getAllProjects().get(
					projectNumber);

			TaskBuilder builder = Task.builder(description, estimatedDuration,
					acceptableDeviation);

			// add dependencies if there are any
			if (task.get("prerequisiteTasks") != null) {
				ArrayList<Integer> prerequisiteTasks = (ArrayList<Integer>) task
						.get("prerequisiteTasks");
				for (Integer taskNr : prerequisiteTasks) {
					builder.addDependencies(projectOfTask.getAllTasks().get(
							taskNr - 1));
				}
			}

			// add alternative task if there is any
			if (task.get("alternativeFor") != null) {
				int alternativeTaskNr = (int) task.get("alternativeFor");
				builder.setOriginalTask(projectOfTask.getAllTasks().get(
						alternativeTaskNr - 1));
			}

			// add required resource types
			if (task.get("requiredTypes") != null) {
				List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
						controller.getAllResourceTypes());
				for (LinkedHashMap<String, Object> pair : (List<LinkedHashMap<String, Object>>) task
						.get("requiredTypes")) {
					builder.addRequiredResourceType(
							resourceTypeList.get((int) pair.get("type")),
							(int) pair.get("quantity"));
				}
			}

			// build the new task
			builder.build(projectOfTask);
			Task newTask = projectOfTask.getAllTasks().get(
					projectOfTask.getAllTasks().size() - 1);

			alltasks.add(newTask);

			if (taskNrSet.contains(counter)) {
				LinkedHashMap<String, Object> planningOfTask = plannings
						.get(planningCounter);
				constructPlannings(planningOfTask, controller);
				if (task.get("status") != null) {
					String status = (String) task.get("status");
					if (status.equals("executing")) {
						LocalDateTime startTime = LocalDateTime.parse(
								(CharSequence) plannings.get(planningCounter)
								.get("plannedStartTime"),
								dateTimeFormatter);
						controller.setExecuting(newTask, startTime);
					} else {
						LocalDateTime startTime = LocalDateTime.parse(
								(CharSequence) task.get("startTime"),
								dateTimeFormatter);
						LocalDateTime endTime = LocalDateTime.parse(
								(CharSequence) task.get("endTime"),
								dateTimeFormatter);
						if (status.equals("failed")) {
							controller.setExecuting(newTask, startTime);
							controller.setFailed(newTask, endTime);
						} else {
							controller.setExecuting(newTask, startTime);
							controller.setFinished(newTask, endTime);
						}

					}
				}
				planningCounter++;
			}

			counter++;
		}

	}

	/**
	 * Constructs the projects
	 */
	@SuppressWarnings("unchecked")
	private void constructPlannings(LinkedHashMap<String, Object> planning,
			TaskManController controller) {

		LocalDateTime startTime = LocalDateTime.parse(
				(CharSequence) planning.get("plannedStartTime"),
				dateTimeFormatter);

		ArrayList<Integer> developersNr = (ArrayList<Integer>) planning
				.get("developers");
		List<Developer> assignedDevs = new ArrayList<Developer>();

		for (Integer devNr : developersNr) {
			assignedDevs.add(new ArrayList<Developer>(controller
					.getAllDevelopers()).get(devNr));
		}

		int taskNr = (int) (planning.get("task"));
		PlanningBuilder pbuilder = controller.getPlanner().createPlanning(startTime,
				alltasks.get(taskNr), assignedDevs.get(0));

		for (int i = 1; i < assignedDevs.size(); i++) {
			pbuilder.addDeveloper(assignedDevs.get(i));
		}

		if (planning.get("resources") != null) {
			for (LinkedHashMap<String, Object> pair : (List<LinkedHashMap<String, Object>>) planning
					.get("resources")) {
				for (Integer resourceNr : (ArrayList<Integer>) pair
						.get("resource")) {
					pbuilder.addResources(allresources.get(resourceNr));
				}
			}
		}
		pbuilder.build();

	}
}
