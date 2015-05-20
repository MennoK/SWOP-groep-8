package taskmanager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import taskmanager.exception.UnplannableDevAmountException;
import taskmanager.exception.UnplannableResourceAmountException;
import taskmanager.exception.UnplannableResourceTypeException;
import utility.TimeSpan;

public class TaskManController {
	private Company company;
	private BranchOffice activeOffice;
	private Developer activeDeveloper;
	private TaskManClock taskManClock;

	/**
	 * Constructor of TaskManController
	 * 
	 * @param now
	 */
	public TaskManController(LocalDateTime now) {
		taskManClock = new TaskManClock(now);
		company = new Company(taskManClock);
	}

	/**
	 * Returns the company of TaskMan
	 * 
	 * @return company : company
	 */
	public Company getCompany() {
		return company;
	}

	/**
	 * Log into a Branch office
	 * 
	 * @param activeOffice
	 */
	public void logIn(BranchOffice activeOffice) {
		setActiveOffice(activeOffice);
	}

	/**
	 * delegates a task from one branch office to an other.
	 * 
	 * @param activeDeveloper
	 */
	public void logIn(Developer activeDeveloper) {
		setActiveDeveloper(activeDeveloper);
	}

	/**
	 * Log out returning the active developer and branch office to their base
	 * state.
	 */
	public void logOut() {
		setActiveDeveloper(null);
		setActiveOffice(null);
	}

	/**
	 * delegates a task from one branch office to an other.
	 * 
	 * @param task
	 *            : the task that must be delegated
	 * @param branchOffice
	 *            : the branch office to where the task must be delegated
	 */
	public void delegate(Task task, BranchOffice branchOffice) {
		if (taskIsDelegatedToActiveOffice(task)) {
			branchOffice.getDelegatedTaskExpert().addDelegatedTask(
					task,
					activeOffice.getDelegatedTaskExpert()
							.getOriginalOffice(task));
			activeOffice.getDelegatedTaskExpert().removeDelegatedTask(task);

		} else {
			branchOffice.getDelegatedTaskExpert().addDelegatedTask(task,
					activeOffice);
		}
	}

