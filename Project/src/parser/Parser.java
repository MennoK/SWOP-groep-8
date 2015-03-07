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

import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.Task;

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
	 * This method parses the input file after it has checked if the given file
	 * is in valid format for TaskMan. 
	 * 
	 * @param pathToFile
	 * @param projectController
	 * @throws FileNotFoundException
	 * @throws RuntimeException
	 * @throws LoopingDependencyException 
	 */
	@SuppressWarnings("unchecked")
	public void parse(String pathToFile, ProjectController projectController) throws FileNotFoundException, RuntimeException, LoopingDependencyException{
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
			Project newProject = new Project(name, description, creationTime, dueTime);
			controller.addProject(newProject);
		}
	}

	/**
	 * The method will construct the tasks given by the input file
	 * 
	 * @param tasks : list of all projects
	 * @param controller: the projectController
	 * @throws LoopingDependencyException 
	 */
	private void constructTasks(List<LinkedHashMap<String, Object>> tasks, ProjectController controller) throws LoopingDependencyException{

		for(LinkedHashMap<String, Object> task: tasks){

			//get all arguments needed for a task: project, description, estimated duration and acceptable deviation.
			int projectNumber = (int) (task.get("project"));
			String description = (String) (task.get("description"));
			Duration estimatedDuration = Duration.ofHours((long) (int) task.get("estimatedDuration"));
			double acceptableDeviation = (double) ((int) (task.get("acceptableDeviation")));

			//create a new task to the project
			Task newTask = new Task(description, estimatedDuration, acceptableDeviation);
			Project projectOfTask = controller.getAllProjects().get(projectNumber);
			projectOfTask.addTask(newTask);

			//A task is alternative for a failed task
			if(task.get("alternativeFor") != null){
				int alternativeFor = (int) task.get("alternativeFor");
				//TODO moet nog ge"implementeerd worden in task?
			}

			//if a task has prequisite tasks, add dependencies to the task
			if(task.get("prerequisiteTasks") != null){
				ArrayList<Integer> prerequisiteTasks = (ArrayList<Integer>) task.get("prerequisiteTasks");
				for (int taskNr : prerequisiteTasks) {
					newTask.addDependency(projectOfTask.getAllTasks().get(taskNr));
				}
			}

			//if status is failed or finished, update the status and set start en end time
			if(task.get("status") != null){
				String status = (String) task.get("status");
				LocalDateTime starTime = LocalDateTime.parse((CharSequence) task.get("startTime"), dateTimeFormatter);
				LocalDateTime endTime = LocalDateTime.parse((CharSequence) task.get("endTime"), dateTimeFormatter);
				if(status.equals("finished")){
					
				}
				else{
					
				}
				
			//	newTask.setStartTime(starTime);
			//	newTask.setEndTime(endTime);
				
			}
		}
	}
}
