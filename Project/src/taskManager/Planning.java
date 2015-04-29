package taskManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import utility.TimeSpan;

public class Planning {

	private TimeSpan timeSpan;
	private Set<Developer> developers = new LinkedHashSet<Developer>();
	private Set<Resource> resources = new HashSet<Resource>();

	/**
	 * The TaskBuilder is an inner class builder for constructing new tasks. The
	 * description, estimated duration and acceptable deviation of a task are
	 * required parameters. The optional parameters for a task are the original
	 * task, dependencies and required resource types.
	 */
	public static class PlanningBuilder {

		private Planner planner;
		private TimeSpan timeSpan;
		private Task task;
		private Set<Developer> developers;
		private Set<Resource> resources;

		/**
		 * Creates a PlanningBuilder with the required information for the
		 * creation of a Planning
		 * 
		 * 
		 * @param startTime
		 *            : planned start time
		 * @param task
		 *            : task that is being planned
		 * @param developers
		 *            : assigned developers
		 */
		public PlanningBuilder(LocalDateTime startTime, Task task,
				Developer developer, Planner planner) {
			this.timeSpan = new TimeSpan(startTime, startTime.plus(task
					.getDuration()));
			this.task = task;
			this.resources = new HashSet<Resource>();
			this.planner = planner;
			this.developers = new LinkedHashSet<Developer>();
			this.addDeveloper(developer);
		}

		/**
		 * a planning may require resources
		 */
		public PlanningBuilder addResources(Resource resource) {
			if(planner.isAvailableFor(resource, task, timeSpan)){
				this.resources.add(resource);
			}else{
				throw new IllegalArgumentException("there is a conflict with an other task");  
			}
			return this;
		}

		/**
		 * a planning may require resources
		 */
		public PlanningBuilder addAllResources(Set<Resource> resources) {
			for (Resource resource : resources) {
				addResources(resource);
			}
			return this;
		}

		/**
		 * a planning may require more developers
		 */
		public PlanningBuilder addDeveloper(Developer developer) {
			if(planner.isAvailableFor(developer, task, timeSpan)){
				this.developers.add(developer);
			}else{
				throw new IllegalArgumentException("there is a conflict with an other task");  
			}
			return this;
		}

		/**
		 * Build a Planning after all the optional values have been set.
		 */
		public Planning build() {
			Planning planning = new Planning(this);
			planner.addPlanning(planning);
			task.setPlanning(planning);
			planner.updateStatus(task);
			return planning;
		}
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
	public static PlanningBuilder builder(LocalDateTime startTime, Task task,
			Developer developer, Planner planner) {
		return new PlanningBuilder(startTime, task, developer, planner);
	}

	/**
	 * The constructor of planning has a planning builder as argument. The
	 * planning builder contains all the required parameters and possible
	 * optional parameters
	 * 
	 * @param planningBuilder
	 *            : planning builder with parameters
	 */
	public Planning(PlanningBuilder planningBuilder) {
		setDevelopers(planningBuilder.developers);
		setTimeSpan(planningBuilder.timeSpan);
		setResources(planningBuilder.resources);
	}

	public Set<Developer> getDevelopers() {
		return Collections.unmodifiableSet(developers);
	}

	void setDevelopers(Set<Developer> developers) {
		this.developers = developers;
	}

	public Set<Resource> getResources() {
		return Collections.unmodifiableSet(resources);
	}

	void setResources(Set<Resource> resources) {
		this.resources = resources;
	}

	public TimeSpan getTimeSpan() {
		return timeSpan;
	}

	/**
	 * sets the timespan of the planning
	 * 
	 * @param timeSpan
	 *            the new timespan of the planning
	 */
	public void setTimeSpan(TimeSpan timeSpan) {
		this.timeSpan = timeSpan;
	}

	/**
	 * allow to edit the end time of the planning
	 * 
	 * @param endTime
	 *            the new end time of the planning
	 */
	public void setEndTime(LocalDateTime endTime) {
		if (!endTime.isAfter(getTimeSpan().getBegin())) {
			throw new IllegalStateException(
					"given end time is before the start time");
		}
		if (endTime.isBefore(this.getTimeSpan().getEnd())) {
			this.getTimeSpan().setEnd(endTime);
		}

	}

}
