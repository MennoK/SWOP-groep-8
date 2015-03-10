package ui;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
	private Scanner scan;

	private void askInitialState() {
		while (true) {
			System.out
					.println("Give a file for initialisation of the system:\n"
							+ "(press enter to use ./input.tman)\n"
							+ "(give '0' to initilise as empty)");
			String dateInput = scan.nextLine();
			if (dateInput.equals("0")) {
				System.out.println("Starting with an empty system");
				return;
			}
			if (dateInput.equals(""))
				dateInput = "./input.tman";
			try {
				Parser parser = new Parser();
				parser.parse(dateInput, projectController);
				System.out.println("Starting with system initialised from "
						+ dateInput);
				return;
			} catch (FileNotFoundException | RuntimeException
					| LoopingDependencyException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	UiTaskMan() {
		scan = new Scanner(System.in);
		reader = new Reader(scan);
		TaskManClock clock = new TaskManClock(
				reader.getDate("Give the current time:"));
		System.out.println("Current time initialized on:\n" + clock.getTime()
				+ "\n");
		projectController = new ProjectController(clock);
		askInitialState();
	}

	private void printProjects() {
		List<Project> projects = projectController.getAllProjects();
		for (int i = 0; i < projects.size(); i++)
			System.out.println((i + 1) + ": project '"
					+ projects.get(i).getName() + "' is "
					+ projects.get(i).getStatus());
	}

	private Project selectProject() {
		while (true) {
			System.out.println("select a project:");
			printProjects();
			try {
				int projectIndex = Integer.parseInt(scan.nextLine());
				return projectController.getAllProjects().get(projectIndex - 1);
			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer");
			}
		}
	}

	private void printTasks(Project project) {
		List<Task> tasks = project.getAllTasks();
		for (int i = 0; i < tasks.size(); i++)
			System.out.println((i + 1) + ": task '"
					+ tasks.get(i).getDescription() + "' is "
					+ tasks.get(i).getStatus());
	}

	private Task selectTask(Project project) {
		while (true) {
			System.out.println("select a task:");
			printTasks(project);
			try {
				int taskIndex = Integer.parseInt(scan.nextLine());
				return project.getAllTasks().get(taskIndex - 1);
			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer");
			}
		}
	}

	private void showProjects() {
		Project project = selectProject();
		System.out.println(Printer.full(project, projectController.getTime()));
		Task task = selectTask(project);
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
		Project project = selectProject();
		ArrayList<Task> tasks = new ArrayList<Task>();
		while (reader.getBoolean("Do you want to add a dependence?")) {
			tasks.add(selectTask(project));
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
		System.out.println("Main menu:\n" + "1: Show projects\n"
				+ "2: Create project\n" + "3: Create task\n"
				+ "4: Update task status\n" + "5: Advance time\n" + "0: Exit");
	}

	void menu() {
		while (true) {
			printMenu();
			String choice = scan.nextLine();
			switch (choice) {
			case "0":
				scan.close();
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
