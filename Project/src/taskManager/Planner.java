package taskManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import taskManager.Planning.PlanningBuilder;
import utility.TimeSpan;

public class Planner {
	Set<Planning> planningSet = new LinkedHashSet<Planning>();
	private static final int TOTAL_POSSIBLE_START_TIMES = 3;

	/**
	 * Return all the tasks that do not have a planning yet
	 * 
	 * @param tasks
	 * @return set of tasks without a planning
	 */
	public Set<Task> getUnplannedTasks(Set<Task> tasks) {

		Set<Task> modifiableTaskSet = new LinkedHashSet<Task>(tasks);

		for (Task task2 : tasks) {
			if (task2.hasPlanning()) {
				modifiableTaskSet.remove(task2);
			}

		}
		return modifiableTaskSet;
	}

	/**
	 * returns 3 times at which a task could be planned so that all required
	 * developers and resources are available
	 * 
	 * @return A set of localdateTimes
	 */
	public Set<LocalDateTime> getPossibleStartTimes(Task task,
			LocalDateTime time, Set<Developer> developers) {

		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<LocalDateTime>();

		while (possibleStartTimes.size() < TOTAL_POSSIBLE_START_TIMES) {
			TimeSpan timeSpan = new TimeSpan(time, task.getDuration());
			if(isPlannableForTimeSpan(task, developers, timeSpan)){
				possibleStartTimes.add(timeSpan.getBegin());
			}
			time = time.plusHours(1);
		}
		return possibleStartTimes;
	}

	boolean isPlannableForTimeSpan(Task task, Set<Developer> developers,TimeSpan timeSpan){
		if(enoughDevelopersAvalaible(developersAvailableFor(developers, task, timeSpan)) && enoughResourcesAvailable(resourcesAvailableFor(task, timeSpan), task)){
			return true;
		}
		else {
			return false;
		}
	}

	private boolean enoughDevelopersAvalaible(
			Set<Developer> developersAvailableFor) {
		if(developersAvailableFor.size() >= 1){
			return true;
		}
		else{
			return false;
		}
	}

	private boolean enoughResourcesAvailable(
			Map<ResourceType, Set<Resource>> tempResourceMap, Task task) {
		for (ResourceType type : task.getRequiredResourceTypes().keySet()) {
			Set<Resource> resources = tempResourceMap.get(type);
			if (!(resources.size() >= task.getRequiredResourceTypes().get(type))) {
				return false;
			}
		}
		return true;
	}


	/**
	 * creates a map with as key the resource types required by the tasks that
	 * maps to the list of resources of that type
	 * 
	 * @param requiredResourceTypes
	 * @return
	 */
	private Map<ResourceType, Set<Resource>> getResourceMap(Task task) {
		Set<ResourceType> requiredResourceTypes = task
				.getRequiredResourceTypes().keySet();
		Map<ResourceType, Set<Resource>> resourceMap = new LinkedHashMap<ResourceType, Set<Resource>>();

		for (ResourceType resourceType : requiredResourceTypes) {
			resourceMap.put(resourceType, resourceType.getAllResources());
		}
		return resourceMap;
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
	 *            : assigned developers
	 * 
	 * @return planningBuilder : new builder for creating planning
	 */
	public PlanningBuilder createPlanning(LocalDateTime startTime, Task task,
			Developer developer) {

		return new PlanningBuilder(startTime, task, developer);
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
	 * This method checks if PlanningExpert can have a given planning. It
	 * returns true if and only if the PlanningExpert does not contain the
	 * planning yet and the planning is not null
	 * 
	 * @param planning
	 *            : given planning to be added
	 * @return true if and only if the given planning is not null and the task
	 *         is not already in the PlanningExpert
	 */
	private boolean canHavePlanning(Planning planning) {
		return (!getAllPlannings().contains(planning) && planning != null);
	}


	/**
	 * returns all tasks that would conflict if a task would be planned at a
	 * certain time
	 * 
	 * @param task
	 *            : the task for which we want to get all the conflicts
	 * @param time
	 *            : the time for the task to be planned
	 * @param tasks
	 *            : all tasks where there is possible a conflict with
	 * @return
	 */
	public Set<Task> getConflictingTasks(Task task, LocalDateTime time,
			Set<Task> tasks) {
		Set<Task> conflictingTasks = new LinkedHashSet<>();
		for (Task conflictingTask : tasks) {

			if (conflictingTask.hasPlanning()) {
				TimeSpan planningTimeSpan = conflictingTask.getPlanning().getTimeSpan();

				if (planningTimeSpan.overlaps(new TimeSpan(time, task.getDuration()))) {
					conflictingTasks.add(conflictingTask);
				}
			}
		}
		return conflictingTasks;

	}
	
	/**
	 * returns if a task will have a conflict if planned on a certain time
	 * 
	 * @param task
	 *            : the task that would conflict with currently planned tasks
	 * @param time
	 *            : the time at which there might be a conflict
	 * @return
	 */
	public boolean hasConflictWithAPlannedTask(Task task, LocalDateTime time) {
		TimeSpan taskTimeSpan = new TimeSpan(time, task.getDuration());
		for (Planning planning : planningSet) {
			if (taskTimeSpan.overlaps(planning.getTimeSpan())) {
				return true;
			}
		}
		return false;

	}


	public Set<Planning> getAllPlannings() {
		return this.planningSet;
	}

	Map<ResourceType, Set<Resource>> resourcesAvailableFor(Task task, TimeSpan timeSpan){
		Map<ResourceType, Set<Resource>> availableResourcesForEachResourceType = new LinkedHashMap<ResourceType,Set<Resource>>();
		for (ResourceType resourceType : task.getRequiredResourceTypes().keySet()) {
			availableResourcesForEachResourceType.put(resourceType, resourcesAvailableFor(resourceType, task, timeSpan));
		}
		return availableResourcesForEachResourceType;
	}

	Set<Resource> resourcesAvailableFor(ResourceType resourcetype, Task task, TimeSpan timeSpan){
		Set<Resource> availableResources = new LinkedHashSet<Resource>();
		for(Resource resource : resourcetype.getAllResources()){
			if(isAvailableFor(resource, task, timeSpan)){
				availableResources.add(resource);
			}
		}
		return availableResources;
	}


	private boolean isAvailableFor(Resource resource, Task task, TimeSpan timeSpan){
		Set<Planning> otherPlannings = this.getAllPlannings();
		otherPlannings.remove(task.getPlanning());
		for(Planning otherPlanning : otherPlannings){
			if(otherPlanning.getDevelopers().contains(resource)){
				if (timeSpan.overlaps(otherPlanning.getTimeSpan())){
					return false;
				}
			}
		}
		return true;
	}

	Set<Developer> developersAvailableFor(Set<Developer> developers, Task task, TimeSpan timeSpan){
		Set<Developer> availableDevelopers = new LinkedHashSet<Developer>();
		for(Developer developer : developers ){
			if(isAvailableFor(developer, task, timeSpan)){
				availableDevelopers.add(developer);
			}
		}
		return availableDevelopers;
	}

	private boolean isAvailableFor(Developer developer, Task task,
			TimeSpan timeSpan) {
		Set<Planning> otherPlanings = this.getAllPlannings();
		if(task.hasPlanning()){
			otherPlanings.remove(task.getPlanning());
		}
		for (Planning otherPlanning : otherPlanings) {
			if (otherPlanning.getDevelopers().contains(developer)) {
				if (timeSpan.overlaps(otherPlanning.getTimeSpan())){
					return false;
				}
			}
		}
		return true;
	}

}
