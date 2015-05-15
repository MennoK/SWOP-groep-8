package taskmanager;

/**
 * Executing tasks may require the use of certain resources.
 * 
 * @author Groep 8
 */
public class Resource implements Visitable {

	private String name;

	/**
	 * This is the constructor of the resource class. Every resource requires an
	 * given name.
	 * 
	 * @param name
	 *            : given name of the resource
	 */
	Resource(String name) {
		setName(name);
	}

	/**
	 * Sets the name of the resource
	 * 
	 * @param name
	 *            : given name of a resource
	 */
	private void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the resource
	 * 
	 * @return name : name of the resource
	 */
	public String getName() {
		return name;
	}

	/**
	 * to string method for debuging
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * accept visitor for visiting this
	 */
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
