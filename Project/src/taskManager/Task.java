package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
 * @author groep 8
 */
public class Task {

	private List<Task> dependencies = new ArrayList<>();
	private String description;
	private Duration estimatedDuration;
	private double acceptableDeviation;
	private LocalDateTime endTime;
	private LocalDateTime startTime;
	private boolean failed = false;
	private Task isAlternativeFor;

	private LocalDateTime lastUpdateTime;

	// Thread safe integer sequence generator that starts at 1
	private static AtomicInteger idCounter = new AtomicInteger(1);
	private int id;

	/**
	 * Constructor of task with arguments: description, estimatedDuration and
	 * acceptable deviation
	 * 
	 * @param description
	 *            : given description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable duration of task
	 */
	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, LocalDateTime now) {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		this.id = idCounter.getAndIncrement();
		update(now);
	}

	/**
	 * Constructor of task with arguments: description, estimatedDuration and
	 * acceptable deviation and a task which the task an alternative for
	 * 
	 * @param description
	 *            : given description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable duration of task
	 * @param isAlternativeFor
	 *            : the alternative task which failed
	 */
	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, LocalDateTime now, Task isAlternativeFor) {
		this(description, estimatedDuration, acceptableDeviation, now);
		setAlternativeTask(isAlternativeFor);
	}

	/**
	 * Constructor of task with arguments: description, estimatedDuration and
	 * acceptable deviation, and a list of dependencies
	 * 
	 * @param description
	 *            : given description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable duration of task
	 * @param dependencies
	 *            : list with dependencies
	 */
	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, LocalDateTime now,
			List<Task> dependencies) {
		this(description, estimatedDuration, acceptableDeviation, now);
		addMultipleDependencies(dependencies);
	}

	/**
	 * Constructor of task with arguments: description, estimatedDuration and
	 * acceptable deviation and and a task which the task an alternative for and
	 * a list with dependencies
	 * 
	 * @param description
	 *            : given description of a task
	 * @param estimatedDuration
	 *            : estimated duration of task
	 * @param acceptableDeviation
	 *            : acceptable duration of task
	 * @param isAlternativeFor
	 *            : the alternative task which failed
	 * @param dependencies
	 *            : list with dependencies
	 * 
	 */
	Task(String description, Duration estimatedDuration,
			double acceptableDeviation, LocalDateTime now,
			Task isAlternativeFor, List<Task> dependencies) {
		this(description, estimatedDuration, acceptableDeviation, now);
		if (dependencies.contains(isAlternativeFor))
			throw new IllegalArgumentException(
					"Can not create an alternative task which is dependent"
							+ " on the task it is an alternative for");
		for (Task dep : dependencies)
			if (dep.hasDependency(isAlternativeFor))
				throw new IllegalArgumentException(
						"Can not create an alternative task which is indirectly dependent"
								+ " on the task it is an alternative for");
		addMultipleDependencies(dependencies);
		setAlternativeTask(isAlternativeFor);
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
	 * This method returns true if and only if the given dependency is not yet
	 * in the dependency list
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
	 * Sets the alternative task if and only if the the current task his status
	 * is failed
	 * 
	 * @param isAlternativeFor
	 *            : alternative task
	 * @throws IllegalArgumentException
	 *             : thrown when the task is not failed
	 */
	private void setAlternativeTask(Task isAlternativeFor)
			throws IllegalArgumentException {
		if (isAlternativeFor.getStatus() != TaskStatus.FAILED) {
			throw new IllegalArgumentException(
					"Task cannot be alternative to a task that has not failed");
		}
		this.isAlternativeFor = isAlternativeFor;
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
	 * Pore mans observer pattern
	 * 
	 * @param time
	 *            : the new time of the clock
	 */
	void update(LocalDateTime time) {
		this.lastUpdateTime = time;
	}

	/**
	 * Allows the user to update the status of a Task to finished or failed
	 * 
	 * @param startTime
	 *            : the time at which the user started working on the task.
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
	public TaskFinishedStatus getFinishStatus() throws IllegalArgumentException {
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

}
