package ui;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.bcel.internal.generic.NEW;

import parser.Parser;
import taskmanager.*;
import taskmanager.Task.TaskBuilder;
import taskmanager.exception.ConlictingPlanningException;
import ui.exception.ExitUseCaseException;
import utility.TimeSpan;

public class UiTaskMan {

	private static final String parserFileName = "input3.tman";

	private TaskManController tmc;
	private Reader reader;
	private Developer activeDeveloper;

	private UiTaskMan() {
		reader = new Reader();
		askInitialState();
		if (tmc == null)
			initialiseEmptySystem();

		System.out.println("Current time initialized on:\n" + tmc.getTime()
				+ "\n");
	}

	private void askInitialState() {
		while (true) {
			String fileName;
			try {
				fileName = reader
						.getString("Give a file for initialisation of the system:\n"
								+ "(press 1 to use ./" + parserFileName + ")");
			} catch (ExitUseCaseException e1) {
				return;
			}
			if (fileName.equals("1"))
				fileName = "./" + parserFileName;
			try {
				Parser parser = new Parser();
				tmc = parser.parse(fileName);
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
		tmc = new TaskManController(now);
	}

	private void showProjects() throws ExitUseCaseException {
		Project project = reader.select(tmc.getAllProjectsAllOffices());
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
		tmc.createProject(name, description, dueTime);
	}

	private void createTask() throws ExitUseCaseException {
		System.out.println("Creating a task\n"
				+ "Please fill in the following form:\n"
				+ "Adding task to which project?");
		Project project = reader.select(tmc.getAllProjectsActiveOffice());
		TaskBuilder builder = Task.builder(
				reader.getString("Give a description:"),
				reader.getDuration("Give an estimate for the task duration:"),
				reader.getDouble("Give an acceptable deviation:"));
		while (reader.getBoolean("Does this task require more ressources?")) {
			builder.addRequiredResourceType(
					reader.select(tmc.getAllResourceTypes()),
					reader.getInt("How many of those do you need?"));
		}
		while (reader.getBoolean("Is this task dependent on an other task?")) {
			builder.addDependencies(reader.select(project.getAllTasks()));
		}
		if (reader.getBoolean("Is this an alternative to a failled task?")) {
			builder.setOriginalTask(reader.select(project.getAllTasks()));
		}
		builder.amountOfRequiredDevelopers(reader
				.getInt("How many developers are required to work on this task?"));
		try {
			builder.build(project);
		} catch (IllegalStateException e) {
			System.out
					.println("This task was Illegal. Did you check the ressource requirements?");
			createTask();
		}
	}

	private void planTask() throws ExitUseCaseException {
		Task task = reader.select(tmc.getAllDelegatablePlannableTasks());
		plan(task);
	}

	private void plan(Task task) throws ExitUseCaseException {
		System.out.println("Planning task:"
				+ new ToStringVisitor().create(task));
		TimeSpan timeSpan = planSelectTimeSpan(task);
		try {
			Planning.PlanningBuilder plan = tmc.getPlanner().createPlanning(
					timeSpan.getBegin(), task,
					reader.select(tmc.getAllDevelopers()));
			while (reader
					.getBoolean("Do you want to assign an extra Developer?")) {
				plan.addDeveloper(reader.select(tmc.getAllDevelopers()));
			}
			if (task.requiresRessources()) {
				plan.addAllResources(planSelectResources(task, timeSpan));
			}
			plan.build();
		} catch (ConlictingPlanningException conflict) {
			resolveConflict(conflict, task);
		}
	}

	private Set<Resource> planSelectResources(Task task, TimeSpan timeSpan)
			throws ExitUseCaseException {
		System.out.println("The system proposes the following ressources:");
		System.out.println(Printer.list(tmc.selectResources(task, timeSpan)));
		if (reader.getBoolean("Do you accept the systems proposal?")) {
			return tmc.selectResources(task, timeSpan);
		} else {
			Set<Resource> selected = new HashSet<Resource>();
			Map<ResourceType, Integer> requirements = task
					.getRequiredResourceTypes();
			for (ResourceType type : requirements.keySet()) {
				System.out.println("The task requires "
						+ requirements.get(type) + " ressources of type "
						+ type.getName());
				for (int i = 0; i < requirements.get(type); i++) {
					selected.add(reader.select(tmc.getPlanner()
							.resourcesOfTypeAvailableFor(type, task, timeSpan)));
				}
			}
			return selected;
		}
	}

	private TimeSpan planSelectTimeSpan(Task task) throws ExitUseCaseException {
		System.out.println("Possible starting times:");
		System.out.println(Printer.listDates(new ArrayList<LocalDateTime>(tmc
				.getPossibleStartTimes(task))));
		if (reader
				.getBoolean("Do you want to start the planning on one of those times?")) {
			return new TimeSpan(reader.selectDate(tmc
					.getPossibleStartTimes(task)), task.getDuration());
		} else {
			return new TimeSpan(
					reader.getDate("When do you want to start the planning of this Task?"),
					task.getDuration());
		}
	}

	private void resolveConflict(ConlictingPlanningException conflict, Task task)
			throws ExitUseCaseException {
		System.out.println("A conflict occured with the following Tasks:");
		for (Task conflictingTask : conflict.getConflictingTasks()) {
			System.out.println(new ToStringVisitor().create(conflictingTask));
		}
		if (!reader
				.getBoolean("y => re-start planning the new task / n => re-plan the conflicting task")) {
			for (Task conflictingTask : conflict.getConflictingTasks()) {
				plan(conflictingTask);
			}
		}
		plan(task);
	}

	private void updateTaskStatus() throws ExitUseCaseException {
		System.out.println("Updating the status of a task\n"
				+ "Please select a task:");
		Task task = reader.select(tmc.getAllTasks(activeDeveloper));

		while (true) {
			try {
				if (task.getStatus() == TaskStatus.AVAILABLE) {
					tmc.setExecuting(task,
							reader.getDate("When did you start this task?"));
				} else if (task.getStatus() == TaskStatus.EXECUTING) {
					if (reader
							.getBoolean("Was this task finished succesfully?")) {
						tmc.setFinished(task, reader
								.getDate("When did you finish this task?"));
					} else {
						tmc.setFailed(task,
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
				tmc.advanceTime(reader.getDate("Enter the new timestamp:"));
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
		tmc.saveSystem();
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
						tmc.loadSystem();
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
			tmc.logIn(reader.select(tmc.getAllOffices()));
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
		tmc.logOut();
	}

	private void developerMenu() {
		try {
			tmc.logIn(reader.select(tmc.getAllOffices()));
			tmc.logIn(reader.select(tmc.getAllDevelopers()));
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
		tmc.logOut();
	}

	public static void main(String[] args) {
		System.out.println("Welcome to TaskMan");
		new UiTaskMan().switchUserMenu();
		System.out.println("Goodbye!");
	}

}
