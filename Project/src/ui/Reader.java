package ui;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Reader {
	private Scanner scan;

	Reader(Scanner scan) {
		this.setScan(scan);
	}

	String getString(String querry) {
		System.out.println(querry + ":");
		return scan.nextLine();
	}

	boolean getBoolean(String querry) {
		while (true) {
			System.out.println(querry + " (y/n)");
			switch (scan.nextLine()) {
			case "Y":
			case "y":
			case "yes":
			case "Yes":
				return true;
			case "n":
			case "N":
			case "no":
			case "No":
				return false;
			default:
				System.out.println("Invalid answer, try again.");
				break;
			}
		}
	}

	double getDouble(String querry) {
		while (true) {
			System.out.println(querry + " (double)");
			try {
				return Double.parseDouble(scan.nextLine());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give a double");
			}
		}
	}

	Duration getDuration(String querry) {
		while (true) {
			System.out.println(querry + " (hours)");
			try {
				return Duration.ofHours(Integer.parseInt(scan.nextLine()));
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer");
			}
		}
	}

	LocalDateTime getDate(String querry) {
		while (true) {
			System.out.println(querry + " (format: 'yyyy-mm-ddThh:mm:ss')\n"
					+ "(type 0 for 09/02/2015, 08:00)");
			try {
				String answer = scan.nextLine();
				if (answer.equals("0"))
					return LocalDateTime.of(2015, 2, 9, 8, 0);
				return LocalDateTime.parse(answer);
			} catch (java.time.format.DateTimeParseException e) {
				System.out.println("The given date was invalid, try again.");
			}
		}
	}

	public Scanner getScan() {
		return scan;
	}

	public void setScan(Scanner scan) {
		this.scan = scan;
	}

}
