package TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * 
 * @author groep 8
 *
 */
public class Task {
	private ArrayList<Task> dependencies = new ArrayList<>();
	private String description;
	private Duration estimatedDuration;
	private double acceptableDeviation;
	private LocalDateTime endTime;
	private LocalDateTime startTime;
	private boolean failed;
	private TaskStatus status;
	private Task isAlternativeFor;

	// Thread safe integer sequence generator that starts at 1
	private static AtomicInteger idCounter = new AtomicInteger(1);
	private int id;

	Task(String description, Duration estimatedDuration,
			double acceptableDeviation) {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		this.id = idCounter.getAndIncrement();
		this.updateStatus();
	}

	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, Task isAlternativeFor) {
		this(description, estimatedDuration, acceptableDeviation);
		setAlternativeTask(isAlternativeFor);
	}

	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, ArrayList<Task> dependencies)
			throws LoopingDependencyException {
		this(description, estimatedDuration, acceptableDeviation);
		addMultipleDependencies(dependencies);
	}

	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, Task isAlternativeFor,
			ArrayList<Task> dependencies) throws LoopingDependencyException {
		this(description, estimatedDuration, acceptableDeviation);
		addMultipleDependencies(dependencies);
		setAlternativeTask(isAlternativeFor);
	}

	private LocalDateTime add(LocalDateTime instant, Duration duration) {
		return instant.plus(Duration.ofDays(duration.toHours() / 8));
	}

	private boolean hasDependency(Task task) {
		if (getDependencies().contains(task))
			return true;
		for (Task dependency : getDependencies())
			if (dependency.hasDependency(task))
				return true;
		return false;
	}

	public LocalDateTime getEstimatedFinishTime(LocalDateTime now) {
		if (getStartTime() != null)
			return add(getStartTime(), getEstimatedDuration());

		if (getDependencies().size() == 0)
			return add(now, getEstimatedDuration());

		LocalDateTime dependenceFinishTime = getDependencies().get(0)
				.getEstimatedFinishTime(now);
		for (Task dependency : getDependencies()) {
			if (dependenceFinishTime.compareTo(dependency
					.getEstimatedFinishTime(now)) < 0)
				dependenceFinishTime = dependency.getEstimatedFinishTime(now);
		}
		if (dependenceFinishTime.compareTo(now) < 0)
			return add(now, getEstimatedDuration());
		return add(dependenceFinishTime, getEstimatedDuration());
	}

	public TaskStatus getStatus() {
		this.updateStatus();
		return this.status;
	}

	public void addMultipleDependencies(ArrayList<Task> dependencies)
			throws LoopingDependencyException {
		for (Task dependency : dependencies) {
			addDependency(dependency);
		}
	}

	public void addDependency(Task dependency)
			throws LoopingDependencyException {
		if (dependency.hasDependency(this))
			throw new LoopingDependencyException(
					"Tried to add task1 as a dependency to task2,"
							+ " but task2 is already dependent on task1.");
		dependencies.add(dependency);
		this.updateStatus();
	}

	/**
	 * Returns the list with dependencies of the task
	 * 
	 * @return dependencies: list with dependencies
	 */
	public List<Task> getDependencies() {
		return dependencies;
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
	 * Sets the description of the task
	 * 
	 * @param description
	 *            : the given description of task
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * Sets the estimated duration of a task by a given argument. The estimated
	 * duration of task has to be strictly positive
	 * 
	 * @param estimatedDuration
	 *            : the given estimated duration
	 */
	// TODO : estimated duration mag niet negatief zijn
	public void setEstimatedDuration(Duration estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
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
	 * Sets the acceptable deviation of task and updates the task status. The
	 * acceptable deviation is strictly positive
	 * 
	 * @param acceptableDeviation
	 *            : the given acceptable deviation
	 */
	// TODO : acceptable deviation mag niet negatief zijn
	public void setAcceptableDeviation(double acceptableDeviation) {
		this.acceptableDeviation = acceptableDeviation;
		this.updateStatus();
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
		this.updateStatus();
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public boolean isFailed() {
		return failed;

	}

	/**
	 * Sets a failed boolean to true or false and updates the task status
	 * 
	 * @param failed
	 *            : true if failed
	 */
	public void setFailed(boolean failed) {
		this.failed = failed;
		this.updateStatus();
	}

	/**
	 * Sets the alternative task if and only if the the current task his status
	 * is failed
	 * 
	 * @param isAlternativeFor
	 *            : alternative task
	 * @throws IllegalArgumentException
	 *             : thrown when the task is not failed
	 */
	public void setAlternativeTask(Task isAlternativeFor)
			throws IllegalArgumentException {
		if (isAlternativeFor.getStatus() != TaskStatus.FAILED) {
			throw new IllegalArgumentException(
					"Task cannot be alternative to a task that has not failed");
		}
		this.isAlternativeFor = isAlternativeFor;
	}

	/**
	 * Returns the task which this task is alternative for
	 * 
	 * @return isAlternativeFor: the alternative task
	 */
	public Task getAlternativeFor() {
		return this.isAlternativeFor;
	}

	/**
	 * Returns the id of a task
	 * 
	 * @return id : id of the task
	 */
	public int getId() {
		return this.id;
	}

	public void updateStatus(LocalDateTime newStartTime,
			LocalDateTime newEndTime, boolean setFailed) {
		// TODO implement
	}

	public void updateStatus() {
		this.status = TaskStatus.AVAILABLE;
		if (isFailed()) {
			this.status = TaskStatus.FAILED;
		} else if (getEndTime() != null) {
			this.status = TaskStatus.FINISHED;
		} else if (!getDependencies().isEmpty()) {
			for (Task dependency : getDependencies()) {
				if (dependency.getStatus() != TaskStatus.FINISHED) {
					this.status = TaskStatus.UNAVAILABLE;
				}
			}
		}

	}
}
