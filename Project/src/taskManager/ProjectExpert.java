package taskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * The projectController class contains a list of all the projects and the
 * internal taskManClock of the system and is able to advance the time. The
 * project controller is allowed to create new projects.
 * 
 * @author Groep 8
 */

public class ProjectExpert implements TimeObserver {

	private ArrayList<Project> projects;

	private Memento memento;
	private LocalDateTime lastUpdateTime;

	/**
	 * The constructor of the projectController needs a date time.
	 * 
	 * @param now
	 *            : the time at which the ProjectController is created
	 */
	ProjectExpert() {
		projects = new ArrayList<>();
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
	Project createProject(String name, String description,
			LocalDateTime creationTime, LocalDateTime dueTime) {
		Project project = new Project(name, description, creationTime, dueTime);
		project.handleTimeChange(lastUpdateTime);
		addProject(project);
		return project;
	}

	/**
	 * Adds a given project to the list of projects. An IllegalArgumentException
	 * will be thrown when the given project is invalid
	 * 
	 * @param project
	 *            : project to be added
	 * @throws IllegalArgumentException
	 *             : thrown when the given project is not valid
	 */
	private void addProject(Project project) throws IllegalArgumentException {
		if (!canHaveProject(project)) {
			throw new IllegalArgumentException(
					"The given project is already in this project.");
		} else {
			projects.add(project);
		}
	}

	/**
	 * Determines if the project controller can have the given project. This is
	 * true if and only if the given project is not yet in the project
	 * controller and the project is not null
	 * 
	 * @param project
	 *            : given project to be added
	 * @return true if and only if the project controller does not contain the
	 *         project yet and the project is not null
	 */
	private boolean canHaveProject(Project project) {
		return (!getAllProjects().contains(project) && project != null);
	}

	/**
	 * Handles time changes with changing time in every project of the
	 * projectExpert
	 */
	@Override
	public void handleTimeChange(LocalDateTime time) {
		this.lastUpdateTime = time;
		for (Project project : this.getAllProjects()) {
			project.handleTimeChange(time);
		}
	}

	/**
	 * Returns a list of the projects
	 * 
	 * @return projects: list of projects
	 */
	List<Project> getAllProjects() {
		return projects;
	}

	/**
	 * Returns all tasks in all projects
	 * 
	 * @return All the tasks in all projects
	 */
	Set<Task> getAllTasks() {
		Set<Task> tasks = new HashSet<Task>();
		for (Project project : getAllProjects()) {
			tasks.addAll(project.getAllTasks());
		}
		return tasks;
	}

	/**
	 * 
	 * Returns a set with all tasks of a given developer
	 * 
	 * @param dev
	 *            the active Developer
	 * @return All the tasks to which this developer is assigned.
	 */
	Set<Task> getAllTasks(Developer dev) {
		Set<Task> tasks = new HashSet<Task>();
		for (Project project : getAllProjects()) {
			for (Task task : project.getAllTasks()) {
				if (task.hasPlanning()
						&& task.getPlanning().getDevelopers().contains(dev)) {
					tasks.add(task);
				}
			}
		}
		return tasks;
	}

	/**
	 * Saves the current state of the project expert
	 */
	void save() {
		this.memento = new Memento(this);
		for (Project project : this.projects) {
			project.save();
		}
	}

	/**
	 * Loads the last saved state
	 * 
	 * @return last state of project expert
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load(this);
			for (Project project : this.projects) {
				project.load();
			}
		}
	}

	/**
	 * 
	 * Inner momento class of project expert
	 * 
	 * @author groep 8
	 */
	private class Memento {
		private ArrayList<Project> projects;
		private LocalDateTime lastUpdateTime;

		/**
		 * Constructor of the momento inner class of project expert. Initialize
		 * a new set of project of the current state and saves the last update
		 * time
		 * 
		 * @param pe
		 *            : projectExpert
		 */
		public Memento(ProjectExpert pe) {
			this.projects = new ArrayList<Project>(pe.projects);
			this.lastUpdateTime = pe.lastUpdateTime;
		}

		/**
		 * Sets the project set of the project expert class to the saved set of
		 * the momento class
		 * 
		 * @param pe
		 *            : projectExpert
		 */
		public void load(ProjectExpert pe) {
			pe.projects = this.projects;
			pe.lastUpdateTime = this.lastUpdateTime;
		}
	}

}
