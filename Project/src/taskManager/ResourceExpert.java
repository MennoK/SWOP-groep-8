package taskManager;

import java.util.Collections;
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
	ResourceExpert(){
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
	public void addResourceType(ResourceType resourcetype) {
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
	public Set<ResourceType> getAllResourceTypes(){
		return Collections.unmodifiableSet(resourcetypes);
	}

	void save() {
		this.memento = new Memento(this);
		for(ResourceType rt : this.resourcetypes) {
			rt.save();
		}
	}

	boolean load() {
		if (this.memento == null) {
			return false;
		} else {
			this.memento.load(this);
			for(ResourceType rt : this.resourcetypes) {
				rt.load();
			}
			return true;
		}
	}

	private class Memento {
		private Set<ResourceType> resourcetypes;

		public Memento(ResourceExpert re) {
			this.resourcetypes = new LinkedHashSet<ResourceType>(
					re.resourcetypes);
		}

		public void load(ResourceExpert re) {
			re.resourcetypes = this.resourcetypes;
		}
	}
}
