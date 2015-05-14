	package taskmanager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
public class BranchOffice {

	private String location;

	private DeveloperExpert developerExpert;
	private ResourceExpert resourceExpert;
	private ProjectExpert projectExpert;
	private DelegatedTaskExpert delegatedTaskExpert;
	private Planner planner;
	private TaskManClock clock;

	/**
	 * Constructor of TaskManController. When a new TaskManController has been
	 * created new expert classes will be created.
	 */
	@Deprecated
	public BranchOffice(LocalDateTime now) {
		createDeveloperExpert();
		createResourceExpert();
		createProjectExpert();
		createDelegatedTaskExpert();
		createPlanner();
	}

	public BranchOffice(String location, ImmutableClock clock) {
		// temporary time object
		this.clock = (TaskManClock)clock;
		setLocation(location);
		createDeveloperExpert();
		createResourceExpert();
		createProjectExpert();
		createDelegatedTaskExpert();
		createPlanner();
	}

	@Deprecated
	public BranchOffice(String string) {
		this(string, new TaskManClock(LocalDateTime.of(1, 1, 1, 1, 1 )));
	}


	/**
	 * Creates a new delegated task expert
	 */
	private void createDelegatedTaskExpert() {
		this.delegatedTaskExpert = new DelegatedTaskExpert();
	}

	/**
	 * Sets the location of the branch office
	 * 
	 * @param location
	 *            : given location
	 */
	private void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Creates a new project expert
	 */
	private void createProjectExpert() {
		this.projectExpert = new ProjectExpert(clock);
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
		this.planner = new Planner(clock);
	}

	/**
	 * Returns the developer expert
	 * 
	 * @return developerExpert : developer expert
	 */
	DeveloperExpert getDeveloperExpert() {
		return developerExpert;
	}

	/**
	 * Returns the resource expert
	 * 
	 * @return resourceExpert : resource expert
	 */
	ResourceExpert getResourceExpert() {
		return resourceExpert;
	}

	/**
	 * Returns the project expert
	 * 
	 * @return projectExpert : project expert
	 */
	ProjectExpert getProjectExpert() {
		return projectExpert;
	}

