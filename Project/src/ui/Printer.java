package ui;

import java.time.LocalDateTime;
import java.util.List;

import TaskManager.Project;
import TaskManager.ProjectStatus;
import TaskManager.Task;

public class Printer {

	static String oneLine(Task task) {
		return "task with description '" + task.getDescription() + "' is "
				+ task.getStatus();
	}

	static String oneLine(Project project) {
		return "project '" + project.getName() + "' is " + project.getStatus();
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
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + 1) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	static String full(Task task) {
		String str = "description: " + task.getDescription() + "\n";
		str += "estimated duration: " + task.getEstimatedDuration() + "\n";
		str += "acceptable deviation: " + task.getAcceptableDeviation() + "\n";
		str += "status: " + task.getStatus() + "\n";
		/* TODO display whether task was finished early, on time or with delay. */
		if (task.getAlternativeFor() != null)
			str += "Alternative task is: " + task.getAlternativeFor() + "\n";
		if (!task.getDependencies().isEmpty())
			str += "dependencies:\n";
		str += listTasks(task.getDependencies());
		return str;
	}

	// TODO replace by to be written method in Project
	static boolean willFinishOnTime(Project project, LocalDateTime now) {
		return project.getEstimatedFinishTime(now)
				.isAfter(project.getDueTime());
	}

	static String full(Project project, LocalDateTime now) {
		String str = "project name: " + project.getName() + "\n";
		str += "description: " + project.getDescription() + "\n";
		str += "creation time: " + project.getCreationTime() + "\n";
		str += "due time: " + project.getDueTime() + "\n";
		str += "status: " + project.getStatus() + "\n";
		if (project.getStatus() == ProjectStatus.ONGOING) {
			if (willFinishOnTime(project, now))
				str += "The project is estimated to finish over time\n";
			else
				str += "the project is estimated to finish on time\n";
		}
		if (project.getStatus() == ProjectStatus.FINISHED) {
			str += "The total delay was: " + project.getTotalDelay() + "\n";
			/*
			 * TODO print whether the project finished early, on time or with
			 * delay, this requires project.getFinishTime() or
			 * project.isFinishedOnTime()
			 */
		}
		str += "The project contains the following tasks:\n";
		str += listTasks(project.getAllTasks());
		return str;
	}
}
