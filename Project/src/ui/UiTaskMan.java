package ui;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import parser.Parser;
import taskManager.*;
import taskManager.Task.TaskBuilder;
import taskManager.exception.ConlictingPlanningException;
import ui.exception.ExitUseCaseException;
import utility.TimeSpan;

public class UiTaskMan {

	private TaskManController taskManController;
	private Reader reader;
	private Developer activeDeveloper;

	private UiTaskMan() {
		reader = new Reader();
		askInitialState();
		if (taskManController == null)
			initialiseEmptySystem();

		System.out.println("Current time initialized on:\n"
				+ taskManController.getTime() + "\n");
	}

	private void askInitialState() {
		while (true) {
			String fileName;
			try {
				fileName = reader
						.getString("Give a file for initialisation of the system:\n"
								+ "(press 2 to use ./input2.tman)");
			} catch (ExitUseCaseException e1) {
				return;
			}
			if (fileName.equals("2"))
				fileName = "./input2.tman";
			try {
				Parser parser = new Parser();
				taskManController = parser.parse(fileName);
				System.out.println("Starting with system initialised from "
						+ fileName);
				return;
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void initialiseEmptySystem() {
		System.out.println("Starting with an empty system");
		LocalDateTime now = LocalDateTime.of(2014, 1, 1, 8, 0);
		try {
			now = reader.getDate("Give the current time:");
		} catch (ExitUseCaseException e) {
			System.out.println("The standard time will be used.");
		}
		taskManController = new TaskManController(now);
	}

	private void showProjects() throws ExitUseCaseException {
		Project project = reader.select(taskManController.getProjectExpert()
				.getAllProjects());
		System.out.println(new ToStringVisitor().create(project));
		Task task = reader.select(project.getAllTasks(), false);
		System.out.println(new ToStringVisitor().create(task));
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
		taskManController.getProjectExpert().createProject(name, description,
				dueTime);
	}

	private void createTask() throws ExitUseCaseException {
		while (true) {
			System.out.println("Creating a task\n"
					+ "Please fill in the following form:\n"
					+ "Adding task to which project?");
			Project project = reader.select(taskManController
					.getProjectExpert().getAllProjects());
			TaskBuilder builder = Task.builder(reader
					.getString("Give a description:"), reader
					.getDuration("Give an estimate for the task duration:"),
					reader.getDouble("Give an acceptable deviation:"));
			while (reader
					.getBoolean("Is this task dependent on an other task?")) {
				builder.addDependencies(reader.select(project.getAllTasks()));
			}
			if (reader.getBoolean("Is this an alternative to a failled task?")) {
				builder.setOriginalTask(reader.select(project.getAllTasks()));
			}
			builder.build(project);
			return;
		}
	}

	private void planTask() throws ExitUseCaseException {
		Task task = reader.select(taskManController.getUnplannedTasks());
		plan(task);
	}

	private void plan(Task task) throws ExitUseCaseException {
		System.out.println(Printer.listDates(new ArrayList<LocalDateTime>(
				taskManController.getPossibleStartTimes(task))));
		TimeSpan timeSpan;
		if (reader
				.getBoolean("Do you want to start the planning on one of those times?")) {
			timeSpan = new TimeSpan(reader.selectDate(taskManController
					.getPossibleStartTimes(task)), task.getDuration());
		} else {
			timeSpan = new TimeSpan(
					reader.getDate("When do you want to start the planning of this Task?"),
					task.getDuration());
		}
		Planning.PlanningBuilder plan;
		try {
			plan = Planning.builder(timeSpan.getBegin(), task, reader
					.select(taskManController.getDeveloperExpert()
							.getAllDevelopers()), taskManController
					.getPlanner());
			while (reader
					.getBoolean("Do you want to assign an extra Developer?")) {
				plan.addDeveloper(reader.select(taskManController
						.getDeveloperExpert().getAllDevelopers()));
			}
			if (task.requiresRessources()) {
				Set<Resource> ressources = taskManController.selectResources(
						task, timeSpan);
				System.out
						.println("The system proposes the following ressources:");
				Printer.list(ressources);
				plan.addAllResources(ressources);
			}
			plan.build();
		} catch (ConlictingPlanningException conflict) {
			System.out.println("A conflict occured with the following Tasks:");
			for (Planning planning : conflict.getConflictingPlannings()) {
				System.out.println(new ToStringVisitor()
						.create(taskManController.getTask(planning)));
			}
			if (!reader
					.getBoolean("Do you want to restart planning the original Task?")) {
				for (Planning planning : conflict.getConflictingPlannings()) {
					plan(taskManController.getTask(planning));
				}
			}
			plan(task);
		}
	}

	private void updateTaskStatus() throws ExitUseCaseException {
		System.out.println("Updating the status of a task\n"
				+ "Please select a task:");
		Task task = reader.select(taskManController.getProjectExpert()
				.getAllTasks(activeDeveloper));

		while (true) {
			try {
				if (task.getStatus() == TaskStatus.AVAILABLE) {
					taskManController.setExecuting(task,
							reader.getDate("When did you start this task?"));
				} else if (task.getStatus() == TaskStatus.EXECUTING) {
					if (reader
							.getBoolean("Was this task finished succesfully?")) {
						taskManController.setFinished(task, reader
								.getDate("When did you finish this task?"));
					} else {
						taskManController.setFailed(task,
								reader.getDate("When did you fail this task?"));
					}
				}
				return;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void advanceTime() throws ExitUseCaseException {
		while (true) {
			try {
				taskManController.advanceTime(reader
						.getDate("Enter the new timestamp:"));
				return;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void printSimMenu() {
		System.out.println("\nSimulation menu:\n" + "1: Show projects\t"
				+ "2: Create task\t" + "3: Plan task\n" + "9: Stop simulation");
	}

	private void runSimulation() {
		taskManController.saveSystem();
		while (true) {
			try {
				printSimMenu();
				String choice = reader.getString("select option:");
				switch (choice) {
				case "1":
					showProjects();
					break;
				case "2":
					createTask();
					break;
				case "3":
					planTask();
					break;
				case "9":
					if (!reader
							.getBoolean("Do you want to keep the simulation results?")) {
						taskManController.loadSystem();
					}
					return;
				default:
					System.out
							.println("Invalid choice, try again. (9 to exit)");
					break;
				}
			} catch (ExitUseCaseException e) {
				System.out
						.println("Use case exited, returning to simulation menu.");
			}
		}
	}

	private void selectDeveloper() throws ExitUseCaseException {
		activeDeveloper = reader.select(taskManController.getDeveloperExpert()
				.getAllDevelopers());
	}

	private void printSwitchUserMenu() {
		System.out.println("\nSwitch user menu:\n" + "1: Projectmanager\t"
				+ "2: Developer\t" + "9: Exit");
	}

	private void printProjectManagerMenu() {
		System.out.println("\nProject Manager Main menu:\n"
				+ "1: Show projects\t" + "2: Create project\t"
				+ "3: Create task\n" + "4: Plan task\t\t" + "5: Advance time\n"
				+ "6: Run simulation\t" + "9: Return to user menu");
	}

	private void printDeveloperMenu() {
		System.out.println("\nDeveloper Main menu:\n" + "1: Show projects\t"
				+ "2: Update task status\t" + "3: Advance time\t"
				+ "9: Return to user menu");
	}

	private void switchUserMenu() {
		while (true) {
			try {
				printSwitchUserMenu();
				String choice = reader.getString("select user");
				switch (choice) {
				case "1":
					projectManagerMenu();
					break;
				case "2":
					selectDeveloper();
					developerMenu();
					break;
				case "9":
					reader.close();
					return;
				default:
					System.out
							.println("Invalid choice, try again. (9 to exit)");
					break;
				}
			} catch (ExitUseCaseException e) {
				System.out
						.println("Use case exited, returning to the main menu.");
			}
		}
	}

	private void projectManagerMenu() {
		try {
			printProjectManagerMenu();
			String choice = reader.getString("Select an option");
			switch (choice) {
			case "9":
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
				advanceTime();
				break;
			case "6":
				runSimulation();
				break;
			default:
				System.out.println("Invalid choice, try again.");
				break;
			}
		} catch (ExitUseCaseException e) {
			System.out.println("Use case exited, returning to the main menu.");
		}
	}

	private void developerMenu() {
		try {
			printDeveloperMenu();
			String choice = reader.getString("Select an option");
			switch (choice) {
			case "9":
				return;
			case "1":
				showProjects();
				break;
			case "2":
				updateTaskStatus();
				break;
			case "3":
				advanceTime();
				break;
			default:
				System.out.println("Invalid choice, try again.");
				break;
			}
		} catch (ExitUseCaseException e) {
			System.out.println("Use case exited, returning to the main menu.");
		}
	}

	public static void main(String[] args) {
		System.out.println("Welcome to TaskMan");
		new UiTaskMan().switchUserMenu();
		System.out.println("Goodbye!");
	}

}
