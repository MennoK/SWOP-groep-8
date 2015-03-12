package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.activity.InvalidActivityException;

import taskManager.exception.InvalidTimeException;

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

	private ArrayList<Task> dependencies = new ArrayList<>();
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
			ArrayList<Task> dependencies) {
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
			Task isAlternativeFor, ArrayList<Task> dependencies) {
		this(description, estimatedDuration, acceptableDeviation, now);
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
	boolean hasDependency(Task task) {
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
	public LocalDateTime getEstimatedFinishTime() {
		
		if(this.getEndTime() != null) {
			return this.getEndTime();
		} else {
			
			if(this.getDependencies().isEmpty()) {
				return this.lastUpdateTime.plus(this.estimatedDuration);
			} else {
				//Find last estimated time of the dependencies
				LocalDateTime estimatedTime = this.lastUpdateTime;
				for(Task t : this.getDependencies()) {
					if(t.getEstimatedFinishTime().isAfter(estimatedTime)) {
						estimatedTime = t.getEstimatedFinishTime();
					}
				}
				return estimatedTime;
			}
			
		}
		

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
	private void addMultipleDependencies(ArrayList<Task> dependencies) {
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
	 * Sets the acceptable deviation of task. The The acceptable deviation must
	 * be positive or zero
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
	 * Returns the end time of a project
	 * 
	 * @return endTime : the endTime of a project
	 */
	public LocalDateTime getEndTime() {
		return endTime;
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
	 * Checks whether the endtime is after the start time
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
	private void setStartTime(LocalDateTime startTime) {
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
	 * Sets a failed boolean to true or false
	 * 
	 * @param failed
	 *            : true if failed
	 */
	private void setFailed() {
		if(this.getStatus() == TaskStatus.FINISHED || TaskStatus.AVAILABLE)
			this.failed = true;
		else 
			throw new IllegalStateException();
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
		if (!isValidStartTimeAndEndTime(startTime, endTime))
			throw new InvalidTimeException(
					"the given end time is before the start time");
		if (setToFail) {
			this.setFailed();
		}
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		
	}

	/**
	 * Gets the status of task. There are four different statuses for a task:
	 * Available, unavailable, finished or failed.
	 * 
	 * A task is failed when the boolean isFailed is true A task is finished
	 * when the task has an end time The task availability is dependent on the
	 * dependencies of the task
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
	 * Pore mans observer pattern
	 * 
	 * @param time
	 *            : the new time of the clock
	 */
	void update(LocalDateTime time) {
		this.lastUpdateTime = time;
	}

	/**
	 * Getter for lastUpdateTime
	 */
	LocalDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}
}
