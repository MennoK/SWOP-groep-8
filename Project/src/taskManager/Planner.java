package taskManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import taskManager.Planning.PlanningBuilder;
import utility.TimeSpan;
import utility.WorkDay;

public class Planner {
	Set<Planning> planningSet = new LinkedHashSet<Planning>();

	private Memento memento;

	private static final int TOTAL_POSSIBLE_START_TIMES = 3;

	/**
	 * Return all the tasks that do not have a planning yet
	 * 
	 * @param tasks
	 * @return set of tasks without a planning
	 */
	Set<Task> getUnplannedTasks(Set<Task> tasks) {

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
			LocalDateTime startTime, Set<Developer> developers) {

		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<LocalDateTime>();

		if (developers.isEmpty()) {
			throw new IllegalArgumentException(
					"Requires at least one developer to find a start time");
		}
		LocalDateTime time = LocalDateTime.of(startTime.getYear(), startTime.getMonth(), startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
		while (possibleStartTimes.size() < TOTAL_POSSIBLE_START_TIMES && time.isBefore(startTime.plusYears(1))) {
			TimeSpan timeSpan = new TimeSpan(time, task.getDuration());
			if (isPlannableForTimeSpan(task, developers, timeSpan)) {
				possibleStartTimes.add(timeSpan.getBegin());
			}
			time = time.plusHours(1);
		}
		return possibleStartTimes;
	}

	boolean isPlannableForTimeSpan(Task task, Set<Developer> developers,
			TimeSpan timeSpan) {
		if (enoughDevelopersAvalaible(developersAvailableFor(developers, task,
				timeSpan))
				&& enoughResourcesAvailable(
						resourcesAvailableFor(task, timeSpan), task)) {
			return true;
		} else {
			return false;
		}
	}

	boolean resourceDailyAvailableIsAvailable(Task task, TimeSpan timeSpan) {
		for (ResourceType type : task.getRequiredResourceTypes().keySet()) {
				if ((type.getDailyAvailability().getBegin()
					.isAfter(WorkDay.getStartTime()) || type
					.getDailyAvailability().getEnd()
					.isBefore(WorkDay.getEndTime()))) {
				if (timeSpan.getBegin().getHour() >= type.getDailyAvailability()
						.getBegin().getHour()
						&& timeSpan.getEnd().getHour() <= type
								.getDailyAvailability().getEnd().getHour()) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;

	}

	private boolean enoughDevelopersAvalaible(
			Set<Developer> developersAvailableFor) {
		if (developersAvailableFor.size() >= 1) {
			return true;
		} else {
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
	 * Removes a planning from the Planner
	 * 
	 * @param planning
	 */
	void removePlanning(Planning planning) {
		planningSet.remove(planning);
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
				TimeSpan planningTimeSpan = conflictingTask.getPlanning()
						.getTimeSpan();

				if (planningTimeSpan.overlaps(new TimeSpan(time, task
						.getDuration()))) {
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

	/**
	 * returns all plannings for all tasks
	 * 
	 * @return all the plannings in the system
	 */
	public Set<Planning> getAllPlannings() {
		return Collections.unmodifiableSet(this.planningSet);
	}

	Map<ResourceType, Set<Resource>> resourcesAvailableFor(Task task,
			TimeSpan timeSpan) {
		Map<ResourceType, Set<Resource>> availableResourcesForEachResourceType = new LinkedHashMap<ResourceType, Set<Resource>>();
		for (ResourceType resourceType : task.getRequiredResourceTypes()
				.keySet()) {
			availableResourcesForEachResourceType.put(resourceType,
					resourcesOfTypeAvailableFor(resourceType, task, timeSpan));
		}
		return availableResourcesForEachResourceType;
	}

	public Set<Resource> resourcesOfTypeAvailableFor(ResourceType resourcetype,
			Task task, TimeSpan timeSpan) {
		Set<Resource> availableResources = new LinkedHashSet<Resource>();
		for (Resource resource : resourcetype.getAllResources()) {
			if (isAvailableFor(resource, task, timeSpan)) {
				availableResources.add(resource);
			}
		}
		return availableResources;
	}

	boolean isAvailableFor(Resource resource, Task task, TimeSpan timeSpan) {
		Set<Planning> otherPlannings = new LinkedHashSet<Planning>(
				this.getAllPlannings());
		if (task.hasPlanning()) {
			otherPlannings.remove(task.getPlanning());
		}
		for (Planning otherPlanning : otherPlannings) {
			if (otherPlanning.getResources().contains(resource)) {
				if (timeSpan.overlaps(otherPlanning.getTimeSpan())) {
					return false;
				}
			}
		}
		return true;
	}

	public Set<Developer> developersAvailableFor(Set<Developer> developers,
			Task task, TimeSpan timeSpan) {
		Set<Developer> availableDevelopers = new LinkedHashSet<Developer>();
		for (Developer developer : developers) {
			if (isAvailableFor(developer, task, timeSpan)) {
				availableDevelopers.add(developer);
			}
		}
		return availableDevelopers;
	}

	boolean isAvailableFor(Developer developer, Task task, TimeSpan timeSpan) {
		Set<Planning> otherPlanings = new LinkedHashSet<Planning>(
				this.getAllPlannings());
		if (task.hasPlanning()) {
			otherPlanings.remove(task.getPlanning());
		}
		for (Planning otherPlanning : otherPlanings) {
			if (otherPlanning.getDevelopers().contains(developer)) {
				if (timeSpan.overlaps(otherPlanning.getTimeSpan())) {
					return false;
				}
			}
		}
		return true;
	}

	boolean isAvailableForDevelopers(Set<Developer> developers, Task task,
			TimeSpan timeSpan) {
		for (Developer developer : developers) {
			if (!isAvailableFor(developer, task, timeSpan)) {
				return false;
			}
		}
		return true;
	}

	boolean isAvailableForResources(Set<Resource> resources, Task task,
			TimeSpan timeSpan) {
		for (Resource resource : resources) {
			if (!isAvailableFor(resource, task, timeSpan)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * returns conlicting plannings based on the information of a
	 * planningbuilder
	 * 
	 * @param planningBuilder
	 *            : the planningbuilder that contains the information for a
	 *            planning that would conflict
	 * @return : set of conflicting plannings
	 */
	Set<Planning> getConflictingPlanningsForBuilder(
			PlanningBuilder planningBuilder) {
		Set<Planning> conflictingPlannings = new HashSet<>();

		for (Planning planning : this.getAllPlannings()) {
			if (planning.getTimeSpan().overlaps(planningBuilder.getTimeSpan())) {
				for (Developer developer : planningBuilder.getDevelopers()) {
					if (planning.getDevelopers().contains(developer)) {
						conflictingPlannings.add(planning);
					}
				}
				for (Resource resource : planningBuilder.getResources()) {
					if (planning.getResources().contains(resource)
							&& !conflictingPlannings.contains(planning)) {
						conflictingPlannings.add(planning);
					}
				}
			}
		}
		return conflictingPlannings;
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
			planningSet.add(planning);
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

	void updateStatus(Task task) {
		if (task.getStatus() == TaskStatus.EXECUTING
				|| task.getStatus() == TaskStatus.FINISHED
				|| task.getStatus() == TaskStatus.FAILED || !task.hasPlanning()
				|| !task.checkDependenciesFinished())
			// task status remains unchanged
			return;
		if (isPlannableForTimeSpan(task, task.getPlanning().getDevelopers(),
				new TimeSpan(task.getLastUpdateTime(), task.getDuration()))) {
			task.setStatus(TaskStatus.AVAILABLE);
		} else {
			task.setStatus(TaskStatus.UNAVAILABLE);
		}
	}

	void save() {
		this.memento = new Memento(this);
		for (Planning planning : this.planningSet) {
			planning.save();
		}
	}

	boolean load() {
		if (this.memento == null) {
			return false;
		} else {
			this.memento.load(this);
			for (Planning planning : this.planningSet) {
				planning.load();
			}
			return true;
		}
	}

	private class Memento {
		Set<Planning> planningSet;

		public Memento(Planner pe) {
			this.planningSet = new LinkedHashSet<Planning>(pe.planningSet);
		}

		public void load(Planner pe) {
			pe.planningSet = this.planningSet;
		}
	}
}
