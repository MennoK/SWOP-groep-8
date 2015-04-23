package ui;

import taskManager.Developer;
import taskManager.Project;
import taskManager.Task;
import taskManager.Visitable;
import taskManager.Visitor;

public class SummerizingVisitor implements Visitor {

	private String summary;

	public String createSummary(Visitable visitable) {
		visitable.accept(this);
		return summary;
	}

	@Override
	public void visit(Task task) {
		summary = "Task " + task.getId() + " " + task.getStatus();
	}

	@Override
	public void visit(Project project) {
		summary = "project '" + project.getName() + "': " + project.getStatus();
	}

	@Override
	public void visit(Developer developer) {
		summary = developer.getName();
	}

}
