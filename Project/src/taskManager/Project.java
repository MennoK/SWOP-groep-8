package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * a project consists of multiple tasks required to complete the project. A
 * project has a name, a description, a creation time, and a due time by which
 * it should be finished. The system should help the user to manage 8his
 * projects, meaning that a project should be marked as either ongoing or
 * finished. A project can only be finished if it has at least one task, and all
 * its tasks are finished (or each failed task has a finished alternative).
 * Additionally, for ongoing projects, the system should indicate whether the
 * project is estimated to finish on time or over time (based on the time spans
 * of the finished tasks, the estimated duration of the unfinished tasks
 * ignoring the acceptable deviation and parallelizing as mush as possible, and
 * using a Monday to Friday working week with 8 hours a day). For finished
 * projects, the system should indicate the total delay that occurred within the
 * tasks of the project, and whether the project finished on time or not (based
 * on the time spans of the tasks within the project).
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

	private LocalDateTime lastUpdateTime;

	/**
	 * Constructor of the Project class: Sets a new list of tasks
	 * 
	 * @param name
	 *            : name of the project
	 * @param description
	 *            : description of the project
	 * @param creationTime
	 *            : creation time of the project
	 * @param dueTime
	 *            : due time of the project
	 */
	Project(String name, String description, LocalDateTime creationTime,
			LocalDateTime dueTime) {
		setName(name);
		setDescription(description);
		this.creationTime = creationTime;
		setDueTime(dueTime);
		this.tasks = new ArrayList<Task>();
		this.update(creationTime);
	}

	/**
	 * Creates a new task no dependencies or alternative task to the project and
	 * will add the task to the tasklist of the project.
	 * 
	 * @param description
	 *            : description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable deviation of a task
	 */
	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation) {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, this.lastUpdateTime);
		this.addTask(task);
	}

	/**
	 * Creates a new task with alternative task to the project and will add the
	 * task to the tasklist of the project.
	 * 
	 * @param description
	 *            : description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable deviation of a task
	 * @param alternativeTask
	 *            : The alternative task
	 */
	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation, Task isAlternativeForTask) {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, this.lastUpdateTime, isAlternativeForTask);

		updateDependencies(task, isAlternativeForTask);
		this.addTask(task);
	}

	/**
	 * Creates a new task with dependencies to the project and will add the task
	 * to the tasklist of the project
	 * 
	 * @param description
	 *            : description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable deviation of a task
	 * @param dependencies
	 *            : list with dependencies
	 */
	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation, ArrayList<Task> dependencies) {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, this.lastUpdateTime, dependencies);
		this.addTask(task);
	}

	/**
	 * Creates a new task with alternative task and dependencies to the project
	 * and will add the task to the tasklist of the project
	 * 
	 * @param description
	 *            : description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable deviation of a task
	 * @param alternativeTask
	 *            : The alternative task
	 * @param dependencies
	 *            : list with dependencies
	 */
	public void createTask(String description, Duration estimatedDuration,
			double acceptableDeviation, Task isAlternativeForTask,
			ArrayList<Task> dependencies) {
		Task task = new Task(description, estimatedDuration,
				acceptableDeviation, this.lastUpdateTime, isAlternativeForTask,
				dependencies);
		updateDependencies(task, isAlternativeForTask);
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
	void addTask(Task task) {
		if (!canHaveTask(task)) {
			throw new IllegalArgumentException(
					"The given task is already in this project.");
		} else {
			this.tasks.add(task);
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
	 * checks all the dependencies of all the tasks and replaces the old, failed
	 * task with a new the alternative one.
	 * 
	 * @param alternativeTask
	 * @param isAlternativeForTask
	 */
	private void updateDependencies(Task alternativeTask,
			Task isAlternativeForTask) {
		List<Task> taskList = this.getAllTasks();
		List<Task> dependecyList;
		for (Task task : taskList) {
			dependecyList = task.getDependencies();
			for (Task dependency : dependecyList) {
				if (dependency == isAlternativeForTask) {
					task.addDependency(alternativeTask);
					dependecyList.remove(dependency);
				}
			}
		}

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
	 * sets a description for a project
	 * 
	 * @param description
	 *            : the given description
	 */
	private void setDescription(String description) {
		this.description = description;
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

	/**
	 * Returns whether the project finished on time or not.
	 * 
	 * @return ON_TIME or OVER_TIME depending whether the project finished on
	 *         time or not.
	 * @throws IllegalStateException
	 *             if the project is not yet finished
	 */
	public ProjectFinishingStatus finishedOnTime() throws IllegalStateException {
		if (!this.hasFinished()) {
			if (this.getEstimatedFinishTime().isBefore(this.getDueTime())) {
				return ProjectFinishingStatus.ON_TIME;
			}
			return ProjectFinishingStatus.OVER_TIME;
		} else {
			if (this.getEstimatedFinishTime().isAfter(this.getDueTime())) {
				return ProjectFinishingStatus.OVER_TIME;
			} else {
				return ProjectFinishingStatus.ON_TIME;
			}
		}
	}

	/**
	 * Updates the state of the object and it's tasks with state =
	 * lastupdatetime
	 * 
	 * @param time
	 *            the current time
	 */
	void update(LocalDateTime time) {
		for (Task task : this.getAllTasks()) {
			task.update(time);
		}
		this.lastUpdateTime = time;
	}

	/**
	 * Estimates the finish time by calculating the estimated finished time of
	 * each task
	 * 
	 * @return estimatedFinishTime : the estimated finish time
	 * 
	 */
	LocalDateTime getEstimatedFinishTime() {
		LocalDateTime estimatedFinishTime = this.creationTime;
		for (Task task : getAllTasks()) {
			LocalDateTime taskFinishTime = task.getEstimatedFinishTime();
			if (taskFinishTime.isAfter(estimatedFinishTime)) {
				estimatedFinishTime = task.getEstimatedFinishTime();
			}
		}
		if(this.getStatus() != ProjectStatus.FINISHED && this.lastUpdateTime.isAfter(estimatedFinishTime)){
			estimatedFinishTime = this.lastUpdateTime;
		}
		return estimatedFinishTime;
	}

	/**
	 * Returns the currently expected delay of the project
	 * 
	 * @return the number of hours of delay expected
	 * 
	 * @throws IllegalStateException
	 *             if the project is finished or scheduled to finish on time
	 */
	public Duration getCurrentDelay() {

		if (finishedOnTime() == ProjectFinishingStatus.ON_TIME)
			throw new IllegalStateException(
					"Can not ask the current delay of a task which is expected to finish on time");

		Duration currentDelay = Duration.ofHours(0);

		for (Task task : getAllTasks()) {
			if (task.getEstimatedFinishTime().isAfter(getDueTime())) {
				currentDelay = WorkTime.durationBetween(getDueTime(),
						task.getEstimatedFinishTime());
			}
		}
		return currentDelay;
	}

	/**
	 * Returns the status of a project
	 * 
	 * @return ONGOING: if not all tasks are finished
	 * @return FINISHED: if all tasks are finished
	 */
	public ProjectStatus getStatus() {
		if (hasFinished()) {
			return ProjectStatus.FINISHED;
		} else {
			return ProjectStatus.ONGOING;
		}
	}

	/**
	 * Returns true if and only if all tasks of the project are finished. It
	 * returns false if a task is unavailable or not yet available.
	 * 
	 * If a project does not have any tasks, the project is not finished.
	 * 
	 * @return true if and only if all tasks are finished
	 */
	private boolean hasFinished() {
		if (this.getAllTasks().size() != 0) {
			for (Task task : this.getAllTasks()) {
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
	 * Returns the list of tasks of the project
	 * 
	 * @return list of tasks
	 */
	public List<Task> getAllTasks() {
		return Collections.unmodifiableList(tasks);
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
	 * returns the creation time of a project
	 * 
	 * @return creationTime of a project
	 */
	public LocalDateTime getCreationTime() {
		return creationTime;
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
	 * Returns the latest update time
	 * 
	 * @return lastUpdateTime: latest update time
	 */
	public LocalDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

}
