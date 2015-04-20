package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import utility.Summarizable;

/**
 * A task is a unit of work that can be performed by a user of the system. A
 * task is assigned to an unfinished project upon creation. Each task has a
 * description, an estimated duration expressed in hours and minutes (e.g. 4h
 * 30m), and an acceptable deviation, expressed as a percentage (e.g. 10%). The
 * estimated duration represents the estimated time it should take to complete
 * the task, and the acceptable deviation the time period within which the task
 * can be finished on time. A task can be finished early (even before the
 * earliest acceptable deviation from the estimated duration), on time (within
 * the acceptable deviation) or with a delay (even later than the latest
 * acceptable deviation). A task can depend on other tasks, meaning that they
 * have to be finished before the execution of the dependent task can start.
 * Naturally, no loops are allowed in the dependency graph. Additionally, when a
 * task fails (see Status), an alternative task can be created to replace the
 * current task. For example, if the task to deploy apache is marked as failed,
 * the user may create an alternative task deploy nginx. This alternative task
 * replaces the failed task with respect to dependency management or determining
 * the project status (ongoing or finished). The time spent on the failed task
 * is however counted for the total execution time of the project.
 * 
 * @author Groep 8
 */
public class Task implements Summarizable {

	private String description;
	private Duration estimatedDuration;
	private double acceptableDeviation;

	private List<Task> dependencies = new ArrayList<>();
	private Map<ResourceType, Integer> requiredResourceTypes = new LinkedHashMap<ResourceType, Integer>();
	private Task originalTask;
	private boolean failed = false;

	private LocalDateTime endTime;
	private LocalDateTime startTime;
	private LocalDateTime lastUpdateTime;

	private TaskMemento memento;

	private Planning planning;

	private static AtomicInteger idCounter = new AtomicInteger(1);
	private int id;

	/**
	 * The TaskBuilder is an inner class builder for constructing new tasks. The
	 * description, estimated duration and acceptable deviation of a task are
	 * required parameters. The optional parameters for a task are the original
	 * task, dependencies and required resource types.
	 */
	public static class TaskBuilder {

		private String description;
		private Duration estimatedDuration;
		private double acceptableDeviation;
		private Project project;
		private LocalDateTime now;
		private Task originalTask = null;

		private List<Task> dependencies = new ArrayList<Task>();
		private Map<ResourceType, Integer> requiredResourceTypes = new LinkedHashMap<ResourceType, Integer>();

		/**
		 * Creates a TaskBuilder with the required information for the creation
		 * of a Task
		 * 
		 * @param description
		 *            : description of a task
		 * @param estimatedDuration
		 *            : estimated duration of task
		 * @param acceptableDeviation
		 *            : acceptable deviation of a task
		 */
		public TaskBuilder(String description, Duration estimatedDuration,
				double acceptableDeviation, Project project) {
			this.description = description;
			this.estimatedDuration = estimatedDuration;
			this.acceptableDeviation = acceptableDeviation;
			this.project = project;
			this.now = project.getLastUpdateTime();
		}

		/**
		 * If the Task being build is the alternative Task for some other Task
		 * which failed then use this to specify the original task.
		 * 
		 * @param originalTask
		 *            : the failed Task
		 * @return This TaskBuilder
		 */
		public TaskBuilder setOriginalTask(Task originalTask) {
			this.originalTask = originalTask;
			return this;
		}

		/**
		 * If the Task being build has dependencies, then add them one at a
		 * time.
		 */
		public TaskBuilder addDependencies(Task dependency) {
			this.dependencies.add(dependency);
			return this;
		}

		/**
		 * If the Task being build has required resource types, then add them
		 * one at a time with their quantity.
		 */
		public TaskBuilder addRequiredResourceType(
				ResourceType requiredResourceType, int quantity) {
			this.requiredResourceTypes.put(requiredResourceType, quantity);
			return this;
		}

