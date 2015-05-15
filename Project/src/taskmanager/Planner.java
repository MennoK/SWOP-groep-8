package taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBiMap;

import taskmanager.Planning.PlanningBuilder;
import utility.TimeSpan;
import utility.WorkDay;
import utility.WorkTime;

/**
 * 
 * The planner class makes and plans all the plannings.
 * 
 * @author Groep 8
 *
 */
public class Planner {
	private static final int TOTAL_POSSIBLE_START_TIMES = 3;

	private HashBiMap<Task, Planning> plannings = HashBiMap.create();

	private Memento memento;

	private final ImmutableClock clock;

	/**
	 * default constructor of planner
	 * 
	 * @param clock : required to keep track of system time
	 */
	Planner(ImmutableClock clock) {
		this.clock = clock;
	}

	/**
	 * Return all the tasks that do not have a planning yet
	 * 
	 * @param tasks
	 * @return set of tasks without a planning
	 */
	Set<Task> getUnplannedTasks(Set<Task> tasks) {

		Set<Task> modifiableTaskSet = new LinkedHashSet<Task>(tasks);

		for (Task task2 : tasks) {
			if (this.taskHasPlanning(task2)) {
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
	Set<LocalDateTime> getPossibleStartTimes(Task task,
			LocalDateTime startTime, Set<Developer> developers) {

		Set<LocalDateTime> possibleStartTimes = new LinkedHashSet<LocalDateTime>();

		if (developers.isEmpty()) {
			throw new IllegalArgumentException(
					"Requires at least one developer to find a start time");
		}

		LocalDateTime time = LocalDateTime.of(startTime.getYear(),
				startTime.getMonth(), startTime.getDayOfMonth(),
				startTime.getHour(), startTime.getMinute());

		while (possibleStartTimes.size() < TOTAL_POSSIBLE_START_TIMES
				&& time.isBefore(startTime.plusYears(1))) {
			TimeSpan timeSpan = new TimeSpan(time, task.getDuration());
			if (isPlannableForTimeSpan(task, developers, timeSpan)) {
				possibleStartTimes.add(timeSpan.getBegin());
			}
			time = WorkTime.getFinishTime(time, Duration.ofHours(1));
		}
		return possibleStartTimes;
	}

	/**
	 * Checks whether a task is plannable for given time span
	 * 
	 * @param task
	 *            : given task to be planned
	 * @param developers
	 *            : all developers
	 * @param timeSpan
	 *            : given time span
	 * 
	 * @return true if all developers and required resources are available for
	 *         the task in the given time span
	 */
	boolean isPlannableForTimeSpan(Task task, Set<Developer> developers,
			TimeSpan timeSpan) {
		if (enoughDevelopersAvalaible(
				developersAvailableFor(developers, task, timeSpan), task)
				&& enoughResourcesAvailable(
						resourcesAvailableFor(task, timeSpan), task)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether the required resources of a task are available during a
	 * given time span
	 * 
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * 
	 * @return true if all resources are available
	 */
	boolean resourceDailyAvailableIsAvailable(Task task, TimeSpan timeSpan) {
		for (ResourceType type : task.getRequiredResourceTypes().keySet()) {
			if ((type.getDailyAvailability().getBegin()
					.isAfter(WorkDay.getStartTime()) || type
					.getDailyAvailability().getEnd()
					.isBefore(WorkDay.getEndTime()))) {
				if (timeSpan.getBegin().getHour() >= type
						.getDailyAvailability().getBegin().getHour()
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

	/**
	 * checks whether there is at least 1 developer available
	 * 
	 * @param developersAvailableFor
	 *            : set of available developers
	 * 
	 * @return true if the size of the available developers is greater or equal
	 *         than one
	 */
	private boolean enoughDevelopersAvalaible(
			Set<Developer> developersAvailableFor, Task task) {
		if (developersAvailableFor.size() >= task
				.getAmountOfRequiredDevelopers()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether there are enough resources available
	 * 
	 * @param tempResourceMap
	 *            : map of available resources for each resource type
	 * @param task
	 *            : given task
	 * 
	 * @return true if the size of the available resources is equal to the
	 *         quantity of required resources of the task
	 */
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
		plannings.inverse().remove(planning);
	}

	void removePlanning(Task task) {
		plannings.remove(task);
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
	Set<Task> getConflictingTasks(Task task, LocalDateTime time, Set<Task> tasks) {
		Set<Task> conflictingTasks = new LinkedHashSet<>();
		for (Task conflictingTask : tasks) {

			if (this.taskHasPlanning(conflictingTask)) {
				TimeSpan planningTimeSpan = this.plannings.get(conflictingTask)
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
	boolean hasConflictWithAPlannedTask(Task task, LocalDateTime time) {
		TimeSpan taskTimeSpan = new TimeSpan(time, task.getDuration());
		for (Planning planning : this.plannings.values()) {
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
		return Collections.unmodifiableSet(plannings.values());
	}

	/**
	 * Returns a map with resource types and set of the available resources of
	 * the type
	 * 
	 * @param task
	 * @param timeSpan
	 * 
	 * @return avalaibleResourcesForEachResourceType : map with each
	 *         resourcetype required by the task and a set with available
	 *         resources
	 * 
	 */
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

	/**
	 * Returns a set of available resource of a given resource type during a
	 * time span
	 * 
	 * @param resourcetype
	 *            : given resource type
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * 
	 * @return availableResources : set of available resources during a given
	 *         time span
	 */
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

	/**
	 * Checks whether a resource of task is available for a giving timespan
	 *
	 * @param resource
	 *            : given resource
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * 
	 * @return true if the given resource is not yet in another planning
	 */
	boolean isAvailableFor(Resource resource, Task task, TimeSpan timeSpan) {
		Set<Planning> otherPlannings = new LinkedHashSet<Planning>(
				this.getAllPlannings());
		if (this.taskHasPlanning(task)) {
			otherPlannings.remove(this.plannings.get(task));
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

	/**
	 * Returns all available developers for a task during a given time span
	 * 
	 * @param developers
	 *            : set of resources
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * 
	 * @return a set of available developers during a given time span
	 */
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

	/**
	 * Checks whether a given developer is available for a task during a given
	 * time span
	 * 
	 * @param developer
	 *            : given developer
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * @return true if the given developer is not yet in an other planning
	 *         during the given time span
	 */
	boolean isAvailableFor(Developer developer, Task task, TimeSpan timeSpan) {
		Set<Planning> otherPlanings = new LinkedHashSet<Planning>(
				this.getAllPlannings());
		if (this.taskHasPlanning(task)) {
			otherPlanings.remove(this.plannings.get(task));
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

	/**
	 * Checks whether a set of developers are a available for a task during a
	 * given time span
	 * 
	 * @param developers
	 *            : given set of developers
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * @return true if all developers in the set of developers are available
	 */
	boolean isAvailableForDevelopers(Set<Developer> developers, Task task,
			TimeSpan timeSpan) {
		for (Developer developer : developers) {
			if (!isAvailableFor(developer, task, timeSpan)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether a set of resources are a available for a task during a
	 * given time span
	 * 
	 * @param resources
	 *            : given set of resources
	 * @param task
	 *            : given task
	 * @param timeSpan
	 *            : given time span
	 * @return true if all resources in the set of resources are available
	 */
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
	 * returns conlicting tasks based on the information of a planningbuilder
	 * 
	 * @param planningBuilder
	 *            : the planningbuilder that contains the information for a
	 *            planning that would conflict
	 * @return : set of conflicting tasks
	 */
	Set<Task> getConflictingTasksForBuilder(PlanningBuilder planningBuilder) {
		Set<Task> conflictingPlannings = new HashSet<>();

		for (Planning planning : this.getAllPlannings()) {
			if (planning.getTimeSpan().overlaps(planningBuilder.getTimeSpan())) {
				for (Developer developer : planningBuilder.getDevelopers()) {
					if (planning.getDevelopers().contains(developer)) {
						conflictingPlannings.add(getTask(planning));
					}
				}
				for (Resource resource : planningBuilder.getResources()) {
					if (planning.getResources().contains(resource)
							&& !conflictingPlannings.contains(planning)) {
						conflictingPlannings.add(getTask(planning));
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
	void addPlanning(Task task, Planning planning) {
		if (!canHavePlanning(planning)) {
			throw new IllegalArgumentException(
					"The given planning is already in the planningExpert.");
		} else {
			this.plannings.put(task, planning);
		}
	}

	/**
	 * returns the task that has the given planning 
	 * 
	 * @param planning : the given planning
	 * @return the task that is planned with the given planning
	 */
	public Task getTask(Planning planning) {
		return this.plannings.inverse().get(planning);
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
				|| task.getStatus() == TaskStatus.FAILED
				|| !this.taskHasPlanning(task)
				|| !task.checkDependenciesFinished())
			// task status remains unchanged
			return;
		if (isPlannableForTimeSpan(task, this.plannings.get(task)
				.getDevelopers(), new TimeSpan(this.clock.getCurrentTime(),
						task.getDuration()))) {
			task.setStatus(TaskStatus.AVAILABLE);
		} else {
			task.setStatus(TaskStatus.UNAVAILABLE);
		}
	}

	/**
	 * saves the current state of the memento
	 */
	void save() {
		this.memento = new Memento();
		for (Planning planning : this.plannings.values()) {
			planning.save();
		}
	}

	/**
	 * Loads the set of plannings to last saved state
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load();
			for (Planning planning : this.plannings.values()) {
				planning.load();
			}
		}
	}

	/**
	 * checks if a taks has a planning
	 * @param task : the task for which you want to know if it has a planning
	 * @return true if the task has a planning
	 */
	public boolean taskHasPlanning(Task task) {
		return this.plannings.get(task) != null;
	}

	/**
	 * returns the planning of a task
	 * 
	 * @param task : the task for which you want the planning
	 * @return the planning of the given task
	 */
	public Planning getPlanning(Task task) {
		if(taskHasPlanning(task)){
			return this.plannings.get(task);
		}else {
			return null;
		}
	}

	public PlanningBuilder createPlanning(LocalDateTime startTime, Task task,
			Developer developer) {
		return Planning.builder(startTime, task, developer, this);
	}

	/**
	 * Memento inner class of the planner
	 * 
	 * @author groep 8
	 *
	 */
	private class Memento {
		HashBiMap<Task, Planning> plannings;

		/**
		 * Constructor of the memento. It initializes the set of plannings in
		 * the memento to the current set of planning of the given planner
		 * 
		 * 
		 * @param pe
		 *            : planner
		 */
		public Memento() {
			this.plannings = HashBiMap.create(Planner.this.plannings);
		}

		/**
		 * Sets all the set of plannings back to saved state in the memento
		 * 
		 * @param pe
		 *            : given planner
		 */
		public void load() {
			Planner.this.plannings = this.plannings;
		}
	}
}
