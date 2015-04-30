package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.activity.InvalidActivityException;

import utility.WorkDay;
import utility.WorkTime;

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
public class Task implements Visitable {

	private String description;
	private Duration estimatedDuration;
	private double acceptableDeviation;
	private TaskStatus status = TaskStatus.UNAVAILABLE;

	private List<Task> dependencies = new ArrayList<>();
	private Map<ResourceType, Integer> requiredResourceTypes = new LinkedHashMap<ResourceType, Integer>();
	private Task originalTask;

	private LocalDateTime endTime;
	private LocalDateTime startTime;
	private LocalDateTime lastUpdateTime;

	private Memento memento;

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
				double acceptableDeviation) {
			this.description = description;
			this.estimatedDuration = estimatedDuration;
			this.acceptableDeviation = acceptableDeviation;
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

			if (((requiredResourceType.getDailyAvailability().getBegin()
					.isAfter(WorkDay.getStartTime()) || requiredResourceType
					.getDailyAvailability().getEnd()
					.isBefore(WorkDay.getEndTime())) && estimatedDuration
					.compareTo(Duration.between(requiredResourceType
							.getDailyAvailability().getBegin(),
							requiredResourceType.getDailyAvailability()
									.getEnd())) > 0)) {
				throw new IllegalArgumentException(
						"The estimated duration of the task is longer then the availablitiy of the resource");

			} else {
				this.requiredResourceTypes.put(requiredResourceType, quantity);
			}
			return this;
		
		}
		
		private boolean checkRequiredResources() {
			for (ResourceType type : requiredResourceTypes.keySet()) {
				if(!type.getRequiredResourceTypes().isEmpty()){
					for (ResourceType requiredResourceType : type.getRequiredResourceTypes()) {
						if(!requiredResourceTypes.keySet().contains(requiredResourceType)){
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * Build a Task after all the optional values have been set. An project
		 * is required to add the created task to.
		 */
		public Task build(Project project) {
			if (checkRequiredResources()) {
				this.now = project.getLastUpdateTime();
				Task task = new Task(this);
				project.updateDependencies(task, originalTask);
				project.addTask(task);
				return task;
			} else {
				throw new IllegalStateException("A resource is requiring an other resource.");
			}
		}

		
	}

	/**
	 * Static method that creates a new taskbuilder. This method returns the
	 * builder after all given required parameters are set.
	 * 
	 * @param description
	 *            : required description of a task
	 * @param estimatedDuration
	 *            : required duration
	 * @param acceptableDeviation
	 *            : required deviation
	 * @return taskBuilder : new taskBuilder
	 */
	public static TaskBuilder builder(String description,
			Duration estimatedDuration, double acceptableDeviation) {
		return new TaskBuilder(description, estimatedDuration,
				acceptableDeviation);
	}

	/**
	 * The constructor of task has a task builder as argument. The task builder
	 * contains all the required parameters and possible optional parameters
	 * 
	 * @param taskBuilder
	 *            : task builder with parameters
	 */
	public Task(TaskBuilder taskBuilder) {
		if ((taskBuilder.dependencies != null && !taskBuilder.dependencies
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
	boolean wasFinishedEarly() {
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
	boolean wasFinishedWithADelay() {
		long hours = (long) ((int) getEstimatedDuration().toHours() + (int) getEstimatedDuration()
				.toHours() * getAcceptableDeviation());
		LocalDateTime delayTime = getStartTime().plusHours(hours);
		return getEndTime().isAfter(delayTime);
	}

	/**
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
	 * Returns the current status of a task
	 * 
	 * @return status : current status
	 */
	public TaskStatus getStatus() {
		return status;
	}

	/**
	 * Sets a new task status to the task
	 * 
	 * @param status
	 *            : given task status
	 */
	void setStatus(TaskStatus status) {
		this.status = status;
	}

	/**
	 * Set the status to Executing
	 * 
	 * @param startTime
	 */
	void setExecuting(LocalDateTime startTime) {
		setStatus(getStatus().goExecuting());
		setStartTime(startTime);
	}

	/**
	 * Set the status to finished
	 * 
	 * @param endTime
	 */
	void setFinished(LocalDateTime endTime) {
		if (endTime.isBefore(getStartTime()))
			throw new IllegalArgumentException(
					"End time can not be before start time");
		setStatus(getStatus().goFinished());
		setEndTime(endTime);
		setEndTimePlanning(endTime);
	}

	/**
	 * set the status to failed
	 * 
	 * @param endTime
	 */
	void setFailed(LocalDateTime endTime) {
		if (endTime.isBefore(getStartTime()))
			throw new IllegalArgumentException(
					"End time can not be before start time");
		setStatus(getStatus().goFailed());
		setEndTime(endTime);
		setEndTimePlanning(endTime);
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
	 * Check whether the dependencies of this Task are finished
	 */
	boolean checkDependenciesFinished() {
		for (Task dependency : getDependencies()) {
			if (dependency.getStatus() != TaskStatus.FINISHED)
				return false;
		}
		return true;
	}

	/**
	 * Returns the TaskFinishedStatus of a task
	 * 
	 * @return taskFinishStatus : status of a finished task
	 * @throws InvalidActivityException
	 *             : thrown when the task is not finished yet
	 */
	public TaskFinishedStatus getFinishStatus() {
		return status.getFinishStatus(this);
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
	 * 
	 * @return true if the Task requires resources
	 */
	public boolean requiresRessources() {
		return !getRequiredResourceTypes().isEmpty();
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

	/**
	 * Checks whether a task has a planning
	 * 
	 * @return true if the task has a planning
	 */
	public boolean hasPlanning() {
		return (planning != null);
	}

	/**
	 * Sets the endtime of a planning if the task has a planning
	 * 
	 * @param endTime
	 *            : endtime of a planning
	 */
	private void setEndTimePlanning(LocalDateTime endTime) {
		if (this.hasPlanning()) {
			getPlanning().setEndTime(endTime);
		}
	}

	/**
	 * accept visitor for visiting this
	 */
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Saves the current state of the task to a new memento
	 */
	void save() {
		this.memento = new Memento(this);
	}

	/**
	 * loads the last saved state of task from the momento
	 */
	boolean load() {
		if (this.memento == null) {
			return false;
		} else {
			this.memento.load(this);
			return true;
		}

	}

	/**
	 * 
	 * Inner momento class of task
	 * 
	 * @author groep 8
	 */
	private class Memento {

		private String description;
		private Duration estimatedDuration;
		private double acceptableDeviation;

		private List<Task> dependencies;
		private Map<ResourceType, Integer> requiredResourceTypes;
		private Task originalTask;

		private LocalDateTime endTime;
		private LocalDateTime startTime;
		private LocalDateTime lastUpdateTime;

		private Planning planning;

		private int id;

		private TaskStatus status;

		// Het aanmaken van het memento object
		// Alles moet gekopieerd worden, behalve referenties naar objecten in
		// ons domein
		// bv. de dependencies worden shallow gekopieerd
		/**
		 * Constructor of the momento inner class. It initialize all parameters
		 * of the current state of the task.
		 * 
		 * @param task
		 */
		public Memento(Task task) {
			this.description = new String(task.description);
			this.estimatedDuration = task.estimatedDuration.plusSeconds(0);
			this.acceptableDeviation = new Double(task.acceptableDeviation);

			this.dependencies = new ArrayList<Task>(task.dependencies);
			this.requiredResourceTypes = new LinkedHashMap<ResourceType, Integer>(
					task.requiredResourceTypes);
			this.originalTask = task.originalTask;

			this.endTime = task.endTime;
			this.startTime = task.startTime;
			this.lastUpdateTime = task.lastUpdateTime;

			this.planning = task.planning;
			this.id = task.id;

			this.status = task.status;
		}

		/**
		 * Sets the parameters of the task to the saved parameters
		 * 
		 * @param task
		 *            : given task
		 */
		public void load(Task task) {
			task.description = this.description;
			task.estimatedDuration = this.estimatedDuration;
			task.acceptableDeviation = this.acceptableDeviation;

			task.dependencies = this.dependencies;
			task.requiredResourceTypes = this.requiredResourceTypes;
			task.originalTask = this.originalTask;

			task.endTime = this.endTime;
			task.startTime = this.startTime;
			task.lastUpdateTime = this.lastUpdateTime;

			task.planning = this.planning;
			task.id = this.id;

			task.status = this.status;
		}
	}
}
