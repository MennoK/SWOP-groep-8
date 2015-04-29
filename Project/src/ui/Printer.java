package ui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import taskManager.Developer;
import taskManager.Project;
import taskManager.ProjectFinishingStatus;
import taskManager.ResourceType;
import taskManager.Task;
import taskManager.TaskFinishedStatus;
import taskManager.TaskStatus;
import taskManager.Visitable;

public class Printer {

	static <T extends Visitable> String list(Collection<T> options) {
		return list(options, 1);
	}

	static <T extends Visitable> String list(Collection<T> options,
			int startingIndex) {
		List<T> optionsList = new ArrayList<T>(options);
		SummerizingVisitor summarizingVisitor = new SummerizingVisitor();
		String str = "";
		for (int i = 0; i < optionsList.size(); i++) {
			str += (i + startingIndex) + ": "
					+ summarizingVisitor.createSummary(optionsList.get(i))
					+ "\n";
		}
		return str.trim();
	}

	static String listDates(List<LocalDateTime> options) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (1 + i) + ": " + options.get(i) + "\n";
		}
		return str.trim();
	}

	static String print(Map<ResourceType, Integer> requirredRessources) {
		String str = "Requirrements:\n";
		for (ResourceType type : requirredRessources.keySet()) {
			str += type.getName() + " " + requirredRessources.get(type) + "\n";
		}
		return str.trim();
	}

	@Deprecated
	static String oneLine(Task task) {
		return "Task " + task.getId() + " " + task.getStatus();
	}

	@Deprecated
	static String oneLine(Project project) {
		return "project '" + project.getName() + "': " + project.getStatus();
	}

	@Deprecated
	static String oneLine(Developer developer) {
		return developer.getName();
	}

	@Deprecated
	static String listTasks(List<Task> options) {
		return listTasks(options, 1);
	}

	@Deprecated
	static String listTasks(List<Task> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	@Deprecated
	static String listProjects(List<Project> options) {
		return listProjects(options, 1);
	}

	@Deprecated
	static String listProjects(List<Project> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	@Deprecated
	static String listDevelopers(List<Developer> options) {
		return listDevelopers(options, 1);
	}

	@Deprecated
	static String listDevelopers(List<Developer> options, int startingIndex) {
		String str = "";
		for (int i = 0; i < options.size(); i++) {
			str += (i + startingIndex) + ": " + oneLine(options.get(i)) + "\n";
		}
		return str.trim();
	}

	@Deprecated
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
		if (task.getStatus() == TaskStatus.FINISHED) {
			TaskFinishedStatus finishStatus = task.getFinishStatus();
			str += ", started " + task.getStartTime();
			str += ", finished " + task.getEndTime();
			str += " (" + finishStatus + ")";
		}
		return str;
	}

	@Deprecated
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