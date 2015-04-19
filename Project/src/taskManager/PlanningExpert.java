package taskManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import taskManager.Planning.PlanningBuilder;
public class PlanningExpert {
	Set<Planning> planningSet = new LinkedHashSet<Planning>();
	
	
	
	/**
	 * Return all the tasks that do not have a planning yet
	 * 
	 * @param tasks
	 * @return set of tasks without a planning
	 */
	public Set<Task> getUnplannedTasks(Set<Task> tasks){
		
		Set<Task> modifiableTaskSet = new LinkedHashSet<Task>(tasks);
		
			for (Task task2 : tasks) {
				if(task2.hasPlanning()){
					modifiableTaskSet.remove(task2);
				}
			
		}
		return modifiableTaskSet;
	}
	
	/**
	 * returns 3 times at which a task could be planned so that all required developers and resources
	 * are available
	 * 
	 * @return A set of localdateTimes
	 */
	public Set<LocalDateTime> getPossibleStartTimes(Task task, LocalDateTime time, Set<Developer> developers){
		Set<LocalDateTime> possibleStartTimes = new HashSet<LocalDateTime>();
		
		Set<ResourceType> requiredResourceTypes = task.getRequiredResourceTypes().keySet();
		Map<ResourceType, Set<Resource>>  resourceMap = getResourceMap(requiredResourceTypes);
		
		while(possibleStartTimes.size() != 3){
			Map<ResourceType, Set<Resource> > tempResourceMap = resourceMap;
			Set<Developer> tempDevelopers = developers;
			
			for (Planning planning : this.getAllPlannings()) {
				if(overLap(planning, time, task)){
					tempDevelopers = removeDevelopers(planning,tempDevelopers);
					tempResourceMap = removResources(planning, tempResourceMap);
				}
			}
			if(tempDevelopers.size() > 0 && enoughResourcesAreAvailable(tempResourceMap, task)){
				possibleStartTimes.add(time);
				
				}
			time = time.plusHours(1);
		}
		
		return possibleStartTimes;
	}	
	

	private boolean enoughResourcesAreAvailable(Map<ResourceType, Set<Resource>> tempResourceMap, Task task) {
		
		for (ResourceType type : task.getRequiredResourceTypes().keySet()) {

			Set<Resource> resources = tempResourceMap.get(type);
			if(!(resources.size() >= task.getRequiredResourceTypes().get(type))){
				return false;
			}
		}
		return true;
		
	}
	


	private Map<ResourceType, Set<Resource> > removResources(Planning planning,
			Map<ResourceType, Set<Resource> > tempResourceMap) {
		
		for (ResourceType type : planning.getResources().keySet()) {
			Set<Resource> resources = tempResourceMap.get(type);
			resources.removeAll(planning.getResources().get(type));
			tempResourceMap.put(type, resources);
		}
		
		return tempResourceMap;
	}

	private Set<Developer> removeDevelopers(Planning planning,
			Set<Developer> tempDevelopers) {
		tempDevelopers.removeAll(tempDevelopers);
		return tempDevelopers;
				
	}

	/**
	 * creates a map with as key the resource types required by the tasks that maps to the list of resources of that type  
	 * @param requiredResourceTypes
	 * @return
	 */
	private  Map<ResourceType, Set<Resource> >  getResourceMap(Set<ResourceType> requiredResourceTypes) {
		Map<ResourceType, Set<Resource>> resourceMap = new LinkedHashMap<ResourceType, Set<Resource>>();
		
		for (ResourceType resourceType : requiredResourceTypes) {
			resourceMap.put(resourceType, resourceType.getAllResources());
		}
		return resourceMap;
	}

	/**
	 * checks if there is overlap in the reservations of resources/developers and a task
	 * 
	 * @param planning
	 * @param time
	 * @param task
	 * @return
	 */
	private boolean overLap(Planning planning, LocalDateTime time, Task task) {
		
		if(planning.getEndTime().isAfter(time) && time.isBefore(time.plus(task.getDuration()))){
			return true;
		}
		if(planning.getStartTime().isAfter(time) && planning.getStartTime().isBefore(time.plus(task.getDuration()))){
			return true;
		}
			
		return false;
	}

	/**
	 * 
	 * Returns a new planning builder to add extra parameters such as resources
	 * 
	 * @param startTime
	 *            : planned start time
	 * @param endTime
	 *            : planned end time
	 * @param task
	 *            : task that is being planned
	 * @param developers
	 * 		      : assigned developers 
	 * 
	 * @return planningBuilder : new builder for creating planning
	 */
	public PlanningBuilder createPlanning(LocalDateTime startTime,
			Task task, Set<Developer> developers) {
		
		return new PlanningBuilder(startTime, task, developers);
	}

	/**
	 * This method adds a given planning to the planningExpert
	 * 
	 * @param planning
	 *            : planning to add to project
	 * @throws IllegalArgumentException
	 *             : thrown when the given planning is not valid
	 */
	void addPlanning(Planning planning) {
		if (!canHavePlanning(planning)) {
			throw new IllegalArgumentException(
					"The given planning is already in the planningExpert.");
		} else {
			this.getAllPlannings().add(planning);
		}
	}

	/**
	 * This method checks if PlanningExpert can have a given planning. It returns true if
	 * and only if the PlanningExpert does not contain the planning yet and the planning is not
	 * null
	 * 
	 * @param planning
	 *            : given planning to be added
	 * @return true if and only if the given planning is not null and the task is
	 *         not already in the PlanningExpert
	 */
	private boolean canHavePlanning(Planning planning) {
		return (!getAllPlannings().contains(planning) && planning != null);
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
