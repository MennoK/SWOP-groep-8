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

	public TaskStatus getStatus() {
		return null;
	}
	
	public Instant getEstimatedFinishTime(Instant now) {
		return null;
	}

	public void addDependency(Task dependency) {

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
