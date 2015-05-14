package taskmanager;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

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
	Developer createDeveloper(String name) {
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
	@NonNull
	private void addDeveloper(Developer developer) {
		if (getAllDevelopers().contains(developer)) {
			throw new IllegalArgumentException(
					"The developer expert has already the given developer.");
		}
		this.developers.add(developer);
	}

	/**
	 * Returns the unmodifiable set of all developers
	 * 
	 * @return developers : set of all developers
	 */
	Set<Developer> getAllDevelopers() {
		return developers;
	}

	/**
	 * Saves the current state of the class
	 */
	void save() {
		this.memento = new Memento();
	}

	/**
	 * Loads the last save state of the class
	 * 
	 * @return last state of the class
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load();
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
		 * @param de
		 *            : developerExpert
		 */
		public Memento() {
			this.developers = new LinkedHashSet<Developer>(DeveloperExpert.this.developers);
		}

		/**
		 * Sets the developer set of the developer class to the saved set of the
		 * momento class
		 * 
		 * @param de
		 *            : developer expert
		 */
		public void load() {
			DeveloperExpert.this.developers = this.developers;
		}
	}

}
