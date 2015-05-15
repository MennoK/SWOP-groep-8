package taskmanager.exception;

import java.util.Set;

import taskmanager.ResourceType;
import taskmanager.Task.TaskBuilder;

public class IllegalResourceException extends RuntimeException {

	private Set<ResourceType> requiredResourceTypes;
	private ResourceType errorType;
	private boolean conflicting;

	private TaskBuilder taskBuilder;

	public IllegalResourceException(ResourceType type,
			Set<ResourceType> resourceTypes, TaskBuilder taskBuilder,
			boolean conflicting) {
		this.requiredResourceTypes = resourceTypes;
		this.taskBuilder = taskBuilder;
		this.errorType = type;
		this.conflicting = conflicting;
	}

	public Set<ResourceType> getproblematicResourceTypes() {
		return requiredResourceTypes;
	}

	public TaskBuilder getTaskBuilder() {
		return taskBuilder;
	}

	public ResourceType getErrorType() {
		return errorType;
	}

	public boolean isConflicting() {
		return conflicting;
	}
}
