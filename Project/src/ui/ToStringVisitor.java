package ui;

import taskManager.Developer;
import taskManager.Project;
import taskManager.ProjectFinishingStatus;
import taskManager.Task;
import taskManager.TaskFinishedStatus;
import taskManager.TaskStatus;
import taskManager.Visitable;
import taskManager.Visitor;

public class ToStringVisitor implements Visitor {

	private String str;

	public String create(Visitable visitable) {
		visitable.accept(this);
		return str;
	}

	@Override
	public void visit(Task task) {
		str = new SummerizingVisitor().createSummary(task) + ": ";
		str += task.getDescription() + ", ";
		str += task.getEstimatedDuration().toHours() + " hours, ";
		str += task.getAcceptableDeviation() * 100 + "% margin";
		if (!task.getDependencies().isEmpty()) {
			str += ", depends on {";
			for (Task dep : task.getDependencies())
				str += " task " + dep.getId();
			str += " }";
		}
		if (task.getOriginal() != null)
			str += ", alternative for task " + task.getOriginal().getId();
		if (task.getStatus() == TaskStatus.FINISHED) {
			TaskFinishedStatus finishStatus = task.getFinishStatus();
			str += ", started " + task.getStartTime();
			str += ", finished " + task.getEndTime();
			str += " (" + finishStatus + ")";
		}
	}

	@Override
	public void visit(Project project) {
		str = new SummerizingVisitor().createSummary(project);
		str += ", " + project.getDescription();
		str += ", " + project.finishedOnTime();
		str += " (Created " + project.getCreationTime();
		str += ", Due " + project.getDueTime();
		if (project.finishedOnTime() == ProjectFinishingStatus.OVER_TIME)
			str += "(" + project.getCurrentDelay().toHours()
					+ " working hours short)";
		str += ")\n";
		str += Printer.list(project.getAllTasks());
	}

	@Override
	public void visit(Developer developer) {
		throw new UnsupportedOperationException("Not implemented!");
	}

}
