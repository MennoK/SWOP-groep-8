package ui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import TaskManager.Clock;
import TaskManager.Project;
import TaskManager.ProjectController;

public class UiTaskMan {

	private ProjectController projectController;

	UiTaskMan() {
		projectController = new ProjectController(new Clock(LocalDateTime.of(
				2015, 03, 07, 01, 00)));
	}

	private void showProjects() {
		System.out.println("TODO UC: show projects!");
		List<Project> projects = projectController.getAllProjects();
		for (int i = 0; i < projects.size(); i++)
			System.out.println(i + ": project '" + projects.get(i).getName()
					+ "' is " + projects.get(i).getStatus());
	}

	private void createProject() {
		System.out.println("TODO UC: create project!");
	}

	private void createTask() {
		System.out.println("TODO UC: create task!");
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
		System.out.println("1: Show projects\n" + "2: Create project\n"
				+ "3: Create task\n" + "4: Update task status\n"
				+ "5: Advance time\n" + "6: Parse file");
	}

	public void menu() {
		Scanner scan = new Scanner(System.in);
		while (true) {
			printMenu();
			String choice = scan.next();
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
