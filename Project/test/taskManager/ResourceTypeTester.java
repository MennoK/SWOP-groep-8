package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
		ResourceType.builder("type").build(resourceExpert);
		ResourceType.builder("requiredResourceType").build(resourceExpert);
		ResourceType.builder("conflictedResourceType").build(resourceExpert);

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
		ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		assertEquals(4, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(3).getRequiredResourceTypes()
				.size());
	}

	@Test
	public void testAddConflictedResourceType() {
		ResourceType.builder("type")
				.addConflictedResourceTypes(requiredResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		assertEquals(4, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(3).getConflictedResourceTypes()
				.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInRequiredList() {
		ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addRequiredResourceType(requiredResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInConflictedList() {
		ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addConflictedResourceType(requiredResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInRequiredList() {
		ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addRequiredResourceType(conflictedResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInConflictedList() {
		ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		resourceTypeList.get(3).addConflictedResourceType(
				conflictedResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingRequiredTypes() {
		ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		requiredResourceType.addRequiredResourceType(resourceTypeList.get(3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingConflictedTypes() {
		ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType).build(resourceExpert);
		resourceTypeList = new ArrayList<ResourceType>(
				resourceExpert.getAllResourceTypes());
		conflictedResourceType.addConflictedResourceType(resourceTypeList
				.get(3));
	}

}