		/**
		 * Build a Task after all the optional values have been set.
		 */
		public Task build() {
			Task task = new Task(this);
			project.updateDependencies(task, originalTask);
			project.addTask(task);
			return task;
		}
	}

	/**
	 * The constructor of task has a task builder as argument. The task builder
	 * contains all the required parameters and possible optional parameters
	 * 
	 * @param taskBuilder
	 *            : task builder with parameters
	 */
	public Task(TaskBuilder taskBuilder) {
		if ((taskBuilder.dependencies != null || !taskBuilder.dependencies
				.isEmpty()) && taskBuilder.originalTask != null) {
			if (taskBuilder.dependencies.contains(taskBuilder.originalTask))
				throw new IllegalArgumentException(
						"Can not create an alternative task which is dependent"
								+ " on the task it is an alternative for");
			for (Task dep : taskBuilder.dependencies)
				if (dep.hasDependency(taskBuilder.originalTask))
					throw new IllegalArgumentException(
							"Can not create an alternative task which is indirectly dependent"
									+ " on the task it is an alternative for");
		}

		addMultipleDependencies(taskBuilder.dependencies);
		addMultipleResourceTypes(taskBuilder.requiredResourceTypes);

		// null means no original task
		if (taskBuilder.originalTask != null) {
			setAlternativeTask(taskBuilder.originalTask);
		}

		setDescription(taskBuilder.description);
		setEstimatedDuration(taskBuilder.estimatedDuration);
		setAcceptableDeviation(taskBuilder.acceptableDeviation);
		this.id = idCounter.getAndIncrement();

		handleTimeChange(taskBuilder.now);
	}

	private LocalDateTime add(LocalDateTime baseTime, Duration duration) {
		return WorkTime.getFinishTime(baseTime, duration);
	}

	/**
	 * Checks whether a task has a dependency tasks
	 * 
	 * @param task
	 *            : dependent task
	 * @return true if the task has the given task as dependency
	 */
	boolean hasDependency(Task task) {
		if (getDependencies().contains(task))
			return true;
		for (Task dependency : getDependencies())
			if (dependency.hasDependency(task))
				return true;
		return false;
	}

	/**
	 * Adds a list of dependencies to task. The dependent tasks may not be
	 * already in the dependency list of the task
	 * 
	 * @param dependencies2
	 *            : list with dependency task
	 * @throws LoopingDependencyException
	 *             : thrown when a loop occurs
	 */
	private void addMultipleDependencies(List<Task> dependencies2) {
		for (Task dependency : dependencies2) {
			if (!isValidDependency(dependency)) {
				throw new IllegalArgumentException(
						"The given dependency task is already dependent on this task");
			} else {
				addDependency(dependency);

			}
		}
	}

	/**
	 * Adds a given task to the dependency list of the task
	 * 
	 * @param dependency
	 *            : task
	 * @throws LoopingDependencyException
	 *             : thrown when a loop occurs
	 */
	void addDependency(Task dependency) {
		if (dependency.hasDependency(this))
			throw new IllegalArgumentException(
					"Tried to create a dependency loop.");
		if (!isValidDependency(dependency)) {
			throw new IllegalArgumentException(
					"The given dependency task is already dependent on this task");
		} else {
			dependencies.add(dependency);
		}
	}

