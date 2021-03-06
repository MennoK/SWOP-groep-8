package ui;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import taskmanager.Visitable;
import ui.exception.ExitUseCaseException;

public class Reader {
	private Scanner scan;

	Reader() {
		setScan(new Scanner(System.in));
	}

	private void setScan(Scanner scan) {
		this.scan = scan;
	}

	private Scanner getScan() {
		return scan;
	}

	private String getData() throws ExitUseCaseException {
		System.out.println("Type enter to skip/return to main menu.");
		String str = getScan().nextLine();
		if (str.equals(""))
			throw new ExitUseCaseException("Exit signal caught.");
		return str;
	}

	void close() {
		getScan().close();
	}

	<T extends Visitable> T select(Collection<T> options)
			throws ExitUseCaseException {
		return select(options, true);
	}

	<T extends Visitable> T select(Collection<T> options, boolean printList)
			throws ExitUseCaseException {
		List<T> listedOptions = new ArrayList<T>(options);
		while (true) {
			if (printList) {
				System.out.println(Printer.list(listedOptions));
			}
			System.out.println("select one:");
			try {
				return listedOptions.get(Integer.parseInt(getData()) - 1);
			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer");
			}
		}
	}

	LocalDateTime selectDate(Collection<LocalDateTime> options)
			throws ExitUseCaseException {
		List<LocalDateTime> listedOptions = new ArrayList<LocalDateTime>(
				options);
		while (true) {
			System.out.println(Printer.listDates(listedOptions));
			System.out.println("select one:");
			try {
				return listedOptions.get(Integer.parseInt(getData()) - 1);
			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer");
			}
		}
	}

	String getString(String querry) throws ExitUseCaseException {
		System.out.println(querry + ":");
		return getData();
	}

	boolean getBoolean(String querry) throws ExitUseCaseException {
		while (true) {
			System.out.println(querry + " (y/n)");
			switch (getData()) {
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

	int getInt(String querry) throws ExitUseCaseException {
		while (true) {
			System.out.println(querry + " (int)");
			try {
				return Integer.parseInt(getData());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an int");
			}
		}
	}

	double getDouble(String querry) throws ExitUseCaseException {
		while (true) {
			System.out.println(querry + " (double)");
			try {
				return Double.parseDouble(getData());
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give a double");
			}
		}
	}

	Duration getDuration(String querry) throws ExitUseCaseException {
		while (true) {
			System.out.println(querry + " (hours)");
			try {
				return Duration.ofHours(Integer.parseInt(getData()));
			} catch (java.lang.NumberFormatException e) {
				System.out.println("Give an integer");
			}
		}
	}

	LocalDateTime getDate(String querry) throws ExitUseCaseException {
		while (true) {
			System.out.println(querry + "\n(format: 'yyyy-mm-ddThh:mm:ss')\n"
					+ "(type N for 2015-02-09T08:00:00 + N hours)");
			String answer = getData();
			try {
				return LocalDateTime.of(2015, 2, 9, 8, 0).plusHours(
						Integer.parseInt(answer));
			} catch (NumberFormatException e1) {
				try {
					return LocalDateTime.parse(answer);
				} catch (java.time.format.DateTimeParseException e2) {
					System.out
							.println("The given date was invalid, try again.");
				}
			}
		}
	}
}