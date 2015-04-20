package taskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import taskManager.Task.TaskBuilder;

public class Planning {


	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Task task;
	private Set<Developer> developers;
	private Map<ResourceType, Set<Resource>> resources;
	/**
	 * The TaskBuilder is an inner class builder for constructing new tasks. The
	 * description, estimated duration and acceptable deviation of a task are
	 * required parameters. The optional parameters for a task are the original
	 * task, dependencies and required resource types.
	 */
	public static class PlanningBuilder {

		private LocalDateTime startTime;
		private LocalDateTime endTime;
		private Task task;
		private Set<Developer> developers;
		private Map<ResourceType, Set<Resource>> resources;
		/**
		 * Creates a PlanningBuilder with the required information for the creation
		 * of a Planning
		 * 
		 * 
		 * @param startTime
		 *            : planned start time
		 * @param task
		 *            : task that is being planned
		 * @param developers
		 * 		      : assigned developers 
		 	*/
		public PlanningBuilder(LocalDateTime startTime,
				Task task, Set<Developer> developers) {
			this.startTime = startTime;
			this.endTime = startTime.plus(task.getDuration());
			this.task = task;
			this.developers = developers;
			resources = new LinkedHashMap<>();
		}

		/**
		 * a planning may require resources 
		 */
		public PlanningBuilder addResources ( Map<ResourceType, Set<Resource>> resources){
			this.resources = new LinkedHashMap<>(resources);
			return this;
		}

		/**
		 * Build a Planning after all the optional values have been set.
		 */
		public Planning build(PlanningExpert planningExpert) {
			Planning planning = new Planning(this);
			planningExpert.addPlanning(planning);
			task.setPlanning(planning);
			return planning;
		}
	}
	/**
	 * The constructor of planning has a planning builder as argument. The planning builder
	 * contains all the required parameters and possible optional parameters
	 * 
	 * @param planningBuilder
	 *            : planning builder with parameters
	 */
	public Planning(PlanningBuilder planningBuilder){
		setDevelopers(planningBuilder.developers);
		setStartTime(planningBuilder.startTime);
		setEndTime(planningBuilder.endTime);
		
		setResources(planningBuilder.resources);
	}
	
	

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return this.endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Task getTask() {
		return task;
	}

	public Set<Developer> getDevelopers() {
		return developers;
	}

	public void setDevelopers(Set<Developer> developers) {
		this.developers = developers;
	}

	public Map<ResourceType, Set<Resource>> getResources() {
		return resources;
	}

	public void setResources(Map<ResourceType, Set<Resource>> resources2) {
		this.resources = resources2;
	}
}
