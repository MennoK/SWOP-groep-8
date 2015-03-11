package ui;

import java.time.LocalDateTime;
import java.util.List;

import javax.activity.InvalidActivityException;

import taskManager.Project;
import taskManager.ProjectStatus;
import taskManager.Task;
import taskManager.TaskFinishedStatus;

public class Printer {

	static String oneLine(Task task) {
		return "Task " + task.getId() + " " + task.getStatus();
	}

	static String oneLine(Project project) {
		return "project '" + project.getName() + "': " + project.getStatus();
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

	static String full(Task task) {
		String str = oneLine(task) + ": ";
		str += task.getDescription() + ", ";
		str += task.getEstimatedDuration() + ", ";
		str += task.getAcceptableDeviation() * 100 + "% margin";
		if (!task.getDependencies().isEmpty()) {
			str += ", depends on {";
			for (Task dep : task.getDependencies())
				str += " task " + dep.getId();
			str += " }";
		}
		if (task.getAlternativeFor() != null)
			str += ", alternative for task " + task.getAlternativeFor().getId();
		try {
			TaskFinishedStatus finishStatus = task.getFinishStatus();
			str += ", started " + task.getStartTime();
			str += ", finished " + task.getEndTime();
			str += " (" + finishStatus + ")";
		} catch (InvalidActivityException e) {
			// If not finished
		}
		return str;
	}

	static String full(Project project, LocalDateTime now) {
		String str = "project name: " + project.getName() + "\n";
		str += "description: " + project.getDescription() + "\n";
		str += "creation time: " + project.getCreationTime() + "\n";
		str += "due time: " + project.getDueTime() + "\n";
		str += "status: " + project.getStatus() + "\n";
		if (project.getStatus() == ProjectStatus.ONGOING)
			str += "The project is estimated to finish "
					+ project.willFinishOnTime() + "\n";
		if (project.getStatus() == ProjectStatus.FINISHED) {
			str += "The total delay was: " + project.getTotalDelay() + "\n";
			str += "The project finished " + project.finishedOnTime() + "\n";
		}
		str += "The project contains the following tasks:\n";
		str += listTasks(project.getAllTasks());
		return str;
	}
}
