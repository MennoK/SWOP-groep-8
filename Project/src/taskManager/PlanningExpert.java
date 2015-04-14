package taskManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class PlanningExpert {
	Set<Planning> planningSet = new HashSet<Planning>();
	
	
	
	/**
	 * Return all the tasks that do not have a planning yet
	 * 
	 * @param tasks
	 * @return set of tasks without a planning
	 */
	public Set<Task> getUnplannedTasks(Set<Task> tasks){
		
		Set<Task> modifieableTaskSet = new HashSet<Task>(tasks);
		for (Task task : modifieableTaskSet) {
			for (Planning planning : getAllPlannings()) {
				if(task.equals(planning.getTask())){
					modifieableTaskSet.remove(task);
				}
			}
		}
		return modifieableTaskSet;
	}
	
	/**
	 * returns 3 times at which a task could be planned so that all required developers and resources
	 * are available
	 * 
	 * @return A set of localdateTimes
	 */
	public Set<LocalDateTime> getPossibleStartTimes(Task task, LocalDateTime time, Set<Developer> developers){
		Set<LocalDateTime> possibleStartTimes = new HashSet<LocalDateTime>();
		//TODO: IMPLEMENTATION, waiting for implementation of Task
		
		while(possibleStartTimes.size() != 3){
		
		}
		
		return possibleStartTimes;
	}
	public void createPlanning(Task task, Set<Developer> developers, Set<Resource> resources, LocalDateTime time){
		getAllPlannings().add(new Planning(task, developers, resources, time));
	}

	public boolean hasConflictWithPlannedTask(Task task, LocalDateTime time){
		return false;
		
	}
	public Set<Task> getConflictingTasks(Task task){
		return null;
		
	}
	public void resolveConflictingTasks(Set<Task> tasks){
		
	}
	public Set<Planning> getAllPlannings(){
		return this.planningSet;
	}
}
