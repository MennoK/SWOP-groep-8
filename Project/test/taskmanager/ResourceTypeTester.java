package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Resource;
import taskmanager.ResourceType;

public class ResourceTypeTester extends TaskManTester {

	private ResourceType resourceType;
	private ResourceType requiredResourceType;
	private ResourceType conflictedResourceType;

	@Before
	public void setUp() {
		super.setUp();
		resourceType = ResourceType.builder("type")
				.build(tmc.getActiveOffice());
		requiredResourceType = ResourceType.builder("requiredResourceType")
				.build(tmc.getActiveOffice());
		conflictedResourceType = ResourceType.builder("conflictedResourceType")
				.build(tmc.getActiveOffice());
	}

	@Test
	public void testGetName() {
		assertEquals("type", resourceType.getName());
	}

	@Test
	public void testCreateResource() {
		Resource res = resourceType.createResource("resource");

		assertEquals(1, resourceType.getAllResources().size());
		assertTrue(resourceType.getAllResources().contains(res));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCannotHaveSameResource() {
		Resource res = resourceType.createResource("resource");
		resourceType.addResource(res);
	}

	@Test
	public void testAddRequiredResourceType() {
		ResourceType requiringResType = ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType)
				.build(tmc.getActiveOffice());
		assertEquals(4, tmc.getAllResourceTypes().size());
		assertTrue(requiringResType.getRequiredResourceTypes().contains(
				requiredResourceType));
	}

	@Test
	public void testAddConflictedResourceType() {
		ResourceType conflictingResType = ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType)
				.build(tmc.getActiveOffice());
		assertEquals(4, tmc.getAllResourceTypes().size());
		assertTrue(conflictingResType.getConflictedResourceTypes().contains(
				conflictedResourceType));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInRequiredList() {
		ResourceType resType = ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType)
				.build(tmc.getActiveOffice());
		// TODO discuss do we need this function, doesn't the builder do this?
		resType.addRequiredResourceType(requiredResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRequiredResourceTypeIsAlreadyInConflictedList() {
		ResourceType resType = ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType)
				.build(tmc.getActiveOffice());
		// TODO discuss do we need this function, doesn't the builder do this?
		resType.addConflictedResourceType(requiredResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInRequiredList() {
		ResourceType resType = ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType)
				.build(tmc.getActiveOffice());
		// TODO discuss do we need this function, doesn't the builder do this?
		resType.addRequiredResourceType(conflictedResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConflictedResourceTypeIsAlreadyInConflictedList() {
		ResourceType resType = ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType)
				.build(tmc.getActiveOffice());
		resType.addConflictedResourceType(conflictedResourceType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingRequiredTypes() {
		ResourceType resType = ResourceType.builder("type")
				.addRequiredResourceTypes(requiredResourceType)
				.build(tmc.getActiveOffice());
		requiredResourceType.addRequiredResourceType(resType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLoopingConflictedTypes() {
		ResourceType resType = ResourceType.builder("type")
				.addConflictedResourceTypes(conflictedResourceType)
				.build(tmc.getActiveOffice());
		conflictedResourceType.addConflictedResourceType(resType);
	}

}
