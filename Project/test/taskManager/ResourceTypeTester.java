package taskManager;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ResourceTypeTester {

	private ResourceExpert resourceExpert;
	private ResourceType resourceType;
	private List<ResourceType> resourceTypeList;
	
	@Before
	public void setUp() {
		resourceExpert = new ResourceExpert();
		resourceExpert.createResourceType("type").build();
		
		resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);
	}
	
	@Test
	public void testGetName(){
		assertEquals("type", resourceType.getName());
	}
	
	@Test
	public void testCreateResource(){
		resourceType.createResource("resource");
		List<Resource> resourceList = new ArrayList<Resource>(resourceType.getAllResources());

		assertEquals(1, resourceType.getAllResources().size());
		assertEquals("resource", resourceList.get(0).getName());
	}
	
	@Test
	public void testCannotHaveNullResource(){
		assertFalse(resourceType.canHaveResource(null));
	}

	@Test
	public void testCannotHaveSameResource(){
		resourceType.createResource("resource");
		List<Resource> resourceList = new ArrayList<Resource>(resourceType.getAllResources());

		assertFalse(resourceType.canHaveResource(resourceList.get(0)));
	}

	@Test
	public void testAddRequiredResourceType(){
		
	}

	@Test
	public void testAddConflictedResourceType(){
		
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInRequiredList(){
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInConflictedList(){
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInRequiredList(){
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInConflictedList(){
		
	}
	
	
	
	
}
