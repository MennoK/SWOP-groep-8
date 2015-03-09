package ui;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import parser.Parser;
import TaskManager.LoopingDependencyException;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.Task;
import TaskManager.TaskManClock;

public class UiTaskMan {

	private ProjectController projectController;
	private Scanner scan;

	private TaskManClock askCurrentTime() {
		while (true) {
			System.out.println("Give the current time: 'yyyy-mm-ddThh:mm:ss'\n"
					+ "(press enter to set 09/02/2015, 08:00:00)");
			String dateInput = scan.nextLine();
			if (dateInput.equals(""))
				return new TaskManClock(LocalDateTime.of(2015, 2, 9, 8, 0));
			try {
				return new TaskManClock(LocalDateTime.parse(dateInput));
			} catch (java.time.format.DateTimeParseException e) {
				System.out.println("Invalid Local date time input.");
			}
		}
	}

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
		TaskManClock clock = askCurrentTime();
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
			System.out.println("select a project: (0 to return to the menu)");
			printProjects();
			try {
				int projectIndex = Integer.parseInt(scan.nextLine());
				return projectController.getAllProjects().get(projectIndex - 1);
			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer to select a project");
			}
		}
	}

	private void printProject(Project project) {
		System.out.println("project name: " + project.getName());
		System.out.println("description: " + project.getDescription());
		System.out.println("creation time: "
				+ project.getCreationTime().toString());
		System.out.println("due time: " + project.getDueTime().toString());
		System.out.println("status: " + project.getStatus());
		List<Task> tasks = project.getAllTasks();
		for (int i = 0; i < tasks.size(); i++)
			System.out.println((i + 1) + ": task '"
					+ tasks.get(i).getDescription() + "' is "
					+ tasks.get(i).getStatus());
	}

	private Task selectTask(Project project) {
		while (true) {
			System.out.println("select a task: (0 to return to the menu)");
			try {
				int taskIndex = Integer.parseInt(scan.nextLine());
				return project.getAllTasks().get(taskIndex - 1);
			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer to select a project");
			}
		}
	}

	private void printTask(Task task) {
		System.out.println("description:" + task.getDescription());
	}

	private void showProjects() {
		Project project = selectProject();
		printProject(project);
		Task task = selectTask(project);
		printTask(task);
	}

	private String getStringFromUser(String querry) {
		System.out.println(querry + ":");
		return scan.nextLine();
	}

	private LocalDateTime getDateFromUser(String querry) {
		System.out.println(querry + ": (format: 'yyyy-mm-ddThh:mm:ss')");
		try {
			return LocalDateTime.parse(scan.nextLine());
		} catch (java.time.format.DateTimeParseException e) {
			System.out.println("The given date was invalid");
			return null;
		}
	}

	private void createProject() {
		System.out.println("Creating a project\n"
				+ "Please fill in the following form:");
		String name = getStringFromUser("name");
		String description = getStringFromUser("description");
		LocalDateTime dueTime = getDateFromUser("due time");
		if (dueTime == null) {
			System.out.println("Project creation aborted.");
			return;
		}
		projectController.createProject(name, description,
				projectController.getTime(), dueTime);
	}

	private Duration getDurationFromUser(String querry) {
		System.out.println("Give an " + querry + "in hours:");
		int numHours = scan.nextInt();
		scan.nextLine();
		return Duration.ofHours(numHours);
	}

	private double getDoubleFromUser(String querry) {
		System.out.println("Give an " + querry + " (double)");
		double val = scan.nextDouble();
		scan.nextLine();
		return val;
	}

	private void createTask() {
		System.out.println("Creating a task\n"
				+ "Please fill in the following form:");
		System.out.println("select a project:");
		printProjects();
		Project project = selectProject();
		Task task = new Task(getStringFromUser("description"),
				getDurationFromUser("estimated task duration"),
				getDoubleFromUser("acceptable deviation"));
		project.addTask(task);

	}

	private void updateTaskStatus() {
		System.out.println("TODO UC: update task status!");
	}

	private void advanceTime() {
		System.out.println("TODO UC: advance time!");
	}

	private void printMenu() {
		System.out.println("Main menu:\n" + "1: Show projects\n"
				+ "2: Create project\n" + "3: Create task\n"
				+ "4: Update task status\n" + "5: Advance time\n");
	}

	public void menu() {
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
