package ui;

import taskmanager.BranchOffice;
import taskmanager.Developer;
import taskmanager.Project;
import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.Task;
import taskmanager.Visitable;
import taskmanager.Visitor;

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

	@Override
	public void visit(ResourceType resourceType) {
		summary = resourceType.getName();
	}

	@Override
	public void visit(Resource resource) {
		summary = resource.getName();
	}

	@Override
	public void visit(BranchOffice office) {
		summary = office.getLocation();
	}

}
