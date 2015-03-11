package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import taskManager.exception.LoopingDependencyException;

/**
 * The Project class describes a project in system. Every project has the same
 * details: name, description, creation time and a due time. A project contains
 * a list of his tasks and is allowed to create new tasks. Projects can be
 * finished or ongoing.
 * 
 * @author groep 8
 *
 */

public class Project {

	private ArrayList<Task> tasks;
	private String name;
	private String description;
	private final LocalDateTime creationTime;
	private LocalDateTime dueTime;
	private LocalDateTime estimatedFinishTime;

	/**
	 * Constructor of the Project class: Sets a new list of tasks
	 * 
	 * @param name
	 *            : name of the project
	 * @param description
	 *            : description of the project
	 * @param creationTime
	 *            : creation time of the project (only the date needed)
	 * @param dueTime
	 *            : due time of the project (only the date needed)
	 */
	// TODO Welke parameters moeten final zijn?
	public Project(String name, String description, LocalDateTime creationTime,
			LocalDateTime dueTime) {
		setName(name);
		setDescription(description);
		this.creationTime = creationTime;
		setDueTime(dueTime);
		this.tasks = new ArrayList<Task>();
		this.estimatedFinishTime = this.creationTime;
	}

	/**
	 * Creates a new task to the project and will add the task to the tasklist
	 * of the project
	 * 
	 * @param description
	 * @param estimatedDuration
	 * @param acceptableDeviation
	 */
	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation) {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation);
		this.addTask(task);
	}

	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation, Task alernativeTask) {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, alernativeTask);
		this.addTask(task);
	}

	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation, ArrayList<Task> dependencies)
			throws LoopingDependencyException {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, dependencies);
		this.addTask(task);
	}

	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation, Task alernativeTask,
			ArrayList<Task> dependencies) throws LoopingDependencyException {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, alernativeTask, dependencies);
		this.addTask(task);
	}

	/**
	 * This method adds a given task to a project
	 * 
	 * @param task
	 *            : task to add to project
	 * @throws IllegalArgumentException
	 *             : thrown when the given task is not valid
	 */
	void addTask(Task task) throws IllegalArgumentException {
		if (!canHaveTask(task)) {
			throw new IllegalArgumentException(
					"The given task is already in this project.");
		} else {
			this.getAllTasks().add(task);
		}
	}

	/**
	 * This method checks if a project can have a given task. It returns true if
	 * and only if the project does not contain the task yet and the task is not
	 * null
	 * 
	 * @param task
	 *            : given task to be added
	 * @return true if and only if the given task is not null and the task is
	 *         not already in the project
	 */
	private boolean canHaveTask(Task task) {
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
	 * Returns true if and only if all tasks of the project are finished. It
	 * returns false if a task is unavailable or not yet available.
	 * 
	 * If a project does not have any tasks, the project has finished as well.
	 * 
	 * @return true if and only if all tasks are finished
	 */
	// TODO commentaar
	private boolean hasFinished() {
		if (getAllTasks().size() != 0) {
			for (Task task : getAllTasks()) {
				if (task.getStatus() == TaskStatus.UNAVAILABLE
						|| task.getStatus() == TaskStatus.AVAILABLE) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the status of a project
	 * 
	 * @return ONGOING: if not all tasks are finished FINISHED: if all tasks are
	 *         finished
	 */
	public ProjectStatus getStatus() {
		if (hasFinished()) {
			return ProjectStatus.FINISHED;
		} else {
			return ProjectStatus.ONGOING;
		}
	}

	// TODO methode testen + documentatie
	public LocalDateTime getEstimatedFinishTime() {
		return this.estimatedFinishTime;
	}

	// TODO methode testen + documentatie
	public Duration getTotalDelay() {
		List<Task> allTasks = this.getAllTasks();
		for (Task task : allTasks) {
			if (task.getStatus() == TaskStatus.FINISHED
					|| task.getStatus() == TaskStatus.FAILED) {
			}

		}
		return null;
	}

	/**
	 * Sets the name of a project
	 * 
	 * @param name
	 *            : the given name of a project
	 */
	private void setName(String name) {
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
	 * @param description
	 *            : the given description
	 */
	private void setDescription(String description) {
		this.description = description;
	}

	/**
	 * returns the due time of project
	 * 
	 * @return dueTime of a project
	 */
	public LocalDateTime getDueTime() {
		return dueTime;
	}

	/**
	 * This method sets the due time of project
	 * 
	 * @param dueTime
	 *            : given due time of a project
	 * @throws IllegalArgumentException
	 *             : thrown when the given due time is not valid
	 */
	void setDueTime(LocalDateTime dueTime) throws IllegalArgumentException {
		if (!canHaveDueTime(dueTime)) {
			throw new IllegalArgumentException(
					"The given due time is not valid.");
		}
		this.dueTime = dueTime;
	}

	/**
	 * Determines if the given due time is valid. It returns true if and only if
	 * the given due time is after the creation time of the project or is equal
	 * to the creation time
	 *
	 * @return true if and only if the due time is after the creation time or is
	 *         equal to the creation time
	 */
	private boolean canHaveDueTime(LocalDateTime dueTime) {
		return dueTime.isAfter(getCreationTime())
				|| dueTime.isEqual(getCreationTime());
	}

	public ProjectFinishingStatus willFinishOnTime() {
		if (this.getEstimatedFinishTime().isBefore(this.getDueTime()))
			return ProjectFinishingStatus.ON_TIME;
		return ProjectFinishingStatus.OVER_TIME;
	}

	void update(LocalDateTime time) {
		this.updateEstimatedFinishTime(time);
		for (Task task : this.getAllTasks()) {
			task.updateStatus();
		}
	}

	void updateEstimatedFinishTime(LocalDateTime time) {
		LocalDateTime estimatedFinishTime = time;
		for (Task task : getAllTasks()) {
			if (task.getEstimatedFinishTime(time).isAfter(estimatedFinishTime)) {
				estimatedFinishTime = task.getEstimatedFinishTime(time);
			}
		}
		this.estimatedFinishTime = estimatedFinishTime;
	}

	/**
	 * returns the creation time of a project
	 * 
	 * @return creationTime of a project
	 */
	public LocalDateTime getCreationTime() {
		return creationTime;
	}

}
