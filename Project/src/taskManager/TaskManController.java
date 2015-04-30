package taskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private Planner planner;
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
		createPlanner();
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
	 * creates a new planner
	 */
	void createPlanner() {
		this.planner = new Planner();
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
		return this.planner;
	}

	/**
	 * Saves the current state of the system. Only the last state is remembered
	 */
	public void saveSystem() {
		this.getProjectExpert().save();
		this.getDeveloperExpert().save();
		this.getPlanner().save();
		this.getResourceExpert().save();
		this.taskManClock.save();
	}

	/**
	 * Loads the last saved state of the system
	 */
	public void loadSystem() {
		if (!(this.getProjectExpert().load()
				&& this.getDeveloperExpert().load() && this.getPlanner().load()
				&& this.getResourceExpert().load() && this.taskManClock.load())) {
			throw new IllegalStateException(
					"You need to save the system before loading");
		}
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
		updateStatusAll();
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
		task.getPlanning().setTimeSpan(
				new TimeSpan(startTime, task.getDuration()));
		updateStatusAll();
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
		updateStatusAll();
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
		updateStatusAll();
	}

	private void updateStatusAll() {
		for (Task task : getProjectExpert().getAllTasks())
			getPlanner().updateStatus(task);
	}

	/**
	 * Return all the tasks that do not have a planning yet.
	 * 
	 * @return set of tasks without a planning
	 */
	public Set<Task> getUnplannedTasks() {
		return getPlanner().getUnplannedTasks(getProjectExpert().getAllTasks());
	}

	/**
	 * returns 3 times at which a task could be planned so that all required
	 * developers and resources are available
	 * 
	 * @return A set of localdateTimes
	 */
	public Set<LocalDateTime> getPossibleStartTimes(Task task) {
		return getPlanner().getPossibleStartTimes(task, getTime(),
				getDeveloperExpert().getAllDevelopers());
	}

	/**
	 * Have the system select resources for the given task, during the given
	 * timeSpan
	 * 
	 * @param task
	 * @param timeSpan
	 * @return The selected resources
	 */
	public Set<Resource> selectResources(Task task, TimeSpan timeSpan) {
		Map<ResourceType, Integer> requirements = task
				.getRequiredResourceTypes();
		Set<Resource> selected = new HashSet<Resource>();
		if (requirements.isEmpty()) {
			return selected;
		} else {
			for (ResourceType type : requirements.keySet()) {
				ArrayList<Resource> available = new ArrayList<Resource>(
						getPlanner().resourcesOfTypeAvailableFor(type, task,
								timeSpan));
				selected.addAll(available.subList(0, requirements.get(type)));
			}
		}
		return selected;
	}

	public Task getTask(Planning planning) {
		for (Task task : getProjectExpert().getAllTasks()) {
			if (task.hasPlanning() && task.getPlanning() == planning) {
				return task;
			}
		}
		throw new IllegalArgumentException(
				"This planning is not the planning of any Task!");
	}
}
