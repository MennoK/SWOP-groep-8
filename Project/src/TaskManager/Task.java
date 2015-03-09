package TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
	
	public Task(String description, Duration estimatedDuration,
			double acceptableDeviation) {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		this.updateStatus();
	}
	public Task(String description, Duration estimatedDuration,
			double acceptableDeviation, Task isAlternativeFor) {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		setAlterternativeTask(isAlternativeFor);
		this.updateStatus();
	}

	public Task(String description, Duration estimatedDuration,
			double acceptableDeviation, ArrayList<Task> dependencies) throws LoopingDependencyException {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		addMultipleDependencies(dependencies);
		this.updateStatus();
	}
	public Task(String description, Duration estimatedDuration,
			double acceptableDeviation, Task isAlternativeFor, ArrayList<Task> dependencies) throws LoopingDependencyException {
		setDescription(description);
		setEstimatedDuration(estimatedDuration);
		setAcceptableDeviation(acceptableDeviation);
		addMultipleDependencies(dependencies);
		setAlterternativeTask(isAlternativeFor);
		this.updateStatus();
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
	public void addMultipleDependencies(ArrayList<Task> dependencies) throws LoopingDependencyException{
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

	public void setFailed(boolean failed) {
		this.failed = failed;
		this.updateStatus();
	}
	public void setAlterternativeTask(Task isAlternativeFor) throws IllegalArgumentException{
		if (isAlternativeFor.getStatus() != TaskStatus.FAILED){
			throw new IllegalArgumentException("Task cannot be alternative to a task that has not failed");
		}
		this.isAlternativeFor = isAlternativeFor;
	}

	public Task getAlternativeFor(){
		return this.isAlternativeFor;
	}
	public void updateStatus()
	{
		this.status = TaskStatus.AVAILABLE;
		if (isFailed()) {
			this.status =  TaskStatus.FAILED;
		}
		else if (getEndTime() != null) {
			this.status = TaskStatus.FINISHED;
		}
		else if (!getDependencies().isEmpty()){
			for (Task dependency : getDependencies()) {
				if (dependency.getStatus() != TaskStatus.FINISHED) {
					this.status = TaskStatus.UNAVAILABLE;
				}
			}
		}

	}
}
