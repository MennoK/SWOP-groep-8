package TaskManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Task {
	private ArrayList<Task> dependencies;
	private String description;
	private Duration estimatedDuration;
	private double acceptableDeviation;
	private Instant endTime;
	private Instant startTime;
	private boolean failed;

	public Task(String description, Duration estimatedDuration,
			double acceptableDeviation) {
		this.description = description;
		this.estimatedDuration = estimatedDuration;
		this.acceptableDeviation = acceptableDeviation;
		this.dependencies = new ArrayList<Task>();
	}

	public TaskStatus getStatus() {
		if (isFailed())
			return TaskStatus.FAILED;
		if (getEndTime() != null)
			return TaskStatus.FINISHED;
		for (Task dependency : getDependencies())
			if (dependency.getStatus() != TaskStatus.FINISHED)
				return TaskStatus.UNAVAILABLE;
		return TaskStatus.AVAILABLE;
	}

	private Instant add(Instant instant, Duration duration) {
		return instant.plus(Duration.ofDays(duration.toHours() / 8));
	}

	public Instant getEstimatedFinishTime(Instant now) {
		if (getStartTime() != null)
			return add(getStartTime(), getEstimatedDuration());

		if (getDependencies().size() == 0)
			return add(now, getEstimatedDuration());

		Instant dependenceFinishTime = getDependencies().get(0)
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

	private boolean hasDependency(Task task) {
		if (getDependencies().contains(task))
			return true;
		for (Task dependency : getDependencies())
			if (dependency.hasDependency(task))
				return true;
		return false;
	}

	public void addDependency(Task dependency)
			throws LoopingDependencyException {
		if (dependency.hasDependency(this))
			throw new LoopingDependencyException(
					"Tried to add task1 as a dependency to task2,"
							+ " but task2 is already dependent on task1.");
		dependencies.add(dependency);
	}

	public List<Task> getDependencies() {
		return dependencies;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Duration getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(Duration estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public double getAcceptableDeviation() {
		return acceptableDeviation;
	}

	public void setAcceptableDeviation(double acceptableDeviation) {
		this.acceptableDeviation = acceptableDeviation;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}
}
