package TaskManager;

public class Time {

	
	public Time(int hours, int minutes,int seconds){
		setHours(hours);
		setMinutes(minutes);
		setSeconds(seconds);
	}
	
	public Time(int hours, int minutes,int seconds, int day, int month, int year){
		setHours(hours);
		setMinutes(minutes);
		setSeconds(seconds);
		setDay(day);
		setMonth(month);
		setYear(year);
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	int day = 0;
	int month = 0;
	int year = 0;
	int hours;
	int minutes;
	int seconds;
}
