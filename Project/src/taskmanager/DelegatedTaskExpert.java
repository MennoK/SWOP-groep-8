package taskmanager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;

/**
 * 
 * The delegated task expert contains all the information of the task that are
 * delegated to the branch office
 * 
 * @author Group 8
 *
 */
public class DelegatedTaskExpert {

	private ArrayListMultimap<BranchOffice, Task> delegatedTasks;
	private HashMap<BranchOffice, Memento> mementos;

	/**
	 * Default constructor of the delegatedTaskExpert. It initializes a new
	 * empty set for delegated tasks
	 */
	DelegatedTaskExpert() {
		this.delegatedTasks = ArrayListMultimap.create();
		this.mementos = new HashMap<BranchOffice, Memento>();
	}

	/**
	 * Adds the given task as a delegated task to the set
	 * 
	 * @param task
	 *            : given task
	 */
	void addDelegatedTask(Task task, BranchOffice office) {
		this.delegatedTasks.put(office, task);
	}

	void removeDelegatedTask(Task task) {
		delegatedTasks.remove(getOriginalOffice(task), task);
	}

	/**
	 * returns the original branch office of a task
	 * 
	 * @param delegatedTask
	 *            : the task you want the original office of
	 * @return : the original office of the task
	 */
	public BranchOffice getOriginalOffice(Task delegatedTask) {
		BranchOffice returnOffice = null;
		for (BranchOffice office : delegatedTasks.keySet()) {
			if (delegatedTasks.containsEntry(office, delegatedTask)) {
				returnOffice = office;
			}
		}
		if (returnOffice != null) {
			return returnOffice;
		} else {
			throw new IllegalArgumentException(
					"the task is not delegated to this office");
		}

	}

	/**
	 * Returns the delegated task set
	 * 
	 * @return delegatedTasks : set with delegated Tasks
	 */
	public Set<Task> getAllDelegatedTasks() {
		if (!delegatedTasks.isEmpty()) {
			return Collections.unmodifiableSet(new HashSet<Task>(delegatedTasks
					.values()));
		} else {
			return new HashSet<Task>();
		}
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

			this.delegatedTasks = DelegatedTaskExpert.this.delegatedTasks
					.get(office);

		}

		/**
		 * loads the memento to restore the state *
		 */
		public void load() {
			DelegatedTaskExpert.this.delegatedTasks.replaceValues(office,
					this.delegatedTasks);
		}
	}

}
