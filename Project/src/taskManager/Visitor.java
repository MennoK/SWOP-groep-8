package taskManager;

public interface Visitor {
	public void visit(Task task);

	public void visit(Project project);

	public void visit(Developer developer);
}
