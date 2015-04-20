package taskManager;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ResourceTypeTester {

	private ResourceExpert resourceExpert;
	private ResourceType resourceType;
	private ResourceType requiredResourceType;
	private ResourceType conflictedResourceType;
	private List<ResourceType> resourceTypeList;

	@Before
	public void setUp() {
		resourceExpert = new ResourceExpert();
		resourceExpert.resourceTypeBuilder("type").build();
		resourceExpert.resourceTypeBuilder("requiredResourceType").build();
		resourceExpert.resourceTypeBuilder("conflictedResourceType").build();

		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceType = resourceTypeList.get(0);
		requiredResourceType = resourceTypeList.get(1);
		conflictedResourceType = resourceTypeList.get(2);
	}

	@Test
	public void testGetName() {
		assertEquals("type", resourceType.getName());
	}

	@Test
	public void testCreateResource() {
		resourceType.createResource("resource");
		List<Resource> resourceList = new ArrayList<Resource>(
				resourceType.getAllResources());

		assertEquals(1, resourceType.getAllResources().size());
		assertEquals("resource", resourceList.get(0).getName());
	}

	@Test
	public void testCannotHaveNullResource() {
		assertFalse(resourceType.canHaveResource(null));
	}

	@Test
	public void testCannotHaveSameResource() {
		resourceType.createResource("resource");
		List<Resource> resourceList = new ArrayList<Resource>(
				resourceType.getAllResources());

		assertFalse(resourceType.canHaveResource(resourceList.get(0)));
	}

	@Test
	public void testAddRequiredResourceType() {
		resourceExpert.resourceTypeBuilder("type")
				.addRequiredResourceTypes(requiredResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		assertEquals(4, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(3).getRequiredResourceTypes()
				.size());
	}

	@Test
	public void testAddConflictedResourceType() {
		resourceExpert.resourceTypeBuilder("type")
				.addConflictedResourceTypes(requiredResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		assertEquals(4, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(3).getConflictedResourceTypes()
				.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInRequiredList() {
		resourceExpert.resourceTypeBuilder("type")
				.addRequiredResourceTypes(requiredResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addRequiredResourceType(requiredResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInConflictedList() {
		resourceExpert.resourceTypeBuilder("type")
				.addRequiredResourceTypes(requiredResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addConflictedResourceType(requiredResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInRequiredList() {
		resourceExpert.resourceTypeBuilder("type")
				.addConflictedResourceTypes(conflictedResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addRequiredResourceType(conflictedResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInConflictedList() {
		resourceExpert.resourceTypeBuilder("type")
				.addConflictedResourceTypes(conflictedResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addConflictedResourceType(
				conflictedResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingRequiredTypes() {
		resourceExpert.resourceTypeBuilder("type")
				.addRequiredResourceTypes(requiredResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		requiredResourceType.addRequiredResourceType(resourceTypeList.get(3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingConflictedTypes() {
		resourceExpert.resourceTypeBuilder("type")
				.addConflictedResourceTypes(conflictedResourceType).build();
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		conflictedResourceType.addConflictedResourceType(resourceTypeList
				.get(3));
	}

}
