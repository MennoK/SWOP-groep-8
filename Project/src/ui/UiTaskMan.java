package ui;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import parser.Parser;
import taskManager.Project;
import taskManager.Task;
import taskManager.Task.TaskBuilder;
import taskManager.TaskManController;
import ui.exception.ExitUseCaseException;
import utility.Utility;

public class UiTaskMan {

	private TaskManController projectController;
	private Reader reader;

	private void askInitialState() {
		while (true) {
			String fileName;
			try {
				fileName = reader
						.getString("Give a file for initialisation of the system:\n"
								+ "(press 1 to use ./input1.tman)\n"
								+ "(press 2 to use ./input2.tman)");
			} catch (ExitUseCaseException e1) {
				System.out.println("Starting with an empty system");
				return;
			}
			if (fileName.equals("1"))
				fileName = "./input1.tman";
			else if (fileName.equals("2"))
				fileName = "./input2.tman";
			try {
				Parser parser = new Parser();
				parser.parse(fileName, projectController);
				System.out.println("Starting with system initialised from "
						+ fileName);
				return;
			} catch (IllegalArgumentException | FileNotFoundException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	UiTaskMan() {
		reader = new Reader();
		LocalDateTime now = LocalDateTime.of(2015, 2, 9, 8, 0);
		try {
			now = reader.getDate("Give the current time:");
		} catch (ExitUseCaseException e) {
		}

		System.out.println("Current time initialized on:\n" + now + "\n");
		projectController = new TaskManController(now);
		askInitialState();
	}

	private void showProjects() throws ExitUseCaseException {
		// TODO move listProjects to a generic list and move it inside select
		System.out.println(Utility.listSummaries(projectController
				.getProjectExpert().getAllProjects(), 1));
		Project project = reader.select(projectController.getProjectExpert()
				.getAllProjects());
		System.out.println(project);
		Task task = reader.select(project.getAllTasks());
		System.out.println(task);
	}

	private void createProject() throws ExitUseCaseException {
		System.out.println("Creating a project\n"
				+ "Please fill in the following form:");
		String name = reader.getString("name");
		String description = reader.getString("description");
		LocalDateTime dueTime = reader.getDate("due time");
		if (dueTime == null) {
			System.out.println("Project creation aborted.");
			return;
		}
		projectController.getProjectExpert().createProject(name, description,
				dueTime);
	}

	private void createTask() throws ExitUseCaseException {
		while (true) {
			System.out.println("Creating a task\n"
					+ "Please fill in the following form:\n"
					+ "Adding task to which project?");
			System.out.println(Utility.listSummaries(projectController
					.getProjectExpert().getAllProjects(), 1));
			Project project = reader.select(projectController
					.getProjectExpert().getAllProjects());
			TaskBuilder builder = project.createTask(reader
					.getString("Give a description:"), reader
					.getDuration("Give an estimate for the task duration:"),
					reader.getDouble("Give an acceptable deviation:"));
			while (reader
					.getBoolean("Is this task dependent on an (other) task?")) {
				System.out.println(Utility.listSummaries(project.getAllTasks(),
						1));
				builder.addDependencies(reader.select(project.getAllTasks()));
			}
			if (reader.getBoolean("Is this an alternative to a failled task?")) {
				System.out.println(Utility.listSummaries(project.getAllTasks(),
						1));
				builder.setOriginalTask(reader.select(project.getAllTasks()));
			}
			while (reader
					.getBoolean("Does this task require and (other) ressources?")) {
				// TODO
			}
			builder.build();
			return;
		}
	}

	private void planTask() throws ExitUseCaseException {
		System.out.println("TODO implement plan task");
	}

	private void updateTaskStatus() throws ExitUseCaseException {
		System.out.println("Updating the status of a task\n"
				+ "Please select a task:");
		ArrayList<Task> allTasks = new ArrayList<Task>();
		for (Project project : projectController.getProjectExpert()
				.getAllProjects()) {
			System.out.println(project.toSummary());
			System.out.println(Utility.listSummaries(project.getAllTasks(),
					allTasks.size() + 1));
			allTasks.addAll(project.getAllTasks());
		}
		Task task = reader.select(allTasks);

		while (true) {
			try {
				task.updateStatus(
						reader.getDate("When did you start this task?"),
						reader.getDate("When did you finish this task?"),
						reader.getBoolean("Was this task failed?"));
				return;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void advanceTime() throws ExitUseCaseException {
		while (true) {
			try {
				projectController.getProjectExpert().advanceTime(
						reader.getDate("Enter the new timestamp:"));
				return;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void runSimulation() throws ExitUseCaseException {
		System.out.println("TODO implement run simulation");
	}

	private void printMenu() {
		System.out
				.println("\nMain menu:\n" + "1: Show projects\t"
						+ "2: Create project\t" + "3: Create task\n"
						+ "4: Plan task\t\t" + "5: Update task status\t"
						+ "6: Advance time\n" + "7: Run simulation\t\t\t\t"
						+ "9: Exit");
	}

	void menu() {
		while (true) {
			try {
				printMenu();
				String choice = reader.getString("Select an option");
				switch (choice) {
				case "9":
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
					planTask();
					break;
				case "5":
					updateTaskStatus();
					break;
				case "6":
					advanceTime();
					break;
				case "7":
					runSimulation();
					break;
				default:
					System.out
							.println("Invalid choice, try again. (0 to exit)");
					break;
				}
			} catch (ExitUseCaseException e) {
				System.out
						.println("Use case exited, returning to the main menu.");
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("Welcome to TaskMan");
		new UiTaskMan().menu();
		System.out.println("Goodbye!");
	}

}
