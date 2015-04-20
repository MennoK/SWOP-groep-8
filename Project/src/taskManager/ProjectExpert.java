package taskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * The projectController class contains a list of all the projects and the
 * internal taskManClock of the system and is able to advance the time. The
 * project controller is allowed to create new projects.
 * 
 * @author Groep 8
 */

public class ProjectExpert implements TimeObserver{

	private ArrayList<Project> projects;
	private LocalDateTime lastUpdateTime;

	/**
	 * The constructor of the projectController needs a date time.
	 * 
	 * @param now
	 *            : the time at which the ProjectController is created
	 */
	public ProjectExpert() {
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
	public void createProject(String name, String description,
			LocalDateTime creationTime, LocalDateTime dueTime) {
		Project project = new Project(name, description, creationTime, dueTime);
		project.handleTimeChange(lastUpdateTime);
		this.addProject(project);
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
	public void createProject(String name, String description,
			LocalDateTime dueTime) {
		this.createProject(name, description, lastUpdateTime, dueTime);
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
	boolean canHaveProject(Project project) {
		return (!getAllProjects().contains(project) && project != null);
	}
	
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
	public List<Project> getAllProjects() {
		return Collections.unmodifiableList(projects);
	}
	
}
