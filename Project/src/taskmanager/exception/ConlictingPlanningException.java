package taskmanager.exception;
import java.util.Set;

import taskmanager.*;
import taskmanager.Planning.PlanningBuilder;

public class ConlictingPlanningException extends RuntimeException{

	private static final long serialVersionUID = 1884581243622150274L;
	
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
