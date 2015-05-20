package taskmanager.exception;

import taskmanager.ResourceType;

public class UnplannableResourceTypeException extends RuntimeException{
	
	private static final long serialVersionUID = -2971588873060009105L;
	
	private ResourceType type;
	
	public UnplannableResourceTypeException(ResourceType type) {
		this.type = type;
	}
	
	public ResourceType getResourceType(){
		return type;
	}

}