	/**
	 * Adds a map of required resource types to task. The required resource type
	 * may not be already in the map of resource type of the task
	 * 
	 * @param requiredResourceTypes
	 *            : map with required resource type
	 */
	private void addMultipleResourceTypes(
			Map<ResourceType, Integer> requiredResourceTypes) {
		for (Map.Entry<ResourceType, Integer> entry : requiredResourceTypes
				.entrySet()) {
			addResourceType(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds a given resource type and the needed quantity to the map of required
	 * resource types of the task
	 * 
	 * @param requiredResourceType
	 *            : required resource type
	 */
	void addResourceType(ResourceType requiredResourceType, int quantity) {
		if (!isValidResourceType(requiredResourceType)) {
			throw new IllegalArgumentException(
					"The given resource type is already required by this task");
		}
		if (quantity < 1) {
			throw new IllegalArgumentException(
					"The quantity must be strictly positive");
		}
		if (requiredResourceType.getAllResources().size() < quantity) {
			throw new IllegalArgumentException(
					"The amount of resources of the given resource type does not exist");
		} else {
			requiredResourceTypes.put(requiredResourceType, quantity);
		}
	}

	/**
	 * This method returns true if and only if the given resource type is not
	 * yet in the map of required resource types
	 * 
	 * @param required
	 *            resource type
	 * @return true if and only if the map of required resource type does not
	 *         contain the given resource type.
	 */
	private boolean isValidResourceType(ResourceType requiredResourceType) {
		return !this.getRequiredResourceTypes().containsKey(
				requiredResourceType);
	}

	/**
	 * Checks whether the task has finished early or not. This occurs only if
	 * the end time of the task is before the esimated duration minus the
	 * acceptable deviation
	 * 
	 * @return true if and only if the task was finished early
	 */
	private boolean wasFinishedEarly() {
		long hours = (long) ((int) getEstimatedDuration().toHours() - (int) getEstimatedDuration()
				.toHours() * getAcceptableDeviation());
		LocalDateTime earlyTime = getStartTime().plusHours(hours);
		return getEndTime().isBefore(earlyTime);
	}

	/**
	 * Checks whether the task has finished with a delay or not. This occurs
	 * only if the end time of the task is past the estimated duration plus the
	 * acceptable deviation
	 * 
	 * @return true if and only if the task was finished on a delay
	 */
	private boolean wasFinishedWithADelay() {
		long hours = (long) ((int) getEstimatedDuration().toHours() + (int) getEstimatedDuration()
				.toHours() * getAcceptableDeviation());
		LocalDateTime delayTime = getStartTime().plusHours(hours);
		return getEndTime().isAfter(delayTime);
	}

	/**
	 * String str = toSummary() + ": "; str += getDescription() + ", "; str +=
	 * getEstimatedDuration().toHours() + " hours, "; str +=
	 * getAcceptableDeviation() * 100 + "% margin"; if
	 * (!getDependencies().isEmpty()) { str += ", depends on {"; for (Task dep :
	 * getDependencies()) str += " task " + dep.getId(); str += " }"; } if
	 * (getOriginal() != null) str += ", alternative for task " +
	 * getOriginal().getId(); if (this.getStatus() == TaskStatus.FINISHED) { str
	 * += ", started " + getStartTime(); str += ", finished " + getEndTime();
	 * str += " (" + getFinishStatus() + ")"; } return str; This method returns
	 * true if and only if the given dependency is not yet in the dependency
	 * list
	 * 
	 * @param dependency
	 * @return true if and only if the given task dependency is valid
	 */
	private boolean isValidDependency(Task dependency) {
		return !this.getDependencies().contains(dependency);
	}

	/**
	 * Sets the description of the task
	 * 
	 * @param description
	 *            : the given description of task
	 */
	private void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the estimated duration of a task by a given argument. The estimated
	 * duration of task has to be strictly positive
	 * 
	 * @param estimatedDuration
	 * @throws IllegalArgumentException
	 *             : thrown when the given estimated duration is not valid
	 */
	private void setEstimatedDuration(Duration estimatedDuration) {
		if (estimatedDuration.toHours() <= 0) {
			throw new IllegalArgumentException(
					"The estimated duration must be strictly positive");
		} else {
			this.estimatedDuration = estimatedDuration;
		}
	}

	/**
	 * Sets the acceptable deviation of task. The acceptable deviation must be
	 * positive or zero
	 * 
	 * @param acceptableDeviation
	 * @throws IllegalArgumentException
	 *             : thrown when the given acceptableDeviation is not valid
	 */
	private void setAcceptableDeviation(double acceptableDeviation) {
		if (acceptableDeviation < 0) {
			throw new IllegalArgumentException(
					"The acceptable deviation must be greater or equal then zero");
		} else {
			this.acceptableDeviation = acceptableDeviation;
		}
	}

	/**
	 * Sets the end time.
	 * 
	 * @param endTime
	 *            : the end time of task
	 */
	private void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * Sets the start time of a task
	 * 
	 * @param startTime
	 *            : the given start time of a task
	 */
	private void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	/**
	 * Checks whether the end time is after the start time
	 * 
	 * @param startTime
	 *            : the startTime of a task
	 * @param endTime
	 *            : the endTime of a task
	 * @return true if and only if the start time is before the endtime
	 */
	private boolean isValidStartTimeAndEndTime(LocalDateTime startTime,
			LocalDateTime endTime) {
		return endTime.isAfter(startTime);
	}

	/**
	 * Sets a failed boolean to true or false
	 * 
	 * @param failed
	 *            : true if failed
	 */
	private void setFailed() {
		this.failed = true;
	}

	/**
	 * Sets the alternative task if and only if the original task is failed
	 * 
	 * @param original
	 *            : original task
	 * @throws IllegalArgumentException
	 *             : thrown when the task is not failed
	 */
	private void setAlternativeTask(Task original)
			throws IllegalArgumentException {
		if (original.getStatus() != TaskStatus.FAILED) {
			throw new IllegalArgumentException(
					"Task cannot be alternative to a task that has not failed");
		}
		this.originalTask = original;
	}

	/**
	 * Returns the last update time
	 * 
	 * @return lastupdatetime
	 */
	LocalDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * observer pattern
	 * 
	 * @param time
	 *            : the new time of the clock
	 */
	public void handleTimeChange(LocalDateTime time) {
		this.lastUpdateTime = time;
	}

	/**
	 * Allows the user to update the status of a Task to finished or failed
	 * 
	 * @param startTime
	 *            : String str = toSummary() + ": "; str += getDescription() +
	 *            ", "; str += getEstimatedDuration().toHours() + " hours, ";
	 *            str += getAcceptableDeviation() * 100 + "% margin"; if
	 *            (!getDependencies().isEmpty()) { str += ", depends on {"; for
	 *            (Task dep : getDependencies()) str += " task " + dep.getId();
	 *            str += " }"; } if (getOriginal() != null) str +=
	 *            ", alternative for task " + getOriginal().getId(); if
	 *            (this.getStatus() == TaskStatus.FINISHED) { str +=
	 *            ", started " + getStartTime(); str += ", finished " +
	 *            getEndTime(); str += " (" + getFinishStatus() + ")"; } return
	 *            str; the time at which the user started working on the task.
	 * @param endTime
	 *            : the time at which the user stopped working on the task.
	 * @param setToFail
	 *            : true if the task failed, false if the task was successfully
	 *            finished.
	 * @throws IllegalArgumentException
	 *             : if the startTime was after the endTime.
	 */
	public void updateStatus(LocalDateTime startTime, LocalDateTime endTime,
			boolean setToFail) {
		if (!isValidStartTimeAndEndTime(startTime, endTime))
			throw new IllegalArgumentException(
					"the given end time is before the start time");

		if (getStatus() == TaskStatus.FAILED)
			throw new IllegalStateException("Can not update failed task");

		if (getStatus() == TaskStatus.FINISHED)
			throw new IllegalStateException("Can not update finished task");

		if (getStatus() == TaskStatus.UNAVAILABLE && !setToFail)
			throw new IllegalStateException(
					"Can not finish an unavailable task");

		if (setToFail) {
			this.setFailed();
		}
		this.setStartTime(startTime);
		this.setEndTime(endTime);
	}

	/**
	 * Gets the estimated finish time of an unfinished task.
	 * 
	 * @return the estimated finish time
	 */
	public LocalDateTime getEstimatedFinishTime() {

		if (this.getEndTime() != null) {
			return this.getEndTime();
		} else {

			if (this.getDependencies().isEmpty()) {
				return add(this.lastUpdateTime, this.estimatedDuration);
			} else {
				// Find last estimated time of the dependencies
				LocalDateTime estimatedTime = this.lastUpdateTime;
				for (Task t : this.getDependencies()) {
					if (t.getEstimatedFinishTime().isAfter(estimatedTime)) {
						estimatedTime = t.getEstimatedFinishTime();
					}
				}
				return add(estimatedTime, this.estimatedDuration);
			}
		}
	}

	/**
	 * Gets the status of task. There are four different statuses for a task:
	 * Available, unavailable, finished or failed.
	 * 
	 * A task is failed when the boolean isFailed is true A task is finished
	 * when the task has an end time The task availability is dependent on the
	 * dependencies of the task
	 * 
	 * @return the status of the task
	 */
	public TaskStatus getStatus() {
		if (isFailed()) {
			return TaskStatus.FAILED;
		} else if (getEndTime() != null) {
			return TaskStatus.FINISHED;
		} else if (!getDependencies().isEmpty()) {
			for (Task dependency : getDependencies()) {
				if (dependency.getStatus() != TaskStatus.FINISHED) {
					return TaskStatus.UNAVAILABLE;
				}
			}
		}
		return TaskStatus.AVAILABLE;
	}

	/**
	 * Returns the TaskFinishedStatus of a task
	 * 
	 * @return taskFinishStatus : status of a finished task
	 * @throws InvalidActivityException
	 *             : thrown when the task is not finished yet
	 */
	public TaskFinishedStatus getFinishStatus() {
		if (this.getStatus() != TaskStatus.FINISHED) {
			throw new IllegalArgumentException("The task is not finished yet");
		} else {
			if (wasFinishedEarly()) {
				return TaskFinishedStatus.EARLY;
			} else if (wasFinishedWithADelay()) {
				return TaskFinishedStatus.WITH_A_DELAY;
			} else {
				return TaskFinishedStatus.ON_TIME;
			}
		}
	}

	/**
	 * Returns a boolean true if the task is failed false if the task is not
	 * failed
	 * 
	 * @return true if and only if the task is failed
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Returns the end time of a project
	 * 
	 * @return endTime : the endTime of a project
	 */
	public LocalDateTime getEndTime() {
		return endTime;
	}

	/**
	 * Returns the acceptable deviation of a task
	 * 
	 * @return acceptableDeviation: the acceptable deviation of a task
	 * 
	 */
	public double getAcceptableDeviation() {
		return acceptableDeviation;
	}

	/**
	 * returns the estimated duration of a task
	 * 
	 * @return estimatedDuration: estimated duration of a task
	 */
	public Duration getEstimatedDuration() {
		return estimatedDuration;
	}

	/**
	 * Returns the start time of task
	 * 
	 * @return startTime : the start time of a task
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * Returns the list with dependencies of the task
	 * 
	 * @return dependencies: list with dependencies
	 */
	public List<Task> getDependencies() {
		return Collections.unmodifiableList(dependencies);
	}

	/**
	 * Returns the map with required resource type and their quantity of the
	 * task
	 * 
	 * @return requiredResourceTypes: map with required resource type and
	 *         quantity
	 */
	public Map<ResourceType, Integer> getRequiredResourceTypes() {
		return requiredResourceTypes;
	}

	/**
	 * Remove task from the dependency list
	 */
	void removeDependency(Task task) {
		dependencies.remove(task);
	}

	/**
	 * Returns the description of task
	 * 
	 * @return description: the description of a task
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the task which this task is the alternative for
	 * 
	 * @return original: the original task
	 */
	public Task getOriginal() {
		return this.originalTask;
	}

	/**
	 * Returns the id of a task
	 * 
	 * @return id : id of the task
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Returns the estimated duration of a task
	 * 
	 * @return duration: estimated duration of a task
	 */
	public Duration getDuration() {
		return this.estimatedDuration;
	}

	/**
	 * returns the planning of the task, if it has one
	 * 
	 * @return
	 */
	public Planning getPlanning() {
		if (this.hasPlanning()) {
			return planning;
		} else
			throw new NullPointerException("the task has no planning");
	}

	/**
	 * sets a planning for the task
	 * 
	 * @param planning
	 */
	public void setPlanning(Planning planning) {
		this.planning = planning;
	}

	public boolean hasPlanning() {
		return (planning != null);
	}

	/**
	 * Partial toString method
	 * 
	 * @return a summary of the main information defining a Task
	 */
	public String toSummary() {
		return "Task " + getId() + " " + getStatus();
	}

	/**
	 * Full toString method
	 * 
	 * @return a complete description of a Task
	 */
	public String toString() {
		String str = toSummary() + ": ";
		str += getDescription() + ", ";
		str += getEstimatedDuration().toHours() + " hours, ";
		str += getAcceptableDeviation() * 100 + "% margin";
		if (!getDependencies().isEmpty()) {
			str += ", depends on {";
			for (Task dep : getDependencies())
				str += " task " + dep.getId();
			str += " }";
		}
		if (getOriginal() != null)
			str += ", alternative for task " + getOriginal().getId();
		if (this.getStatus() == TaskStatus.FINISHED) {
			str += ", started " + getStartTime();
			str += ", finished " + getEndTime();
			str += " (" + getFinishStatus() + ")";
		}
		return str;
	}

	void save() {
		this.memento = new TaskMemento(this);
	}

	boolean load() {
		if(this.memento == null) {
			return false;
		}
		else {
			this.memento.load(this);
			return true;
		}
	}

	private class TaskMemento {

		private String description;
		private Duration estimatedDuration;
		private double acceptableDeviation;

		private List<Task> dependencies;
		private Map<ResourceType, Integer> requiredResourceTypes;
		private Task originalTask;
		private boolean failed = false;

		private LocalDateTime endTime;
		private LocalDateTime startTime;
		private LocalDateTime lastUpdateTime;

		private Planning planning;

		private int id;

		// Het aanmaken van het memento object
		// Alles moet gekopieerd worden, behalve referenties naar objecten in
		// ons domein
		// bv. de dependencies worden shallow gekopieerd
		public TaskMemento(Task task) {
			this.description = new String(task.description);
			this.estimatedDuration = task.estimatedDuration.plusSeconds(0);
			this.acceptableDeviation = new Double(task.acceptableDeviation);

			this.dependencies = new ArrayList<Task>(task.dependencies);
			this.requiredResourceTypes = new LinkedHashMap<ResourceType, Integer>(
					task.requiredResourceTypes);
			this.originalTask = task.originalTask;
			this.failed = task.failed;

			this.endTime = task.endTime.plusSeconds(0);
			this.startTime = task.startTime.plusSeconds(0);
			this.lastUpdateTime = task.lastUpdateTime.plusSeconds(0);

			this.planning = task.planning;
			this.id = task.id;
		}

		public void load(Task task) {
			task.description = this.description;
			task.estimatedDuration = this.estimatedDuration;
			task.acceptableDeviation = this.acceptableDeviation;

			task.dependencies = this.dependencies;
			task.requiredResourceTypes = this.requiredResourceTypes;
			task.originalTask = this.originalTask;
			task.failed = this.failed;

			task.endTime = this.endTime;
			task.startTime = this.startTime;
			task.lastUpdateTime = this.lastUpdateTime;

			task.planning = this.planning;
			task.id = this.id;
		}
	}

}
