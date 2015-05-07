package taskmanager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import taskmanager.exception.*;
import utility.TimeSpan;

/**
 * 
 * Planning class implements the planning of a task Every planning has an time
 * span (start + end time), a set of developers and optional a set of resources
 * that are required by the tasks.
 * 
 * @author Groep 8
 *
 */
public class Planning {

	private Memento memento;
	private TimeSpan timeSpan;
	private Set<Developer> developers = new LinkedHashSet<Developer>();
	private Set<Resource> resources = new HashSet<Resource>();

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

	/**
	 * 
	 * Returns a new planning builder to add extra parameters such as resources
	 * 
	 * @param startTime
	 *            : planned start time
	 * @param task
	 *            : task that is being planned
	 * @param developer
	 *            : assigned developers
	 * @param planner
	 *            : The projects planner
	 * 
	 * @return planningBuilder : new builder for creating planning
	 * @throws ConlictingPlanningException
	 */
	public static PlanningBuilder builder(LocalDateTime startTime, Task task,
			Developer developer, Planner planner) {
		return new PlanningBuilder(startTime, task, developer, planner);
	}

	/**
	 * Returns a set of all the developers of the planning
	 * 
	 * @return developers : set of developers
	 */
	public Set<Developer> getDevelopers() {
		return Collections.unmodifiableSet(developers);
	}

	/**
	 * Sets the set of developers of the planning
	 * 
	 * @param developers
	 *            : set of developers
	 */
	private void setDevelopers(Set<Developer> developers) {
		this.developers = developers;
	}

	/**
	 * Returns the set of resources of the planning
	 * 
	 * @return resources : set of resources
	 */
	public Set<Resource> getResources() {
		return Collections.unmodifiableSet(resources);
	}

	/**
	 * sets the set of resources of the planning
	 * 
	 * @param resources
	 *            : resources
	 */
	private void setResources(Set<Resource> resources) {
		this.resources = resources;
	}

	/**
	 * Returns the time span of the planning
	 * 
	 * @return timeSpan : time span of the planning
	 */
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

	/**
	 * saves the current state of planning in the memento
	 */
	void save() {
		this.memento = new Memento();
	}

	/**
	 * loads the last saved state of the planning from the memento
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load();
		}
	}

	/**
	 * Inner memento class of a planning
	 * 
	 * @author Groep 8
	 *
	 */
	private class Memento {
		private TimeSpan timeSpan;
		private Set<Developer> developers;
		private Set<Resource> resources;

		/**
		 * Constructor of the inner memento class. It initializes all the
		 * parameter of the current state of the planning
		 * 
		 * @param planning
		 */
		public Memento() {
			this.timeSpan = Planning.this.timeSpan;
			this.developers = new LinkedHashSet<Developer>(Planning.this.developers);
			this.resources = new LinkedHashSet<Resource>(Planning.this.resources);
		}

		/**
		 * Sets all parameters of the given planning to the saved paramets in
		 * the memento
		 * 
		 * @param planning
		 */
		public void load() {
			Planning.this.timeSpan = this.timeSpan;
			Planning.this.developers = this.developers;
			Planning.this.resources = this.resources;
		}
	}

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
		 * @param developer
		 *            : assigned developers
		 * @param planner
		 *            : planner of the planning
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
		 * 
		 * @throws ConlictingPlanningException
		 */
		public PlanningBuilder addResources(Resource resource) {
			this.resources.add(resource);
			if (!planner.isAvailableFor(resource, task, timeSpan)) {
				Set<Planning> conflictingPlannings = planner
						.getConflictingPlanningsForBuilder(this);
				throw new ConlictingPlanningException(conflictingPlannings,
						this);
			}
			return this;
		}

		/**
		 * a planning may require resources
		 * 
		 * @throws ConlictingPlanningException
		 */
		public PlanningBuilder addAllResources(Set<Resource> resources) {
			for (Resource resource : resources) {
				addResources(resource);
			}
			return this;
		}

		/**
		 * a planning may require more developers
		 * 
		 * @throws ConlictingPlanningException
		 */
		public PlanningBuilder addDeveloper(Developer developer) {
			this.developers.add(developer);
			if (!planner.isAvailableFor(developer, task, timeSpan)) {
				Set<Planning> conflictingPlannings = planner
						.getConflictingPlanningsForBuilder(this);
				throw new ConlictingPlanningException(conflictingPlannings,
						this);
			}
			return this;
		}

		/**
		 * Returns the resources that were added to the builder
		 * 
		 * @return
		 */
		Set<Resource> getResources() {
			return this.resources;
		}

		/**
		 * Returns the time span that was added to the builder
		 * 
		 * @return
		 */
		TimeSpan getTimeSpan() {
			return this.timeSpan;
		}

		/**
		 * Returns the developers that were added to the builder
		 * 
		 * @return developers
		 */
		Set<Developer> getDevelopers() {
			return this.developers;
		}

		/**
		 * Build a Planning after all the optional values have been set.
		 */
		public Planning build() {
			if (planner.isAvailableForDevelopers(developers, task, timeSpan)
					&& planner.isAvailableForResources(resources, task,
							timeSpan)) {
				Planning planning = new Planning(this);
				if (planner.taskHasPlanning(task)) {
					planner.removePlanning(task);
				}
				planner.addPlanning(task, planning);
				planner.updateStatus(task);
				return planning;
			} else {
				throw new IllegalStateException();
			}
		}

	}
}
