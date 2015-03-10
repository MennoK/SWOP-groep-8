package ui;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import parser.Parser;
import TaskManager.InvalidTimeException;
import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.Task;
import TaskManager.TaskManClock;

public class UiTaskMan {

	private ProjectController projectController;
	private Reader reader;

	private void askInitialState() {
		while (true) {
			String fileName = reader
					.getString("Give a file for initialisation of the system:\n"
							+ "(press enter to use ./input.tman)\n"
							+ "(give '0' to initilise as empty)");
			if (fileName.equals("0")) {
				System.out.println("Starting with an empty system");
				return;
			}
			if (fileName.equals(""))
				fileName = "./input.tman";
			try {
				Parser parser = new Parser();
				parser.parse(fileName, projectController);
				System.out.println("Starting with system initialised from "
						+ fileName);
				return;
			} catch (FileNotFoundException | RuntimeException
					| LoopingDependencyException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	UiTaskMan() {
		reader = new Reader();
		TaskManClock clock = new TaskManClock(
				reader.getDate("Give the current time:"));
		System.out.println("Current time initialized on:\n" + clock.getTime()
				+ "\n");
		projectController = new ProjectController(clock);
		askInitialState();
	}

	private void showProjects() {
		// TODO move listProjects to a generic list and move it inside select
		System.out.println(Printer.listProjects(projectController
				.getAllProjects()));
		Project project = reader.select(projectController.getAllProjects());
		System.out.println(Printer.full(project, projectController.getTime()));
		Task task = reader.select(project.getAllTasks());
		System.out.println(Printer.full(task));
	}

	private void createProject() {
		System.out.println("Creating a project\n"
				+ "Please fill in the following form:");
		String name = reader.getString("name");
		String description = reader.getString("description");
		LocalDateTime dueTime = reader.getDate("due time");
		if (dueTime == null) {
			System.out.println("Project creation aborted.");
			return;
		}
		projectController.createProject(name, description,
				projectController.getTime(), dueTime);
	}

	private void createTask() {
		System.out.println("Creating a task\n"
				+ "Please fill in the following form:");
		Project project = reader.select(projectController.getAllProjects());
		ArrayList<Task> tasks = new ArrayList<Task>();
		while (reader.getBoolean("Do you want to add a dependence?")) {
			tasks.add(reader.select(project.getAllTasks()));
		}
		// TODO add dep to new Task
		project.createTask(reader.getString("Give a description:"),
				reader.getDuration("Give an estimate for the task duration:"),
				reader.getDouble("Give an acceptable deviation:"));
	}

	private void updateTaskStatus() {
		System.out.println("TODO");
	}

	private void advanceTime() {
		while (true) {
			try {
				projectController.advanceTime(reader
						.getDate("Enter the new timestamp:"));
				return;
			} catch (InvalidTimeException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void printMenu() {
		System.out.println("\nMain menu:\n" + "1: Show projects\n"
				+ "2: Create project\n" + "3: Create task\n"
				+ "4: Update task status\n" + "5: Advance time\n" + "0: Exit");
	}

	void menu() {
		while (true) {
			printMenu();
			String choice = reader.getString("Select an option");
			switch (choice) {
			case "0":
				reader.close();
				return;
			case "1":
				showProjects();
				break;
			case "2":
				createProject();
				break;
			case "3":
				createTask();
				break;
			case "4":
				updateTaskStatus();
				break;
			case "5":
				advanceTime();
				break;
			default:
				System.out.println("Invalid choice, try again. (0 to exit)");
				break;
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("Welcome to TaskMan");
		UiTaskMan ui = new UiTaskMan();
		ui.menu();
		System.out.println("Goodbye");
	}

}
