package ui;

import java.util.List;

import taskManager.Project;
import taskManager.ProjectFinishingStatus;
import taskManager.Task;
import taskManager.TaskFinishedStatus;

public class Printer {

	static String oneLine(Project project) {
		return "project '" + project.getName() + "': " + project.getStatus();
	}

	static String listTasks(List<Task> options) {
		return listTasks(options, 1);
	}

	static String listTasks(List<Task> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + options.get(i).toSummary()
					+ "\n";
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
