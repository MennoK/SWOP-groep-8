package ui;

import java.util.List;

import taskManager.Developer;
import taskManager.Project;
import taskManager.ProjectFinishingStatus;
import taskManager.Task;
import taskManager.TaskFinishedStatus;
import taskManager.Visitable;

public class Printer {

	static <T extends Visitable> String list(List<T> options) {
		return list(options, 1);
	}

	static <T extends Visitable> String list(List<T> options, int startingIndex) {
		SummerizingVisitor summarizingVisitor = new SummerizingVisitor();
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": "
					+ summarizingVisitor.createSummary(options.get(i)) + "\n";
		}
		return str.trim();
	}

	static String oneLine(Task task) {
		return "Task " + task.getId() + " " + task.getStatus();
	}

	static String oneLine(Project project) {
		return "project '" + project.getName() + "': " + project.getStatus();
	}

	static String oneLine(Developer developer) {
		return developer.getName();
	}

	static String listTasks(List<Task> options) {
		return listTasks(options, 1);
	}

	static String listTasks(List<Task> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	static String listProjects(List<Project> options) {
		return listProjects(options, 1);
	}

	static String listProjects(List<Project> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	static String listDevelopers(List<Developer> options) {
		return listDevelopers(options, 1);
	}

	static String listDevelopers(List<Developer> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	static String full(Task task) {
		String str = oneLine(task) + ": ";
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
		try {
			TaskFinishedStatus finishStatus = task.getFinishStatus();
			str += ", started " + task.getStartTime();
			str += ", finished " + task.getEndTime();
			str += " (" + finishStatus + ")";
		} catch (IllegalArgumentException e) {
			// If not finished
		}
		return str;
	}

	static String full(Project project) {
		String str = oneLine(project);
		str += ", " + project.getDescription();
		str += ", " + project.finishedOnTime();
		str += " (Created " + project.getCreationTime();
		str += ", Due " + project.getDueTime();
		if (project.finishedOnTime() == ProjectFinishingStatus.OVER_TIME)
			str += "(" + project.getCurrentDelay().toHours()
					+ " working hours short)";
		str += ")\n";
		str += listTasks(project.getAllTasks());
		return str;
	}
}