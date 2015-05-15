package taskmanager;

public interface Visitor {

	/**
	 * Visit the task
	 * 
	 * @param task
	 */
	public void visit(Task task);

	/**
	 * Visit the project
	 * 
	 * @param project
	 */
	public void visit(Project project);

	/**
	 * Visit the developer
	 * 
	 * @param developer
	 */
	public void visit(Developer developer);

	/**
	 * Visit the resourceType
	 * 
	 * @param resourceType
	 */
	public void visit(ResourceType resourceType);

	/**
	 * Visit the resource
	 * 
	 * @param resource
	 */
	public void visit(Resource resource);

	/**
	 * Visit the office
	 * 
	 * @param office
	 */
	public void visit(BranchOffice office);

	/**
	 * Visit the plan
	 * 
	 * @param plan
	 */
	public void visit(Planning plan);
}
