package taskManager;

import java.time.LocalDateTime;

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
	private PlanningExpert planningExpert;

	/**
	 * Constructor of TaskManController. When a new TaskManController has been
	 * created new expert classes will be created.
	 */
	public TaskManController(LocalDateTime now) {
		createDeveloperExpert();
		createResourceExpert();
		createProjectExpert(now);
		createPlanningExpert();
	}

	/**
	 * Creates a new planning expert
	 */
	private void createPlanningExpert() {
		this.planningExpert = new PlanningExpert();
	}

	/**
	 * Creates a new project expert
	 */
	private void createProjectExpert(LocalDateTime now) {
		this.projectExpert = new ProjectExpert(now);
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
	public PlanningExpert getPlanningExpert() {
		return planningExpert;
	}

	/**
	 * Saves the current state of the system. Only the last state is remembered
	 */
	public void saveSystem() {
		this.getProjectExpert().save();
		this.getDeveloperExpert().save();
		this.getPlanningExpert().save();
		this.getResourceExpert().save();
	}

	/**
	 * Loads the last saved state of the system
	 * 
	 * @return true if loaded, false if no previous state is found
	 */
	public boolean loadSystem() {
		return this.getProjectExpert().load()
				&& this.getDeveloperExpert().load()
				&& this.getPlanningExpert().load()
				&& this.getResourceExpert().load();
	}

}
