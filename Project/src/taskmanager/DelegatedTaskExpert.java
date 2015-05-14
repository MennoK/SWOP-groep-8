package taskmanager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;

/**
 * 
 * The delegated task expert contains all the information of the task that 
 * are delegated to the branch office
 * 
 * @author Group 8
 *
 */
public class DelegatedTaskExpert {

	private ArrayListMultimap<BranchOffice, Task> delegatedTasks;
    private HashMap<BranchOffice, Memento> mementos;
	
	/**
	 * Default constructor of the delegatedTaskExpert. It initializes a 
	 * new empty set for delegated tasks
	 */
	public DelegatedTaskExpert(){
		this.delegatedTasks = ArrayListMultimap.create();
	}
	
	/**
	 * Adds the given task as a delegated task to the set 
	 * 
	 * @param task : given task
	 */
	public void addDelegatedTask(Task task, BranchOffice office){
		this.delegatedTasks.put(office, task);
	}
	/**
	 * Returns the delegated task set
	 * 
	 * @return delegatedTasks : set with delegated Tasks
	 */
	public Set<Task> getAllDelegatedTasks(){
		return Collections.unmodifiableSet(new HashSet<Task>(delegatedTasks.values()));
	}
	/**
	 * Saves the current state of the class
	 */
	void save(BranchOffice office) {
		this.mementos.put(office, new Memento(office));
	}

	/**
	 * Loads the last save state of the class
	 * 
	 * @return last state of the class
	 */
	void load(BranchOffice office) {
		if (this.mementos.get(office) == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.mementos.get(office).load();
		}
	}

	/**
	 * 
	 * Inner memento class of delegated task expert
	 * 
	 * @author groep 8
	 */
	private class Memento {
		BranchOffice office;
		private List<Task> delegatedTasks;

		/**
		 * Constructor of the memento inner class of delegated task expert.
		 * Initialize a new list the delegated tasks in the current state
		 * 
		 */
		public Memento(BranchOffice office) {
			this.office = office;
			
			this.delegatedTasks = DelegatedTaskExpert.this.delegatedTasks.get(office);

		}

		/**
		 * loads the memento to restore the state		 * 
		 */
		public void load() {
			DelegatedTaskExpert.this.delegatedTasks.removeAll(office);
			DelegatedTaskExpert.this.delegatedTasks.putAll(office, delegatedTasks);
		}
	}
	
}
