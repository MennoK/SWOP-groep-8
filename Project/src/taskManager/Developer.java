package taskManager;

/**
 * A developer is responsible for the day to day tasks within the company, 
 * which can include development, deployment, testing, etc. Developers
 * work 8-hours days, from 08:00 to 17:00 with a one hour break between
 * 11:00 and 14:00
 * 
 * @author Groep 8
 */
public class Developer implements Visitable {

	private String name;

	/**
	 * The constructor of  a developer. Every developer requires
	 * a name.
	 * 
	 * @param name : name of a developer
	 */
	Developer(String name){
		setName(name);
	}

	/**
	 * Sets the name of a developer
	 * 
	 * @param name : given name
	 */
	private void setName(String name){
		this.name = name;
	}

	/**
	 * Returns the given name of a developer
	 * 
	 * @return name : the name of a developer
	 */
	public String getName(){
		return name;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
