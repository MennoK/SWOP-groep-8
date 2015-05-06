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
	 * Visit the resource
	 * 
	 * @param resource
	 */
	public void visit(Resource resource);
}
