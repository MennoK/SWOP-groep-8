package taskManager;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import taskManager.ResourceType.ResourceTypeBuilder;

/**
 * The ResourceExpert class is the information expert and creator of resource types.
 * This means it can create a resource type builder to make new resource type
 * object and it has a set of all existing resource type objects
 *  
 * @author groep 8
 */
public class ResourceExpert {

	private Set<ResourceType> resourcetypes;
	
	/**
	 * Default constructor of the resource expert. It initializes
	 * a new set of resource types
	 */
	public ResourceExpert(){
		this.resourcetypes = new LinkedHashSet<ResourceType>();
	}
	
	/**
	 * Returns a new resource type builder to add extra parameters such as
	 * other required resource types and other conflicted resource types
	 * 
	 * @param name : required name of a resource type
	 * @return resourceTypeBuilder : new builder for creating resource types
	 */
	public ResourceTypeBuilder createResourceType(String name){
		return new ResourceTypeBuilder(name,this);
	}
		
	/**
	 * Adds the resource type to the set of resource types if and only if
	 * the given resource type is valid. This means the given resource type
	 * is not null and not already in the set.
	 * 
	 * @param resource type : given resource type
	 */
	public void addResourceType(ResourceType resourcetype){
		if (!canHaveResource(resourcetype)){
			throw new IllegalArgumentException("The resource expert has already the given resource type.");
		}
		this.resourcetypes.add(resourcetype); 
	}
	
	/**
	 * Checks whether the given resource type is valid
	 * 
	 * @param resource type : given resource type
	 * @return true if and only if the resource type is not already in the set and not null
	 */
	boolean canHaveResource(ResourceType resourcetype){
		return (!getAllResourceTypes().contains(resourcetype) && resourcetype != null);
	}
	
	/**
	 * Returns the set of all resource types
	 * 
	 * @return resourcetypes : set of all resource types
	 */
	public Set<ResourceType> getAllResourceTypes(){
		return resourcetypes;
	}
}
