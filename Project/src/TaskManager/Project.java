package TaskManager;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * This class ..
 * 
 * @author groep 8
 *
 */

public class Project {
	
	private ArrayList<Task> tasks;
	private String name;
	private String description;
	private final LocalDate creationTime;
	private LocalDate dueTime;

	/**
	 * Constructor of the Project class
	 * 
	 * @param name : name of the project
	 * @param description : description of the project
	 * @param creationTime : creation time of the project (only the date needed)
	 * @param dueTime : due time of the project (only the date needed)
	 */
	//TODO Welke parameters moeten final zijn?
	public Project(String name,String description, LocalDate creationTime, LocalDate dueTime){
		setName(name);
		setDescription(description);
		this.creationTime = creationTime;
		setDueTime(dueTime);
		this.tasks = new ArrayList<Task>();
	}
	
	/**
	 * Creates a new task to the project and will add the task
	 * to the tasklist of the project
	 * 
	 * @param description
	 * @param estimatedDuration
	 * @param acceptableDeviation
	 */
	public void createTask(String description, Duration estimatedDuration, double acceptableDeviation){
		Task task = new Task(description, estimatedDuration, acceptableDeviation);
		this.addTask(task);
	}

	/**
	 * This method adds a given task to a project
	 * 
	 * @param task : task to add to project
	 * @throws IllegalArgumentException //TODO hoe werkt dit?
	 */
	public void addTask(Task task) throws IllegalArgumentException {
		if(!canHaveTask(task)){
			throw new IllegalArgumentException("The given task is already in this project.");
		}
		else {
			this.getAllTasks().add(task);
		}
	}
	

	/**
	 * This method checks if a project can have a given task. It returns
	 * true if and only if the project does not contain the task yet and 
	 * the task is not null
	 * 
	 * @param task: given task to be added
	 * @return true if and only if the given task is not null and the task is not already in the project
	 */
	private boolean canHaveTask(Task task){
		return (!getAllTasks().contains(task) && task != null);
	}

	/**
	 * Returns the list of tasks of the project
	 * 
	 * @return list of tasks
	 */
	public List<Task> getAllTasks() {
		return tasks;
	}
	
	/**
	 * Returns true if and only if all tasks of the project are finished. It returns
	 * false if a task is failed or not yet available. 
	 * 
	 * @return true if and only if all tasks are finished
	 */
	//TODO Commentaar geentasks = finished
	private boolean hasFinished(){
		for(Task task: getAllTasks()){
			if(task.getStatus() == TaskStatus.UNAVAILABLE || task.getStatus() == TaskStatus.AVAILABLE){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the status of a project
	 * 
	 * @return 	ONGOING: if not all tasks are finished
	 * 		 	FINISHED: if all tasks are finished
	 */
	public ProjectStatus getStatus() {
		if(hasFinished()){
			return ProjectStatus.FINISHED;
		}
		else{
			return ProjectStatus.ONGOING;
		}
	}

	public Instant getEstimatedFinishTime(Instant now) {
		return null;
	}

	public Instant getTotalDelay(Instant now) {
		return null;
	}
	
	/**
	 * Sets the name of a project
	 * 
	 * @param name : the given name of a project
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name of a project
	 * 
	 * @return name of a project
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * returns the description of a project
	 * 
	 * @return description of a project
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * sets a description for a project
	 * 
	 * @param description : the given description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * returns the due time of project 
	 * 
	 * @return dueTime of a project
	 */
	public LocalDate getDueTime() {
		return dueTime;
	}
	
	/**
	 * This method sets the due time of project
	 * 
	 * @param dueTime: given due time of a project
	 * @throws IllegalArgumentException //TODO hoe moet dit? 
	 */
	public void setDueTime(LocalDate dueTime) throws IllegalArgumentException{
		if(!canHaveDueTime(dueTime)){
			throw new IllegalArgumentException("The given due time is not valid.");
		}
		this.dueTime = dueTime;
	}
	
	/**
	 * Checks if the given due time is valid. It returns true if and 
	 * only if the given due time is after the creation time of the project
	 *
	 * @return true if and only if the due time is after the creation time
	 */
	//TODO Commentaar
	private boolean canHaveDueTime(LocalDate dueTime){
		return dueTime.isAfter(getCreationTime()) || dueTime.isEqual(getCreationTime());
	}

	/**
	 * returns the creation time of a project
	 * 
	 * @return creationTime of a project
	 */
	public LocalDate getCreationTime() {
		return creationTime;
	}
	
/*	*//**
	 * sets the creation time of a project
	 * 
	 * @param creationTime : given creation time of a project
	 *//*
	public void setCreationTime(LocalDate creationTime){
		this.creationTime = creationTime;
	}*/
}
