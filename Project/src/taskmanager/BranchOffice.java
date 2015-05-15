package taskmanager;

/**
 * A branch office is a part of some company with a specific geo- graphical
 * location. Each branch office hosts its own projects, manages its own
 * resources and employs its own employees. A task can be planned for execution
 * at the branch office that hosts the corresponding project or a task can be
 * delegated to another branch office, meaning that the delegated task should be
 * planned for execution at another branch office.
 * 
 * @author Groep 8
 *
 */
public class BranchOffice implements Visitable {

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
	BranchOffice(String location, ImmutableClock clock) {
		// temporary time object
		this.clock = (TaskManClock) clock;
		setLocation(location);
		createDeveloperExpert();
		createResourceExpert();
		createProjectExpert();
		createDelegatedTaskExpert();
		createPlanner();
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
	Planner getPlanner() {
		return this.planner;
	}

	/**
	 * Returns the delegated task expert
	 * 
	 * @return delegatedTaskExpert : delegated task expert
	 */
	DelegatedTaskExpert getDelegatedTaskExpert() {
		return this.delegatedTaskExpert;
	}

	/**
	 * Saves the current state of the system. Only the last state is remembered
	 */
	void saveSystem(BranchOffice office) {
		if (this.equals(office)) {
			this.getProjectExpert().save();
			this.getDeveloperExpert().save();
			this.getPlanner().save();
			this.getResourceExpert().save();
		} else {
			this.getDelegatedTaskExpert().save(office);
		}
	}

	/**
	 * Loads the last saved state of the system
	 */
	void loadSystem(BranchOffice office) {
		if (this.equals(office)) {
			this.getProjectExpert().load();
			this.getDeveloperExpert().load();
			this.getPlanner().load();
			this.getResourceExpert().load();
		} else {
			this.getDelegatedTaskExpert().load(office);
		}
	}

	/**
	 * accept visitor for visiting this
	 */
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
