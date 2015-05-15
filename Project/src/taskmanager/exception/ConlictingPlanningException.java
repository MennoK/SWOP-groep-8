package taskmanager.exception;

import java.util.Set;

import taskmanager.*;
import taskmanager.Planning.PlanningBuilder;

public class ConlictingPlanningException extends RuntimeException {

	private static final long serialVersionUID = 1884581243622150274L;

	Set<Task> conflictingTasks;
	PlanningBuilder planningBuilder;

	public ConlictingPlanningException(Set<Task> conflictingTasks,
			PlanningBuilder planningBuilder) {
		this.conflictingTasks = conflictingTasks;
		this.planningBuilder = planningBuilder;
	}

	public Set<Task> getConflictingTasks() {
		return conflictingTasks;
	}

	public PlanningBuilder getPlanningBuilder() {
		return planningBuilder;
	}
}
