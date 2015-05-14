package taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utility.WorkTime;

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
 * ignoring the acceptable deviation and parallelizing as much as possible, and
 * using a Monday to Friday working week with 8 hours a day). For finished
 * projects, the system should indicate the total delay that occurred within the
 * tasks of the project, and whether the project finished on time or not (based
 * on the time spans of the tasks within the project).
 * 
 * @author groep 8
 * 
 */

public class Project implements Visitable {

	//TODO: Set<Task> tasks
	private List<Task> tasks;
	private String name;
	private String description;
	private final LocalDateTime creationTime;
	private LocalDateTime dueTime;
	private LocalDateTime lastUpdateTime;

	private Memento memento;

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
		this.handleTimeChange(creationTime);
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
	 * @param originalTask
	 */
	void updateDependencies(Task alternativeTask, Task originalTask) {
		List<Task> taskList = this.getAllTasks();
		for (Task task : taskList) {
			if (task.getDependencies().contains(originalTask)) {
				task.addDependency(alternativeTask);
				task.removeDependency(originalTask);
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
	private void setDueTime(LocalDateTime dueTime)
			throws IllegalArgumentException {
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
		return dueTime.isAfter(getCreationTime());
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
		if (this.getEstimatedFinishTime().isAfter(this.getDueTime())) {
			return ProjectFinishingStatus.OVER_TIME;
		}
		return ProjectFinishingStatus.ON_TIME;
	}

	/**
	 * Saves the last update time and changes the time in all tasks of the
	 * project
	 * 
	 * @param time
	 *            : the new time of the clock
	 */
	public void handleTimeChange(LocalDateTime time) {
		this.lastUpdateTime = time;
		for (Task t : this.getAllTasks()) {
			t.handleTimeChange(time);
		}
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
		if (this.getStatus() != ProjectStatus.FINISHED
				&& this.lastUpdateTime.isAfter(estimatedFinishTime)) {
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

		if (this.getAllTasks().size() == 0)
			throw new IllegalStateException(
					"A project without tasks can't have a delay");
		if (finishedOnTime() == ProjectFinishingStatus.ON_TIME)
			throw new IllegalStateException(
					"Can not ask the current delay of a task which is expected to finish on time");

		Task latestFinishingTask = this.getAllTasks().get(0);

		for (Task task : getAllTasks()) {
			if (task.getEstimatedFinishTime().isAfter(
					latestFinishingTask.getEstimatedFinishTime())) {
				latestFinishingTask = task;
			}
		}

		return WorkTime.durationBetween(dueTime,
				latestFinishingTask.getEstimatedFinishTime());
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

	/**
	 * accept visitor for visiting this
	 */
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Saves the current state of the project
	 */
	public void save() {
		this.memento = new Memento();
		for (Task task : this.tasks) {
			task.save();
		}
	}

	/**
	 * loads the last saved state of the project
	 * 
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load();
			for (Task task : this.tasks) {
				task.load();
			}
		}
	}

	/**
	 * 
	 * Inner momento class of project
	 * 
	 * @author groep 8
	 */
	private class Memento {
		private List<Task> tasks;
		private String name;
		private String description;
		private LocalDateTime dueTime;

		private LocalDateTime lastUpdateTime;

		/**
		 * Constructor of the momento inner class of project. Initialize all the
		 * parameters of the current state of the given project
		 * 
		 * @param project
		 *            : project
		 */
		public Memento() {
			this.tasks = new ArrayList<Task>(Project.this.tasks);
			this.name = new String(Project.this.name);
			this.description = new String(Project.this.description);
			this.dueTime = Project.this.dueTime;

			this.lastUpdateTime = Project.this.lastUpdateTime;
		}

		/**
		 * 
		 * Sets the parameters of the project to the last saved state of the
		 * project
		 * 
		 * @param project
		 */
		public void load() {
			Project.this.tasks = this.tasks;
			Project.this.name = this.name;
			Project.this.description = this.description;
			Project.this.dueTime = this.dueTime;

			Project.this.lastUpdateTime = this.lastUpdateTime;
		}
	}
}
