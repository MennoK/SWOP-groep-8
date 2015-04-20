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
		Set<ResourceType> requiredResourceTypes = task
				.getRequiredResourceTypes().keySet();
		Map<ResourceType, Set<Resource>> resourceMap = getResourceMap(requiredResourceTypes);
		while (possibleStartTimes.size() < 3) {
			Map<ResourceType, Set<Resource>> tempResourceMap = resourceMap;
			Set<Developer> tempDevelopers = new LinkedHashSet<>(developers);
			Set<Planning> plannings = new LinkedHashSet<>(
					this.getAllPlannings());
			for (Planning planning : plannings) {
				if (overLap(planning, time, task)) {
					tempDevelopers = removeDevelopers(planning, tempDevelopers);
					tempResourceMap = removResources(planning, tempResourceMap);
				}
			}
			if (tempDevelopers.size() > 0
					&& enoughResourcesAreAvailable(tempResourceMap, task)) {
				possibleStartTimes.add(time);

			}
			time = time.plusHours(1);
		}

		return possibleStartTimes;
	}

	private boolean enoughResourcesAreAvailable(
			Map<ResourceType, Set<Resource>> tempResourceMap, Task task) {

		for (ResourceType type : task.getRequiredResourceTypes().keySet()) {
			Set<Resource> resources = tempResourceMap.get(type);
			if (!(resources.size() >= task.getRequiredResourceTypes().get(type))) {
				return false;
			}
		}
		return true;

	}

	private Map<ResourceType, Set<Resource>> removResources(Planning planning,
			Map<ResourceType, Set<Resource>> tempResourceMap) {

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
	 * creates a map with as key the resource types required by the tasks that
	 * maps to the list of resources of that type
	 * 
	 * @param requiredResourceTypes
	 * @return
	 */
	private Map<ResourceType, Set<Resource>> getResourceMap(
			Set<ResourceType> requiredResourceTypes) {
		Map<ResourceType, Set<Resource>> resourceMap = new LinkedHashMap<ResourceType, Set<Resource>>();

		for (ResourceType resourceType : requiredResourceTypes) {
			resourceMap.put(resourceType, resourceType.getAllResources());
		}
		return resourceMap;
	}

	/**
	 * checks if there is overlap in the reservations of resources/developers
	 * and a task
	 * 
	 * @param planning
	 * @param time
	 * @param task
	 * @return
	 */
	private boolean overLap(Planning planning, LocalDateTime time, Task task) {

		if (planning.getEndTime().isAfter(time)
				&& planning.getStartTime().isBefore(time.plus(task.getDuration()))) {
			return true;
		}
		if (planning.getStartTime().isAfter(time)
				&& planning.getStartTime().isBefore(
						time.plus(task.getDuration()))) {
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
	 * returns if a task will have a conflict if planned on a certain time
	 * 
	 * @param task
	 *            : the task that would conflict with currently planned tasks
	 * @param time
	 *            : the time at which there might be a conflict
	 * @return
	 */
	public boolean hasConflictWithAPlannedTask(Task task, LocalDateTime time) {
		for (Planning planning : planningSet) {
			if (overLap(planning, time, task)) {
				return true;
			}
		}
		return false;

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
				if (overLap(conflictingTask.getPlanning(), time, task)) {
					conflictingTasks.add(conflictingTask);
				}
			}
		}
		return conflictingTasks;

	}

	public Set<Planning> getAllPlannings() {
		return this.planningSet;
	}
}
