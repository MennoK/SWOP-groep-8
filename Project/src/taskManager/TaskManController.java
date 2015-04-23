package taskManager;

import java.time.LocalDateTime;
import java.util.PrimitiveIterator.OfDouble;
import java.util.LinkedHashSet;
import java.util.Set;

import sun.util.locale.provider.AvailableLanguageTags;
import utility.TimeSpan;

/**
 * The taskManController class controls every expert
 * 
 * @author Groep 8
 *
 */
public class TaskManController {

	private DeveloperExpert developerExpert;
	private ResourceExpert resourceExpert;
	private ProjectExpert projectExpert;

	private TaskManClock taskManClock;

	/**
	 * Constructor of TaskManController. When a new TaskManController has been
	 * created new expert classes will be created.
	 */
	public TaskManController(LocalDateTime now) {
		this.taskManClock = new TaskManClock(now);

		createDeveloperExpert();
		createResourceExpert();
		createProjectExpert();
	}

	/**
	 * Creates a new project expert
	 */
	private void createProjectExpert() {
		this.projectExpert = new ProjectExpert();
		this.taskManClock.register(projectExpert);
		this.projectExpert.handleTimeChange(getTime());
	}

	/**
	 * Creates a new resource expert
	 */
	private void createResourceExpert() {
		this.resourceExpert = new ResourceExpert();
	}

	/**
	 * Creates a new developer expert
	 */
	private void createDeveloperExpert() {
		this.developerExpert = new DeveloperExpert();
	}

	/**
	 * Returns the developer expert
	 * 
	 * @return developerExpert : developer expert
	 */
	public DeveloperExpert getDeveloperExpert() {
		return developerExpert;
	}

	/**
	 * Returns the resource expert
	 * 
	 * @return resourceExpert : resource expert
	 */
	public ResourceExpert getResourceExpert() {
		return resourceExpert;
	}

	/**
	 * Returns the project expert
	 * 
	 * @return projectExpert : project expert
	 */
	public ProjectExpert getProjectExpert() {
		return projectExpert;
	}

	/**
	 * Returns the planning expert
	 * 
	 * @return planningExpert : planning expert
	 */
	public Planner getPlanner() {
		return getProjectExpert().getPlanner();
	}

	/**
	 * 
	 * Advances the time of TaskMan. This will update the status of every task
	 * in every project of the project controller
	 * 
	 * @param time
	 *            : new time
	 * @throws IllegalArgumentException
	 *             : thrown when the given time is invalid
	 */
	public void advanceTime(LocalDateTime time) {
		this.taskManClock.setTime(time);
		this.getProjectExpert().handleTimeChange(this.taskManClock.getTime());
	}

	/**
	 * Returns the time
	 * 
	 * @return LocalDateTime : time
	 */
	public LocalDateTime getTime() {
		return this.taskManClock.getTime();
	}

	/**
	 * Tell the system execution of Task was started. And updates the status of
	 * all Tasks.
	 * 
	 * @param task
	 * @param startTime
	 */
	public void setExecuting(Task task, LocalDateTime startTime) {
		task.setExecuting(startTime);
		// TODO update status of all tasks
	}

	/**
	 * Tell the system execution of Task was finished. And updates the status of
	 * all Tasks.
	 * 
	 * @param task
	 * @param endTime
	 */
	public void setFinished(Task task, LocalDateTime endTime) {
		task.setFinished(endTime);
		// TODO update status of all tasks
	}

	/**
	 * Tell the system execution of Task failed. And updates the status of all
	 * Tasks.
	 * 
	 * @param task
	 * @param endTime
	 */
	public void setFailed(Task task, LocalDateTime endTime) {
		task.setFailed(endTime);
		// TODO update status of all tasks
	}

	private void updateStatusAll() {
		for (Task task : getProjectExpert().getAllTasks())
			updateStatus(task);
	}

	private void updateStatus(Task task) {
		if (task.getStatus() == TaskStatus.EXECUTING
				|| task.getStatus() == TaskStatus.FINISHED
				|| task.getStatus() == TaskStatus.FAILED || !task.hasPlanning()
				|| !task.checkDependenciesFinished())
			// task status remains unchanged
			return;
		for (Developer developer : task.getPlanning().getDevelopers()) {
			if (!isAvailableFor(developer, task,
					new TimeSpan(getTime(), task.getDuration()))) {
				task.setStatus(TaskStatus.UNAVAILABLE);
				return;
			}
		}
		// TODO check ressources
	}


	boolean resourcesAvailableFor(Task task, TimeSpan timeSpan){
		for (ResourceType resourceType : task.getRequiredResourceTypes().keySet()) {
			if(!isAvailableFor(resourceType, task, timeSpan)){
				return false;
			}
		}
		return true;
	}

	private boolean isAvailableFor(ResourceType resourcetype, Task task, TimeSpan timeSpan){
		Set<Resource> availableResources = new LinkedHashSet<Resource>();
		for(Resource resource : resourcetype.getAllResources()){
			if(isAvailableFor(resource, task, timeSpan)){
				availableResources.add(resource);
			}
		}
		if(availableResources.size() >= task.getRequiredResourceTypes().get(resourcetype)){
			return true;
		}
		else {
			return false;
		}
	}


	private boolean isAvailableFor(Resource resource, Task task, TimeSpan timeSpan){
		Set<Planning> otherPlannings = getPlanner().getAllPlannings();
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

	private boolean isAvailableFor(Developer developer, Task task,
			TimeSpan timeSpan) {
		Set<Planning> otherPlanings = getPlanner().getAllPlannings();
		otherPlanings.remove(task.getPlanning());
		for (Planning otherPlanning : otherPlanings) {
			if (otherPlanning.getDevelopers().contains(developer)) {
				if (timeSpan.overlaps(otherPlanning.getTimeSpan()))
					return false;
			}
		}
		return true;
	}

}
