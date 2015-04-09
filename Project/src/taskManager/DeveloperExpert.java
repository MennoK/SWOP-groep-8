package taskManager;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The DeveloperExpert class is the information expert and creator of developers.
 * This means it can create new developer objects and it has a set of 
 * all existing developer objects
 * 
 * @author Groep 8
 */
public class DeveloperExpert {

	private Set<Developer> developers;
	
	/**
	 * Default constructor of the developer expert. It initializes
	 * a new set of developers.
	 */
	DeveloperExpert(){
		this.developers = new LinkedHashSet<Developer>();
	}
	
	/**
	 * Creates a new developer with the given name. and adds
	 * the new developer to the set of all developers
	 * 
	 * @param name : given name
	 */
	public void createDeveloper(String name){
		Developer developer = new Developer(name);
		this.addDeveloper(developer);
	}
	
	/**
	 * Adds the developer to the set of developers if and only if
	 * the given developer is valid. This means the given developer
	 * is not null and not already in the set.
	 * 
	 * @param developer : given developer
	 * 
	 * @throws IllegalArgumentException : if the developer is a null object or the developer already exists
	 */
	private void addDeveloper(Developer developer){
		if (!canHaveDeveloper(developer)){
			throw new IllegalArgumentException("The developer expert has already the given developer.");
		}
		this.developers.add(developer); 
	}
	
	/**
	 * Checks whether the given developer is valid
	 * 
	 * @param developer : given developer
	 * @return true if and only if the developer is not already in the set and not null
	 */
	boolean canHaveDeveloper(Developer developer){
		return (!getAllDevelopers().contains(developer) && developer != null);
	}
	
	/**
	 * Returns the set of all developers
	 * 
	 * @return developers : set of all developers
	 */
	public Set<Developer> getAllDevelopers(){
		return developers;
	}
	
}
