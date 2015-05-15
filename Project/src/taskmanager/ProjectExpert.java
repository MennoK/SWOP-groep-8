package taskmanager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * 
 * The projectController class contains a list of all the projects and the
 * internal taskManClock of the system and is able to advance the time. The
 * project controller is allowed to create new projects.
 * 
 * @author Groep 8
 */

public class ProjectExpert {

	private Set<Project> projects;

	private Memento memento;
	
	private final ImmutableClock clock;

	/**
	 * The constructor of the projectController needs a date time.
	 * 
	 * @param now
	 *            : the time at which the ProjectController is created
	 */
	ProjectExpert(ImmutableClock clock) {
		this.clock = clock;
		projects = new LinkedHashSet<>();

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
		Project project = new Project(name, description, creationTime, dueTime, clock);
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
	@NonNull
	private void addProject(Project project) throws IllegalArgumentException {
		if (getAllProjects().contains(project)) {
			throw new IllegalArgumentException(
					"The given project is already in this project.");
		} else {
			projects.add(project);
		}
	}

	/**
	 * Returns a list of the projects
	 * 
	 * @return projects: list of projects
	 */
	Set<Project> getAllProjects() {
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
	 * Saves the current state of the project expert
	 */
	void save() {
		this.memento = new Memento();
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
			this.memento.load();
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
		private Set<Project> projects;

		/**
		 * Constructor of the momento inner class of project expert. Initialize
		 * a new set of project of the current state and saves the last update
		 * time
		 * 
		 * @param pe
		 *            : projectExpert
		 */
		public Memento() {
			this.projects = new LinkedHashSet<Project>(
					ProjectExpert.this.projects);
		}

		/**
		 * Sets the project set of the project expert class to the saved set of
		 * the momento class
		 * 
		 * @param pe
		 *            : projectExpert
		 */
		public void load() {
			ProjectExpert.this.projects = this.projects;
		}
	}

}
