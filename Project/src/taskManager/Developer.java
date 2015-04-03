package taskManager;

import java.time.LocalDateTime;


public class Developer {

	private String name;

	public Developer(String name, LocalDateTime dailyAvailability){
		setName(name);
	}

	private void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}
}
