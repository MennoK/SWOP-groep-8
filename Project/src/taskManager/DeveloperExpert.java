package taskManager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The DeveloperExpert class is the information expert and creator of
 * developers. This means it can create new developer objects and it has a set
 * of all existing developer objects
 * 
 * @author Groep 8
 */
public class DeveloperExpert {

	private Set<Developer> developers;
	private Memento memento;
	
	/**
	 * Default constructor of the developer expert. It initializes a new set of
	 * developers.
	 */
	DeveloperExpert() {
		this.developers = new LinkedHashSet<Developer>();
	}

	/**
	 * Creates a new developer with the given name. and adds the new developer
	 * to the set of all developers
	 * 
	 * @param name
	 *            : given name
	 */
	public Developer createDeveloper(String name) {
		Developer developer = new Developer(name);
		this.addDeveloper(developer);
		return developer;
	}

	/**
	 * Adds the developer to the set of developers if and only if the given
	 * developer is valid. This means the given developer is not null and not
	 * already in the set.
	 * 
	 * @param developer
	 *            : given developer
	 * 
	 * @throws IllegalArgumentException
	 *             : if the developer is a null object or the developer already
	 *             exists
	 */
	private void addDeveloper(Developer developer) {
		if (!canHaveDeveloper(developer)) {
			throw new IllegalArgumentException(
					"The developer expert has already the given developer.");
		}
		this.developers.add(developer);
	}

	/**
	 * Checks whether the given developer is valid
	 * 
	 * @param developer
	 *            : given developer
	 * @return true if and only if the developer is not already in the set and
	 *         not null
	 */
	boolean canHaveDeveloper(Developer developer) {
		return (!getAllDevelopers().contains(developer) && developer != null);
	}

	/**
	 * Returns the unmodifiable set of all developers
	 * 
	 * @return developers : set of all developers
	 */
	public Set<Developer> getAllDevelopers() {
		return Collections.unmodifiableSet(developers);
	}
	
	/**
	 * Saves the current state of the class
	 */
	void save() {
		this.memento = new Memento(this);
	}
	
	/**
	 * Loads the last save state of the class
	 * 
	 * @return last state of the class
	 */
	void load() {
		if(this.memento == null) {
			throw new IllegalStateException("You need to save before you can load");
		}
		else {
			this.memento.load(this);
		}
	}
	
	/**
	 * 
	 * Inner momento class of developer expert
	 * 
	 * @author groep 8
	 */
	private class Memento {
		private Set<Developer> developers;
		
		/**
		 * Constructor of the momento inner class of developer expert.
		 * Initialize a new set of developers of the current state
		 * 
		 * @param de : developerExpert
		 */
		public Memento(DeveloperExpert de) {
			this.developers = new LinkedHashSet<Developer>(de.developers);
		}
		
		/**
		 * Sets the developer set of the developer class
		 * to the saved set of the momento class
		 * 
		 * @param de : developer expert
		 */
		public void load(DeveloperExpert de) {
			de.developers = this.developers;
		}
	}
	
}