	/**
	 * }
	 * 
	 * /** Tell the system execution of Task was started. And updates the status
	 * of all Tasks.
	 * 
	 * @param task
	 * @param startTime
	 */
	public void setExecuting(Task task, LocalDateTime startTime) {
		checkActiveOfficeForNull();
		task.setExecuting(startTime);
		activeOffice.getPlanner().getPlanning(task)
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
	public void setFinished(Task task, LocalDateTime endTime) {
		checkActiveOfficeForNull();
		task.setFinished(endTime);
		activeOffice.getPlanner().getPlanning(task).setEndTime(endTime);
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
		checkActiveOfficeForNull();
		task.setFailed(endTime);
		activeOffice.getPlanner().getPlanning(task).setEndTime(endTime);
		updateStatusAll();
	}

	/**
	 * Advances the time of TaskMan.
	 * 
	 * @param time
	 *            : new time
	 * @throws IllegalArgumentException
	 *             : thrown when the given time is invalid
	 */
	public void advanceTime(LocalDateTime time) {
		this.taskManClock.setTime(time);
	}

	/**
	 * Return all the tasks that do not have a planning yet.
	 * 
	 * @return set of tasks without a planning
	 */
	private Set<Task> getUnplannedTasks() {
		checkActiveOfficeForNull();
		Set<Task> allTasks = new HashSet<Task>(activeOffice
				.getDelegatedTaskExpert().getAllDelegatedTasks());
		allTasks.addAll(activeOffice.getProjectExpert().getAllTasks());
		return activeOffice.getPlanner().getUnplannedTasks(allTasks);
	}

	/**
	 * Returns all the tasks that can be delegated from the current active
	 * branch office
	 * 
	 * @return a set of tasks that can be delegated from the current active
	 *         branch office
	 */
	public Set<Task> getAllDelegatablePlannableTasks() {
		Set<Task> unplannedTasks = new HashSet<Task>(getUnplannedTasks());
		Set<Task> delegatableTasks = new HashSet<Task>(getUnplannedTasks());
		for (Task unplannedTask : unplannedTasks) {
			if (!taskIsDelegatable(unplannedTask)) {
				delegatableTasks.remove(unplannedTask);
			}
		}

		return Collections.unmodifiableSet(delegatableTasks);
	}

	private boolean taskIsDelegatable(Task unplannedTask) {
		if (!taskHasBeenDelegated(unplannedTask)
				|| taskIsDelegatedToActiveOffice(unplannedTask)) {
			return true;
		}
		return false;
	}

	private boolean taskIsDelegatedToActiveOffice(Task unplannedTask) {
		checkActiveOfficeForNull();
		return activeOffice.getDelegatedTaskExpert().getAllDelegatedTasks()
				.contains(unplannedTask);
	}

	private boolean taskHasBeenDelegated(Task unplannedTask) {
		for (BranchOffice office : company.getAllBranchOffices()) {
			if (office.getDelegatedTaskExpert().getAllDelegatedTasks()
					.contains(unplannedTask)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns 3 times at which a task could be planned so that all required
	 * developers and resources are available
	 * 
	 * @return A set of localdateTimes
	 */
	public Set<LocalDateTime> getPossibleStartTimes(Task task) {
		checkActiveOfficeForNull();

		if (activeOffice.getDeveloperExpert().getAllDevelopers().isEmpty()) {
			throw new IllegalStateException(
					"Requires at least one developer to find a start time");
		}
		
		if (activeOffice.getDeveloperExpert().getAllDevelopers().size() < task.getAmountOfRequiredDevelopers()){
			throw new UnplannableDevAmountException(task.getAmountOfRequiredDevelopers());
		}
		
		for(ResourceType type : activeOffice.getResourceExpert().getAllResourceTypes()){
			if(task.getRequiredResourceTypes().keySet().contains(type)){
				if (type.getAllResources().size() < task.getRequiredResourceTypes().get(type)){
					throw new UnplannableResourceAmountException(task.getRequiredResourceTypes().get(type), type);
				}
			}
			else{
				throw new UnplannableResourceTypeException(type);
			}
		}

		return activeOffice.getPlanner().getPossibleStartTimes(task, getTime(),
				activeOffice.getDeveloperExpert().getAllDevelopers());
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
		checkActiveOfficeForNull();
		Set<Resource> selected = new HashSet<Resource>();
		for (ResourceType type : task.getRequiredResourceTypes().keySet()) {
			ArrayList<Resource> available = new ArrayList<Resource>(
					activeOffice.getPlanner().resourcesOfTypeAvailableFor(type,
							task, timeSpan));
			selected.addAll(available.subList(0, task
					.getRequiredResourceTypes().get(type)));
		}
		return selected;
	}

	/**
	 * Returns a list of the projects from the active office
	 * 
	 * @return projects
	 */
	public Set<Project> getAllProjectsActiveOffice() {
		return Collections.unmodifiableSet(activeOffice.getProjectExpert()
				.getAllProjects());
	}

	/**
	 * Returns a list of the projects of all offices
	 * 
	 * @return projects
	 */
	public Set<Project> getAllProjectsAllOffices() {
		Set<Project> projects = new LinkedHashSet<Project>();
		for (BranchOffice office : company.getAllBranchOffices()) {
			projects.addAll(office.getProjectExpert().getAllProjects());
		}
		return Collections.unmodifiableSet(projects);
	}

	/**
	 * Returns the set of all resource types
	 * 
	 * @return resourcetypes : set of all resource types
	 */
	public Set<ResourceType> getAllResourceTypes() {
		return Collections.unmodifiableSet(activeOffice.getResourceExpert()
				.getAllResourceTypes());
	}

	/**
	 * Returns the unmodifiable set of all developers
	 * 
	 * @return developers : set of all developers
	 */
	public Set<Developer> getAllDevelopers() {
		return Collections.unmodifiableSet(activeOffice.getDeveloperExpert()
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
	public Set<Task> getAllTasks() {
		checkActiveOfficeForNull();
		Set<Task> tasks = new HashSet<Task>();
		for (Project project : getAllProjectsActiveOffice()) {
			for (Task task : project.getAllTasks()) {
				if (activeOffice.getPlanner().taskHasPlanning(task)
						&& activeOffice.getPlanner().getPlanning(task)
								.getDevelopers().contains(activeDeveloper)) {
					tasks.add(task);
				}
			}
		}
		return Collections.unmodifiableSet(tasks);
	}

	/**
	 * @param project
	 * @return The BranchOffice responsible for this project
	 */
	public BranchOffice getResponsibleBranch(Project project) {
		for (BranchOffice office : company.getAllBranchOffices()) {
			if (office.getProjectExpert().getAllProjects().contains(project)) {
				return office;
			}
		}
		throw new IllegalArgumentException("Project is not in the system");
	}

	/**
	 * If task is delegated return the branch to which it was delegated else
	 * return the owner of the project
	 * 
	 * @param task
	 * @return The BranchOffice responsible for this task
	 */
	public BranchOffice getResponsibleBranch(Task task) {
		for (BranchOffice office : company.getAllBranchOffices()) {
			if (office.getDelegatedTaskExpert().getAllDelegatedTasks()
					.contains(task)) {
				return office;
			}
		}
		for (BranchOffice office : company.getAllBranchOffices()) {
			for (Project project : office.getProjectExpert().getAllProjects()) {
				if (project.getAllTasks().contains(task)) {
					return office;
				}
			}
		}
		throw new IllegalArgumentException("Project is not in the system");
	}

	/**
	 * Create a BranchOffice
	 * 
	 * @param location
	 * @return the new BranchOffice
	 */
	public BranchOffice createBranchOffice(String location) {
		return company.createBranchOffice(location);
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
	public Project createProject(String name, String description,
			LocalDateTime creationTime, LocalDateTime dueTime) {
		return activeOffice.getProjectExpert().createProject(name, description,
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
	public Project createProject(String name, String description,
			LocalDateTime dueTime) {
		return activeOffice.getProjectExpert().createProject(name, description,
				getTime(), dueTime);
	}

	/**
	 * Creates a new developer with the given name. and adds the new developer
	 * to the set of all developers
	 * 
	 * @param name
	 *            : given name
	 */
	public Developer createDeveloper(String name) {
		return activeOffice.getDeveloperExpert().createDeveloper(name);
	}

	/**
	 * Returns the planning expert
	 * 
	 * @return planningExpert : planning expert
	 */
	public Planner getPlanner() {
		return activeOffice.getPlanner();
	}

	/**
	 * @return The user currently logged in
	 */
	public Developer getActiveDeveloper() {
		return activeDeveloper;
	}

	/**
	 * @return The branch office where the user is currently logged in
	 */
	public BranchOffice getActiveOffice() {
		return activeOffice;
	}

	/**
	 * @return all the BranchOffice's of this company
	 */
	public Set<BranchOffice> getAllOffices() {
		return Collections.unmodifiableSet(company.getAllBranchOffices());
	}

	/**
	 * @return all the tasks that are delegated to the active office
	 */
	public Set<Task> getAllDelegatedTasks() {
		return Collections.unmodifiableSet(activeOffice
				.getDelegatedTaskExpert().getAllDelegatedTasks());
	}

	/**
	 * @param office
	 *            : the office from which you want to receive the tasks that are
	 *            delegated to that office
	 * 
	 * @return all the tasks that are delegated to the office
	 */
	public Set<Task> getAllDelegatedTasksTo(BranchOffice office) {
		return Collections.unmodifiableSet(office.getDelegatedTaskExpert()
				.getAllDelegatedTasks());
	}

	/**
	 * Returns the time
	 * 
	 * @return LocalDateTime : time
	 */
	public LocalDateTime getTime() {
		return taskManClock.getCurrentTime();
	}

	/**
	 * Update the status of all tasks
	 */
	private void updateStatusAll() {
		for (BranchOffice office : company.getAllBranchOffices()) {
			for (Task task : office.getProjectExpert().getAllTasks())
				office.getPlanner().updateStatus(task);
		}
	}

	/**
	 * Sets the active developer to the given developer
	 * 
	 * @param activeDeveloper
	 *            : given developer
	 */
	private void setActiveDeveloper(Developer activeDeveloper) {
		this.activeDeveloper = activeDeveloper;
	}

	/**
	 * Sets the active office to the given branch office
	 * 
	 * @param activeOffice
	 *            : given branch office
	 */
	private void setActiveOffice(BranchOffice activeOffice) {
		this.activeOffice = activeOffice;
	}

	/**
	 * Checks whether the active branch office is null or not. If its null, it
	 * will throw an illegal state exception
	 */
	private void checkActiveOfficeForNull() {
		if (this.activeOffice == null) {
			throw new IllegalStateException("No active branch office");
		}
	}

	/**
	 * Saves the current state of the system. Only the last state is remembered
	 */
	public void saveSystem() {
		for (BranchOffice office : this.getCompany().getAllBranchOffices()) {
			office.saveSystem(this.activeOffice);
		}
	}

	/**
	 * Loads the last saved state of the system
	 */
	public void loadSystem() {
		for (BranchOffice office : this.getCompany().getAllBranchOffices()) {
			office.loadSystem(this.activeOffice);
		}
	}

}
