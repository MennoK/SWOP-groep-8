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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import taskManager.Developer;
import taskManager.DeveloperExpert;
import taskManager.Planning;
import taskManager.Planning.PlanningBuilder;
import taskManager.Project;
import taskManager.ProjectExpert;
import taskManager.Resource;
import taskManager.ResourceExpert;
import taskManager.ResourceType;
import taskManager.ResourceType.ResourceTypeBuilder;
import taskManager.Task;
import taskManager.Task.TaskBuilder;
import taskManager.TaskManController;
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
	private List<Developer> alldevelopers = new ArrayList<Developer>();

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

		// check if the given input file is valid for taskman
		TaskManInitFileChecker checker = new TaskManInitFileChecker(
				new FileReader(pathToFile));
		checker.checkFile();

		// create new yaml
		InputStream input = new FileInputStream(new File(pathToFile));
		Yaml yaml = new Yaml();
		Map<String, Object> objects = (Map<String, Object>) yaml.load(input);

		// create system time
		TaskManController controller = constructController((CharSequence) objects
				.get("systemTime"));

		// create daily availability
		constructDailyAvailabilities((List<LinkedHashMap<String, Object>>) objects
				.get("dailyAvailability"));

		// create all resource types
		constructResourceTypes(
				(List<LinkedHashMap<String, Object>>) objects
						.get("resourceTypes"),
				controller.getResourceExpert());

		// create all resources
		constructResources(
				(List<LinkedHashMap<String, Object>>) objects.get("resources"),
				controller.getResourceExpert());

		// create all developers
		constructDevelopers(
				(List<LinkedHashMap<String, Object>>) objects.get("developers"),
				controller.getDeveloperExpert());

		// create all projects
		constructProjects(
				(List<LinkedHashMap<String, Object>>) objects.get("projects"),
				controller.getProjectExpert());

		// create all tasks
		constructTasks(
				(List<LinkedHashMap<String, Object>>) objects.get("tasks"),
				(List<LinkedHashMap<String, Object>>) objects.get("plannings"),
				controller.getProjectExpert(), controller.getResourceExpert(),
				controller);

		return controller;
	}

	private TaskManController constructController(CharSequence time) {
		LocalDateTime systemTime = LocalDateTime.parse(time, dateTimeFormatter);
		return new TaskManController(systemTime);
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
			ResourceExpert resourceExpert) {

		for (LinkedHashMap<String, Object> resourceType : resourceTypes) {

			String name = (String) resourceType.get("name");

			ResourceTypeBuilder builder = ResourceType.builder(name);
			List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
					resourceExpert.getAllResourceTypes());

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

			builder.build(resourceExpert);
		}
	}

	/**
	 * Construct the resources
	 */
	private void constructResources(
			List<LinkedHashMap<String, Object>> resources,
			ResourceExpert resourceExpert) {

		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());

		for (LinkedHashMap<String, Object> resource : resources) {
			String name = (String) resource.get("name");
			int resourceTypeNumber = (int) (resource.get("type"));
			ResourceType resourceTypeOfResource = resourceTypeList
					.get(resourceTypeNumber);

			resourceTypeOfResource.createResource(name);
		}

		for (ResourceType type : resourceExpert.getAllResourceTypes()) {
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
			DeveloperExpert developerExpert) {
		for (LinkedHashMap<String, Object> developer : developers) {
			// get the developer name
			String name = (String) developer.get("name");
			developerExpert.createDeveloper(name);
		}
		alldevelopers = new ArrayList<Developer>(
				developerExpert.getAllDevelopers());
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
	@SuppressWarnings("unchecked")
	private void constructTasks(List<LinkedHashMap<String, Object>> tasks,
			List<LinkedHashMap<String, Object>> plannings,
			ProjectExpert projectExpert, ResourceExpert resourceExpert,
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

			Project projectOfTask = projectExpert.getAllProjects().get(
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
						resourceExpert.getAllResourceTypes());
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
			assignedDevs.add(alldevelopers.get(devNr));
		}

		int taskNr = (int) (planning.get("task"));
		PlanningBuilder pbuilder = Planning.builder(startTime,
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
		pbuilder.build(controller.getPlanner());

	}
}
