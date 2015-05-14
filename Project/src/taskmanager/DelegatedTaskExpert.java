package taskmanager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DelegatedTaskExpert {

	private Set<Task> delegatedTasks;
    private Memento memento;
	
	/**
	 * Default constructor of the delegatedTaskExpert. It initializes a 
	 * new empty set for delegated tasks
	 */
	public DelegatedTaskExpert(){
		this.delegatedTasks = new LinkedHashSet<Task>();
	}
	
	/**
	 * Adds the given task as a delegated task to the set 
	 * 
	 * @param task : given task
	 */
	public void addDelegatedTask(Task task){
		this.addDelegatedTask(task);
	}
	/**
	 * Returns the delegated task set
	 * 
	 * @return delegatedTasks : set with delegated Tasks
	 */
	public Set<Task> getAllDelegatedTasks(){
		return Collections.unmodifiableSet(delegatedTasks);
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
		private Set<Task> delegatedTasks;

		/**
		 * Constructor of the momento inner class of developer expert.
		 * Initialize a new set of developers of the current state
		 * 
		 * @param de
		 *            : developerExpert
		 */
		public Memento() {
            this.delegatedTasks = new LinkedHashSet<Task>(DelegatedTaskExpert.this.delegatedTasks);
		}

		/**
		 * Sets the developer set of the developer class to the saved set of the
		 * momento class
		 * 
		 * @param de
		 *            : developer expert
		 */
		public void load() {
			DelegatedTaskExpert.this.delegatedTasks = this.delegatedTasks;
		}
	}
	
}
