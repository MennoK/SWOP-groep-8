package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.activity.InvalidActivityException;

import taskManager.exception.InvalidTimeException;
import taskManager.exception.LoopingDependencyException;

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
 * the user may create an alternative task deploy nginx . This alternative task
 * replaces the failed task with respect to dependency management or determining
 * the project status (ongoing or finished). The time spent on the failed task
 * is however counted for the total execution time of the project.
 * 
 * @author groep 8
 */
public class Task {

	private ArrayList<Task> dependencies = new ArrayList<>();
	private String description;
	private Duration estimatedDuration;
	private double acceptableDeviation;
	private LocalDateTime endTime;
	private LocalDateTime startTime;
	private boolean failed = false;
	private TaskStatus status;
	private Task isAlternativeFor;

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
			double acceptableDeviation) {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		this.id = idCounter.getAndIncrement();
		this.updateStatus();
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
			double acceptableDeviation, Task isAlternativeFor) {
		this(description, estimatedDuration, acceptableDeviation);
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
			double acceptableDeviation, ArrayList<Task> dependencies) {
		this(description, estimatedDuration, acceptableDeviation);
		try {
			addMultipleDependencies(dependencies);
		} catch (LoopingDependencyException e) {
			// This can never occur in the constructor
		}
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
			double acceptableDeviation, Task isAlternativeFor,
			ArrayList<Task> dependencies) throws LoopingDependencyException {
		this(description, estimatedDuration, acceptableDeviation);
		addMultipleDependencies(dependencies);
		setAlternativeTask(isAlternativeFor);
	}

	// TODO naam niet goed, moet nog beter ge"implementeerd worden
	// TODO create seperate class wrapper for this
	private LocalDateTime add(LocalDateTime instant, Duration duration) {
		return instant.plus(Duration.ofDays(duration.toHours() / 8));
	}

	/**
	 * Checks whether a task has a dependency tasks
	 * 
	 * @param task
	 *            : dependent task
	 * @return true if the task has the given task as dependency
	 */
	private boolean hasDependency(Task task) {
		if (getDependencies().contains(task))
			return true;
		for (Task dependency : getDependencies())
			if (dependency.hasDependency(task))
				return true;
		return false;
	}

	/**
	 * 
	 * TODO doc
	 * 
	 * @param now
	 * @return
	 */
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

	/**
	 * Returns the status of a task
	 * 
	 * @return status of task
	 */
	public TaskStatus getStatus() {
		this.updateStatus();
		return this.status;
	}

	/**
	 * Adds a list of dependencies to task. The dependent tasks may not be
	 * already in the dependency list of the task
	 * 
	 * @param dependencies
	 *            : list with dependency task
	 * @throws LoopingDependencyException
	 *             : thrown when a loop occurs
	 */
	private void addMultipleDependencies(ArrayList<Task> dependencies)
			throws LoopingDependencyException {
		for (Task dependency : dependencies) {
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
	void addDependency(Task dependency) throws LoopingDependencyException {
		if (dependency.hasDependency(this))
			throw new LoopingDependencyException(
					"Tried to add task1 as a dependency to task2,"
							+ " but task2 is already dependent on task1.");
		if (!isValidDependency(dependency)) {
			throw new IllegalArgumentException(
					"The given dependency task is already dependent on this task");
		} else {
			dependencies.add(dependency);
			this.updateStatus();
		}
	}

	/**
	 * Returns the TaskFinishedStatus of a task
	 * 
	 * @return taskFinishStatus : status of a finished task
	 * @throws InvalidActivityException
	 *             : thrown when the task is not finished yet
	 */
	public TaskFinishedStatus getFinishStatus() throws InvalidActivityException {
		if (this.getStatus() != TaskStatus.FINISHED) {
			throw new InvalidActivityException("The task is not finished yet");
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
	private void setDescription(String description) {
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
	 * The acceptable deviation must be positive or zero
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
			this.updateStatus();
		}
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
	 * Sets the end time if and only if the given end time is after the start
	 * the start time of a project. The start time must be set before the end
	 * time
	 * 
	 * @param endTime
	 *            : the end time of task
	 * @throws NullPointerException
	 *             : thrown when the start time is not set yet
	 * 
	 */
	void setEndTime(LocalDateTime endTime) throws InvalidTimeException,
			NullPointerException {
		if (this.getStartTime() == null) {
			throw new NullPointerException(
					"There is not start time, set the starttime first.");
		}
		if (!isEndTimeAfterStartTime((this.getStartTime()), endTime)) {
			throw new InvalidTimeException(
					"the given end time is before the start time");
		} else {
			this.endTime = endTime;
			this.updateStatus();
		}
	}

	/**
	 * Checks whether the endtime is after the start time
	 * 
	 * @param startTime
	 *            : the startTime of a task
	 * @param endTime
	 *            : the endTime of a task
	 * @return true if and only if the start time is before the endtime
	 */
	private boolean isEndTimeAfterStartTime(LocalDateTime startTime,
			LocalDateTime endTime) {
		return endTime.isAfter(startTime);
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
	 * Sets the start time of a task
	 * 
	 * @param startTime
	 *            : the given start time of a task
	 */
	void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
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
	 * Sets a failed boolean to true or false and updates the task status
	 * 
	 * @param failed
	 *            : true if failed
	 */
	void setFailed() {
		this.failed = true;
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
	private void setAlternativeTask(Task isAlternativeFor)
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
	 * @throws InvalidTimeException
	 *             : if the startTime was after the endTime.
	 */
	public void updateStatus(LocalDateTime startTime, LocalDateTime endTime,
			boolean setToFail) throws InvalidTimeException {
		if (startTime.isAfter(endTime))
			throw new InvalidTimeException(
					"the given end time is before the start time");
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		if (setToFail) {
			this.setFailed();
		}
		this.updateStatus();
	}

	/**
	 * Updates the status of task. There are four different statuses for a task:
	 * Available, unavailable, finished or failed.
	 * 
	 * A task is failed when the boolean isFailed is true A task is finished
	 * when the task has an end time The task availability is dependent on the
	 * dependencies of the task
	 */
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
