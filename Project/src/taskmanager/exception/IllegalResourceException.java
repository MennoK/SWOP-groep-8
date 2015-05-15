package taskmanager.exception;

import java.util.Set;

import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.Planning.PlanningBuilder;
import taskmanager.Task.TaskBuilder;

public class IllegalResourceException extends RuntimeException{

	private Set<ResourceType> requiredResourceTypes;
	private ResourceType errorType;


	private TaskBuilder taskBuilder;
	
	

	public IllegalResourceException(ResourceType type, Set<ResourceType> resourceTypes,
			TaskBuilder taskBuilder) {
		this.requiredResourceTypes = resourceTypes;
		this.taskBuilder = taskBuilder;
		this.errorType = type;
	}
	
	public Set<ResourceType> getRequiredResourceTypes() {
		return requiredResourceTypes;
	}


	public TaskBuilder getTaskBuilder() {
		return taskBuilder;
	}
	public ResourceType getErrorType() {
		return errorType;
	}
	
	

}
