package taskManager;

/**
 * Executing tasks may require the use of certain resources.
 * 
 * @author Groep 8
 */
public class Resource {

	private String name;
	
	/**
	 * This is the constructor of the resource class. Every
	 * resource requires an given name.
	 * 
	 * @param name : given name of the resource
	 */
	public Resource (String name){
		setName(name);
	}
	
	/**
	 * Sets the name of the resource
	 * 
	 * @param name : given name of a resource
	 */
	private void setName(String name){
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
}
