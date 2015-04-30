package taskManager;

public interface Visitable {

	/**
	 * accept visitor for visiting the Visitable
	 */
	public void accept(Visitor visitor);
}