	/**
	 * Returns the location of the branch office
	 * 
	 * @return location : location as a string
	 */
	public String getLocation() {
		return this.location;
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
	 * Returns the delegated task expert
	 * 
	 * @return delegatedTaskExpert : delegated task expert
	 */
	public DelegatedTaskExpert getDelegatedTaskExpert() {
		return this.delegatedTaskExpert;
	}

	/**
	 * Saves the current state of the system. Only the last state is remembered
	 */
	// TODO package private
	public void saveSystem() {
		this.getProjectExpert().save();
		this.getDeveloperExpert().save();
		this.getPlanner().save();
		this.getResourceExpert().save();
		this.getDelegatedTaskExpert().save();
	}

	/**
	 * Loads the last saved state of the system
	 */
	// TODO package private
	public void loadSystem() {
		this.getProjectExpert().load();
		this.getDeveloperExpert().load();
		this.getPlanner().load();
		this.getResourceExpert().load();
	}

	/**
	 * Tell the system execution of Task was started. And updates the status of
	 * all Tasks.
	 * 
	 * @param task
	 * @param startTime
	 */
	@Deprecated
	public void setExecuting(Task task, LocalDateTime startTime) {
		task.setExecuting(startTime);
		this.getPlanner().getPlanning(task)
				.setTimeSpan(new TimeSpan(startTime, task.getDuration()));
		updateStatusAll();
	}

	/**
	 * Tell the system execution of Task was finished. And updates the status of
	 * all Tasks.
	 * 
	 * @param task
	 * @param endTime
	 */
	@Deprecated
	public void setFinished(Task task, LocalDateTime endTime) {
		task.setFinished(endTime);
		if (this.getPlanner().taskHasPlanning(task)) {
			this.getPlanner().getPlanning(task).setEndTime(endTime);
		}
		updateStatusAll();
	}

	/**
	 * Tell the system execution of Task failed. And updates the status of all
	 * Tasks.
	 * 
	 * @param task
	 * @param endTime
	 */
	@Deprecated
	public void setFailed(Task task, LocalDateTime endTime) {
		task.setFailed(endTime);
		if (this.getPlanner().taskHasPlanning(task)) {
			this.getPlanner().getPlanning(task).setEndTime(endTime);
		}
		updateStatusAll();
	}

	/**
	 * Update the status of all tasks
	 */
	@Deprecated
	void updateStatusAll() {
		for (Task task : getProjectExpert().getAllTasks())
			getPlanner().updateStatus(task);
	}

	/**
	 * Return all the tasks that do not have a planning yet.
	 * 
	 * @return set of tasks without a planning
	 */
	@Deprecated
	public Set<Task> getUnplannedTasks() {
		return getPlanner().getUnplannedTasks(getProjectExpert().getAllTasks());
	}

	/**
	 * returns 3 times at which a task could be planned so that all required
	 * developers and resources are available
	 * 
	 * @return A set of localdateTimes
	 */
	@Deprecated
	public Set<LocalDateTime> getPossibleStartTimes(Task task) {
		return getPlanner().getPossibleStartTimes(task, this.clock.getCurrentTime(),
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
	@Deprecated
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

	/**
	 * Returns a list of the projects
	 * 
	 * @return projects: list of projects
	 */
	@Deprecated
	public Set<Project> getAllProjects() {
		return Collections.unmodifiableSet(getProjectExpert().getAllProjects());
	}

	/**
	 * Returns the set of all resource types
	 * 
	 * @return resourcetypes : set of all resource types
	 */
	@Deprecated
	public Set<ResourceType> getAllResourceTypes() {
		return Collections.unmodifiableSet(getResourceExpert()
				.getAllResourceTypes());
	}

	/**
	 * Returns the unmodifiable set of all developers
	 * 
	 * @return developers : set of all developers
	 */
	@Deprecated
	public Set<Developer> getAllDevelopers() {
		return Collections.unmodifiableSet(getDeveloperExpert()
				.getAllDevelopers());
	}

	/**
	 * 
	 * Returns a set with all tasks of a given developer
	 * 
	 * @param dev
	 *            the active Developer
	 * @return All the tasks to which this developer is assigned.
	 */
	@Deprecated
	public Set<Task> getAllTasks(Developer dev) {
		Set<Task> tasks = new HashSet<Task>();
		for (Project project : getAllProjects()) {
			for (Task task : project.getAllTasks()) {
				if (this.getPlanner().taskHasPlanning(task)
						&& this.getPlanner().getPlanning(task).getDevelopers()
								.contains(dev)) {
					tasks.add(task);
				}
			}
		}
		return Collections.unmodifiableSet(tasks);
	}

	/**
	 * Creates a new project with the given arguments and adds the project to
	 * the list of projects
	 * 
	 * @param name
	 *            : name of the project
	 * @param description
	 *            : description of the project
	 * @param creationTime
	 *            : creation time of the project
	 * @param dueTime
	 *            : due time of the project
	 */
	@Deprecated
	public Project createProject(String name, String description,
			LocalDateTime creationTime, LocalDateTime dueTime) {
		return getProjectExpert().createProject(name, description,
				creationTime, dueTime);
	}

	/**
	 * Creates a new project with the given arguments and adds the project to
	 * the list of projects. The creationTime is set to the current time
	 * 
	 * @param name
	 *            : name of the project
	 * @param description
	 *            : description of the project
	 * @param dueTime
	 *            : due time of the project
	 */
	@Deprecated
	public Project createProject(String name, String description,
			LocalDateTime dueTime) {
		return getProjectExpert().createProject(name, description, this.clock.getCurrentTime(),
				dueTime);
	}

	/**
	 * Creates a new developer with the given name. and adds the new developer
	 * to the set of all developers
	 * 
	 * @param name
	 *            : given name
	 */
	@Deprecated
	public Developer createDeveloper(String name) {
		return getDeveloperExpert().createDeveloper(name);
	}
	
	@Deprecated
	public LocalDateTime getTime() {
		return clock.getCurrentTime();
	}

	@Deprecated
	public void advanceTime(LocalDateTime date) {
		clock.setTime(date);
	}

}
