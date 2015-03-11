package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


import taskManager.*;
import taskManager.exception.InvalidTimeException;
import taskManager.exception.LoopingDependencyException;

/**
 * The Parser class implements a YAML parser for TaskMan
 * 
 * We are using the snakeYAML library 
 * 
 * @author Groep 8
 *
 */

public class Parser {

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	/**
	 * This method parses the input file (needs absolute path) after it has checked if the given file
	 * is in valid format for TaskMan. 
	 * 
	 * @param pathToFile
	 * @param projectController
	 * @throws FileNotFoundException
	 * @throws RuntimeException
	 * @throws LoopingDependencyException 
	 * @throws InvalidTimeException 
	 */
	@SuppressWarnings("unchecked")
	public void parse(String pathToFile, ProjectController projectController) throws FileNotFoundException, RuntimeException, LoopingDependencyException, InvalidTimeException{
		//check if the given input file is valid for taskman
		TaskManInitFileChecker checker = new TaskManInitFileChecker(new FileReader(pathToFile));
		checker.checkFile();

		//create new yaml
		InputStream input = new FileInputStream(new File(pathToFile));
		Yaml yaml = new Yaml();
		Map<String, Object> objects = (Map<String, Object>) yaml.load(input);

		//create all projects given by the input file
		constructProjects((List<LinkedHashMap<String, Object>>) objects.get("projects"), projectController);		

		//create all tasks given by the input file
		constructTasks((List<LinkedHashMap<String, Object>>) objects.get("tasks"), projectController);
	}

	/**
	 * The method will construct the projects given by the input file
	 * 
	 * @param projects : list of all projects
	 * @param controller : the projectController
	 */
	private void constructProjects(List<LinkedHashMap<String, Object>> projects, ProjectController controller){
		for(LinkedHashMap<String, Object> project: projects){
			//get all arguments needed for a project: name, description, creation time and due time
			String name = (String) project.get("name");
			String description = (String) project.get("description");
			LocalDateTime creationTime = LocalDateTime.parse((CharSequence) project.get("creationTime"),dateTimeFormatter);
			LocalDateTime dueTime = LocalDateTime.parse((CharSequence) project.get("dueTime"),dateTimeFormatter);

			//create a new project object
			controller.createProject(name, description, creationTime, dueTime);
		}
	}

	/**
	 * The method will construct the tasks given by the input file
	 * 
	 * @param tasks : list of all projects
	 * @param controller: the projectController
	 * @throws LoopingDependencyException 
	 * @throws InvalidTimeException 
	 */
	private void constructTasks(List<LinkedHashMap<String, Object>> tasks, ProjectController controller) throws LoopingDependencyException, InvalidTimeException{

		for(LinkedHashMap<String, Object> task: tasks){

			//get all arguments needed for a task: project, description, estimated duration and acceptable deviation.
			int projectNumber = (int) (task.get("project"));
			String description = (String) (task.get("description"));
			Duration estimatedDuration = Duration.ofHours((long) (int) task.get("estimatedDuration"));
			double acceptableDeviation = (double) ((int) (task.get("acceptableDeviation")));


			//create a new task to the project
			Project projectOfTask = controller.getAllProjects().get(projectNumber);



			if(task.get("alternativeFor") != null && task.get("prerequisiteTasks") != null){
				int alternativeTaskNr = (int) task.get("alternativeFor");
				Task alternativeTask = projectOfTask.getAllTasks().get(alternativeTaskNr-1);
				ArrayList<Integer> prerequisiteTasks = (ArrayList<Integer>) task.get("prerequisiteTasks");
				ArrayList<Task> dependencyList = new ArrayList<Task>();

				for (Integer taskNr : prerequisiteTasks) {
					dependencyList.add(projectOfTask.getAllTasks().get(taskNr-1));
				}

				projectOfTask.createTask(description, estimatedDuration, acceptableDeviation, alternativeTask, dependencyList);

			}
			//Sets alternative task if the task is an alternative of an other task
			if(task.get("alternativeFor") != null && task.get("prerequisiteTasks") == null){

				int alternativeTaskNr = (int) task.get("alternativeFor");
				Task alternativeTask = projectOfTask.getAllTasks().get(alternativeTaskNr-1);
				projectOfTask.createTask(description, estimatedDuration, acceptableDeviation, alternativeTask);
			}

			//if a task has prequisite tasks, add dependencies to the task
			else if(task.get("prerequisiteTasks") != null && task.get("alternativeFor") == null){

				ArrayList<Integer> prerequisiteTasks = (ArrayList<Integer>) task.get("prerequisiteTasks");
				ArrayList<Task> dependencyList = new ArrayList<Task>();

				for (Integer taskNr : prerequisiteTasks) {
					dependencyList.add(projectOfTask.getAllTasks().get(taskNr-1));
				}
				projectOfTask.createTask(description, estimatedDuration, acceptableDeviation, dependencyList);

			}
			else if(task.get("prerequisiteTasks") == null && task.get("alternativeFor") == null) {
				projectOfTask.createTask(description, estimatedDuration, acceptableDeviation);

			}

			Task newTask = projectOfTask.getAllTasks().get(projectOfTask.getAllTasks().size()-1);

			//if status is failed or finished, update the status and set start en end time
			if(task.get("status") != null){
				String status = (String) task.get("status");
				LocalDateTime startTime = LocalDateTime.parse((CharSequence) task.get("startTime"), dateTimeFormatter);
				LocalDateTime endTime = LocalDateTime.parse((CharSequence) task.get("endTime"), dateTimeFormatter);		
				newTask.setStartTime(startTime);
				newTask.setEndTime(endTime);
				if(status.equals("failed")){
					newTask.setFailed();
				}

				newTask.updateStatus();
			}
		}
	}
}

