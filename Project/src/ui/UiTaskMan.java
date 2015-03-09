package ui;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import TaskManager.Clock;
import TaskManager.Project;
import TaskManager.ProjectController;
import TaskManager.Task;

public class UiTaskMan {

	private ProjectController projectController;
	private Scanner scan;

	UiTaskMan() {
		scan = new Scanner(System.in);
		Clock clock = new Clock(LocalDateTime.of(2015, 2, 9, 8, 0));
		System.out.println("Current time initialized on:\n" + clock.getTime());
		projectController = new ProjectController(clock);
	}

	private void printProjects() {
		List<Project> projects = projectController.getAllProjects();
		for (int i = 0; i < projects.size(); i++)
			System.out.println(i + ": project '" + projects.get(i).getName()
					+ "' is " + projects.get(i).getStatus());
	}

	private Project selectProject() {
		List<Project> projects = projectController.getAllProjects();
		System.out.println("select a project:");
		int projectIndex = scan.nextInt();
		scan.nextLine();
		return projects.get(projectIndex);
	}

	private void printProject(Project project) {
		System.out.println("name: " + project.getName());
		System.out.println("description: " + project.getDescription());
		System.out.println("creation time: "
				+ project.getCreationTime().toString());
		System.out.println("due time: " + project.getDueTime().toString());
		System.out.println("status: " + project.getStatus());
		List<Task> tasks = project.getAllTasks();
		for (int i = 0; i < tasks.size(); i++)
			System.out.println(i + ": task '" + tasks.get(i).getDescription()
					+ "' is " + tasks.get(i).getStatus());
	}

	private Task selectTask(Project project) {
		List<Task> tasks = project.getAllTasks();
		System.out.println("select a task:");
		int projectIndex = scan.nextInt();
		scan.nextLine();
		return tasks.get(projectIndex);
	}

	private void printTask(Task task) {
		System.out.println("description:" + task.getDescription());
	}

	private void showProjects() {
		printProjects();
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
		projectController.addProject(new Project(name, description,
				projectController.getClock().getTime(), dueTime));
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

	private void parseFile() {
		System.out.println("TODO UC: parse file!");
	}

	private void printMenu() {
		System.out.println("Main menu:\n" + "1: Show projects\n"
				+ "2: Create project\n" + "3: Create task\n"
				+ "4: Update task status\n" + "5: Advance time\n"
				+ "6: Parse file");
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
			case "6":
				parseFile();
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
