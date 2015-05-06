package taskmanager;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The ResourceExpert class is the information expert and creator of resource
 * types. This means it can create a resource type builder to make new resource
 * type object and it has a set of all existing resource type objects
 * 
 * @author groep 8
 */
public class ResourceExpert {

	private Set<ResourceType> resourcetypes;
	private Memento memento;

	/**
	 * Default constructor of the resource expert. It initializes a new set of
	 * resource types
	 */
	ResourceExpert() {
		this.resourcetypes = new LinkedHashSet<ResourceType>();
	}

	/**
	 * Adds the resource type to the set of resource types if and only if the
	 * given resource type is valid. This means the given resource type is not
	 * null and not already in the set.
	 * 
	 * @param resource
	 *            type : given resource type
	 */
	void addResourceType(ResourceType resourcetype) {
		if (!canHaveResource(resourcetype)) {
			throw new IllegalArgumentException(
					"The resource expert has already the given resource type.");
		}
		this.resourcetypes.add(resourcetype);
	}

	/**
	 * Checks whether the given resource type is valid
	 * 
	 * @param resource
	 *            type : given resource type
	 * @return true if and only if the resource type is not already in the set
	 *         and not null
	 */
	boolean canHaveResource(ResourceType resourcetype) {
		return (!getAllResourceTypes().contains(resourcetype) && resourcetype != null);
	}

	/**
	 * Returns the set of all resource types
	 * 
	 * @return resourcetypes : set of all resource types
	 */
	Set<ResourceType> getAllResourceTypes() {
		return resourcetypes;
	}

	/**
	 * Saves the current state of the class
	 */
	void save() {
		this.memento = new Memento(this);
		for (ResourceType rt : this.resourcetypes) {
			rt.save();
		}
	}

	/**
	 * Loads the last save state of the class
	 */
	void load() {
		if (this.memento == null) {
			throw new IllegalStateException(
					"You need to save before you can load");
		} else {
			this.memento.load(this);
			for (ResourceType rt : this.resourcetypes) {
				rt.load();
			}
		}
	}

	/**
	 * 
	 * Inner momento class of resource expert
	 * 
	 * @author groep 8
	 */
	private class Memento {
		private Set<ResourceType> resourcetypes;

		/**
		 * Constructor of the momento inner class of resource expert. Initialize
		 * a new set of resourcetype of the current state
		 * 
		 * @param re
		 *            : resourcetype expert
		 */
		public Memento(ResourceExpert re) {
			this.resourcetypes = new LinkedHashSet<ResourceType>(
					re.resourcetypes);
		}

		/**
		 * Sets the resource type set of the resourceExpert class to the saved
		 * set of the momento class
		 * 
		 * @param re
		 *            : resourcetype expert
		 */
		public void load(ResourceExpert re) {
			re.resourcetypes = this.resourcetypes;
		}
	}
}
