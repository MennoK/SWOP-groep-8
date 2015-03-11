package taskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import taskManager.exception.InvalidTimeException;

/**
 * 
 * The projectController class contains a list of all the projects
 * and the internal taskManClock of the system and is able to advance the time. 
 * The project controller is allowed to create new projects.
 * 
 * @author Groep 8
 */

public class ProjectController {

	private ArrayList<Project> projects;
	private TaskManClock taskManClock;

	/**
	 * The constructor of the projectController needs a TaskManClock. 
	 */
	public ProjectController(TaskManClock taskManClock){
		projects = new ArrayList<>();
		this.taskManClock = taskManClock;
	}

	/**
	 * Creates a new project with the given arguments and adds the project
	 * to the list of projects
	 * 
	 * @param name: name of the project
	 * @param description: description of the project
	 * @param creationTime: creation time of the project (only the date needed)
	 * @param dueTime: due time of the project (only the date needed)
	 */
	public void createProject(String name, String description, LocalDateTime creationTime, LocalDateTime dueTime){
		Project project = new Project(name, description, creationTime, dueTime);
		this.addProject(project);
	}

	/**
	 * Creates a new project with the given arguments and adds the project
	 * to the list of projects
	 * The creationTime is set to the current time
	 *
	 * @param name: name of the project
	 * @param description: description of the project
	 * @param dueTime: due time of the project (only the date needed)
	 */
	public void createProject(String name, String description, LocalDateTime dueTime){
		this.createProject(name, description, this.getTime(), dueTime);
	}

	/**
	 * Adds a given project to the list of projects. An IllegalArgumentException
	 * will be thrown when the given project is invalid
	 * 
	 * @param project: project to be added
	 * @throws IllegalArgumentException : thrown when the given project is not valid
	 */
	public void addProject(Project project) throws IllegalArgumentException {
		if(!canHaveProject(project)){
			throw new IllegalArgumentException("The given project is already in this project.");		
		}
		else {
			getAllProjects().add(project);
		}
	}

	/**
	 * Determines if the project controller can have the given project. This is
	 * true if and only if the given project is not yet in the project controller
	 * and the project is not null 
	 *  
	 * @param project: given project to be added
	 * @return true if and only if the project controller does not contain the project yet and the project is not null
	 */
	private boolean canHaveProject(Project project){
		return (!getAllProjects().contains(project) && project != null);
	}

	/**
	 * Returns a list of the projects
	 * 
	 * @return projects: list of projects
	 */
	public List<Project> getAllProjects() {
		return projects;
	}
	
	/**
	 * Returns the time
	 * 
	 * @return LocalDateTime : time
	 */
	public LocalDateTime getTime() {
		return this.taskManClock.getTime();
	}

	/**
	 * 
	 * Advances the time of taskman. This will update the status of every
	 * task in every project of the project controller
	 *  
	 * @param time : new time
	 * @throws InvalidTimeException : thrown when the given time is invalid
	 */
	public void advanceTime(LocalDateTime time) throws InvalidTimeException{
		this.taskManClock.setTime(time);

		for(Project project : this.getAllProjects()){
			project.update(time);
		}
	}
	
}
