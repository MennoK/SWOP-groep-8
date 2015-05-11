package taskmanager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DelegatedTaskExpert {

	private Set<Task> delegatedTasks;
	
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
	
}
