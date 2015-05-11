package taskmanager;

public class TaskManController {
	private Company company;
	private BranchOffice activeOffice;
	private Developer activeDeveloper;

	public TaskManController() {
		company = new Company();
	}

	/**
	 * Log the user in and set his Branch office
	 * 
	 * @param activeDeveloper
	 * @param activeOffice
	 */
	public void logIn(Developer activeDeveloper, BranchOffice activeOffice) {
		setActiveDeveloper(activeDeveloper);
		setActiveOffice(activeOffice);
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

	private void setActiveDeveloper(Developer activeDeveloper) {
		this.activeDeveloper = activeDeveloper;
	}

	private void setActiveOffice(BranchOffice activeOffice) {
		this.activeOffice = activeOffice;
	}
}
