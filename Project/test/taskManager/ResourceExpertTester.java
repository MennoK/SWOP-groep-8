package taskManager;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ResourceExpertTester {

	private ResourceExpert resourceExpert;

	@Before
	public void setUp() {
		resourceExpert = new ResourceExpert();
	}

	@Test
	public void testResourceTypeSetIsInitialized(){
		assertEquals(0, resourceExpert.getAllResourceTypes().size());
	}

	@Test
	public void testCreateSimpleResourceType(){
		resourceExpert.createResourceType("simple").build();
		assertEquals(1, resourceExpert.getAllResourceTypes().size());
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>();
		resourceTypeList.addAll(resourceExpert.getAllResourceTypes());
		assertEquals("simple", resourceTypeList.get(0).getName());
	}

	@Test 
	public void testCreateResourceTypeWithConflictedResourceTypes(){
		resourceExpert.createResourceType("resourcetype").addConflictedResourceTypes(resourceExpert.createResourceType("conflict").build()).build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		assertEquals(2, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(1).getConflictedResourceTypes().size());

		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(resourceTypeList.get(1).getConflictedResourceTypes());

		assertEquals("conflict", conflictedRTlist.get(0).getName());
	}

	@Test 
	public void testCreateResourceTypeWithRequiredResourceTypes(){
		resourceExpert.createResourceType("resourcetype").addRequiredResourceTypes(resourceExpert.createResourceType("required").build()).build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		assertEquals(2, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(1).getRequiredResourceTypes().size());

		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(resourceTypeList.get(1).getRequiredResourceTypes());
		
		assertEquals("required", requiredRTlist.get(0).getName());
	}

	@Test 
	public void testCreateResourceTypeWithRequiredAndConflictedResourceTypes(){
		resourceExpert.createResourceType("resourcetype").addRequiredResourceTypes(resourceExpert.createResourceType("required").build()).addConflictedResourceTypes(resourceExpert.createResourceType("conflict").build()).build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		assertEquals(3, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(2).getRequiredResourceTypes().size());
		assertEquals(1, resourceTypeList.get(2).getConflictedResourceTypes().size());

		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(resourceTypeList.get(2).getRequiredResourceTypes());
		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(resourceTypeList.get(2).getConflictedResourceTypes());
	
		assertEquals("required", requiredRTlist.get(0).getName());
		assertEquals("conflict", conflictedRTlist.get(0).getName());
	}

	@Test
	public void cannotHaveNullResourceType(){
		resourceExpert.createResourceType("simple").build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		assertFalse(resourceExpert.canHaveResource(resourceTypeList.get(0)));
	}

	@Test 
	public void cannotHaveSameResourceType(){
		assertFalse(resourceExpert.canHaveResource(null));

	}
}
