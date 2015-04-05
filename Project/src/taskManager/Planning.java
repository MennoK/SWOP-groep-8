package taskManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Planning {


	LocalDateTime startTime;
	LocalDateTime endTime;
	Task task;
	Set<Developer> developers;
	Set<Resource> resources;
	
	public Planning(Task task, Set<Developer> developers, Set<Resource> resources, LocalDateTime time){
		setTask(task);
		setDevelopers(developers);
		setResources(resources);
		setStartTime(time);
		setEndTime(time);
	}
	
	

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return this.getStartTime().plusMinutes(getTask().getDuration().toMinutes());
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Set<Developer> getDevelopers() {
		return developers;
	}

	public void setDevelopers(Set<Developer> developers) {
		this.developers = developers;
	}

	public Set<Resource> getResources() {
		return resources;
	}

	public void setResources(Set<Resource> resources) {
		this.resources = resources;
	}
}
