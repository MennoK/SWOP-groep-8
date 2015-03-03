package TaskManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Project {
	private ArrayList<Task> tasks;
	private String name;
	private String description;
	private Instant creationTime;
	private Instant dueTime;

	public ProjectStatus getStatus() {
		return null;
	}
	
	public Instant getEstimatedFinishTime(Instant now) {
		return null;
	}
	
	public Instant getTotalDelay(Instant now) {
		return null;
	}

	public List<Task> getAllTasks() {
		return tasks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getDueTime() {
		return dueTime;
	}

	public void setDueTime(Instant dueTime) {
		this.dueTime = dueTime;
	}

	public Instant getCreationTime() {
		return creationTime;
	}
}
