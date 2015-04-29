package taskManager.exception;
import java.util.HashSet;
import java.util.Set;

import taskManager.*;
import taskManager.Planning.PlanningBuilder;

public class ConlictingPlanningException extends RuntimeException{

	Set<Planning> conflictingPlannings;
	PlanningBuilder planningBuilder;
	public ConlictingPlanningException(Set<Planning> conflictingPlannings, PlanningBuilder planningBuilder){
		this.conflictingPlannings = conflictingPlannings;
		this.planningBuilder = planningBuilder;
	}


	public Set<Planning> getConflictingPlannings() {
		return conflictingPlannings;
	}
	public PlanningBuilder getPlanningBuilder() {
		return planningBuilder;
	}
}
