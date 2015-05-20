package taskmanager.exception;

import taskmanager.ResourceType;

public class UnplannableResourceAmountException extends RuntimeException{

	private static final long serialVersionUID = 1318508049723435085L;
	
	private int amountOfResources;
	private ResourceType resourceType;
	
	public UnplannableResourceAmountException(int amountOfResources, ResourceType resourcetype){
		this.amountOfResources = amountOfResources;
		this.resourceType = resourcetype;
	}
	
	public int getAmountOfResources() {
		return amountOfResources;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
}
