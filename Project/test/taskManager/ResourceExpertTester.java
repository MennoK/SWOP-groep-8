package taskManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import utility.TimeInterval;

public class ResourceExpertTester {

	private TaskManController tmc;

	@Before
	public void setUp() {
		tmc = new TaskManController(LocalDateTime.of(2000, 03, 05, 00, 00));
	}

	@Test
	public void testResourceTypeSetIsInitialized() {
		assertEquals(0, tmc.getAllResourceTypes().size());
	}

	@Test
	public void testCreateSimpleResourceType() {
		ResourceType.builder("simple").build(tmc);
		assertEquals(1, tmc.getAllResourceTypes().size());
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>();
		resourceTypeList.addAll(tmc.getAllResourceTypes());
		assertEquals("simple", resourceTypeList.get(0).getName());
	}

	@Test
	public void testCreateResourceTypeWithConflictedResourceTypes() {
		ResourceType.builder("conflict").build(tmc);
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		ResourceType.builder("resourcetype")
				.addConflictedResourceTypes(resourceTypeList.get(0)).build(tmc);
		resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		assertEquals(2, tmc.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(1).getConflictedResourceTypes()
				.size());

		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(1).getConflictedResourceTypes());

		assertEquals("conflict", conflictedRTlist.get(0).getName());
	}

	@Test
	public void testCreateResourceTypeWithRequiredResourceTypes() {
		ResourceType
				.builder("resourcetype")
				.addRequiredResourceTypes(
						ResourceType.builder("required").build(tmc)).build(tmc);
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		assertEquals(2, tmc.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(1).getRequiredResourceTypes()
				.size());

		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(1).getRequiredResourceTypes());

		assertEquals("required", requiredRTlist.get(0).getName());
	}

	@Test
	public void testCreateResourceTypeWithRequiredAndConflictedResourceTypes() {
		ResourceType
				.builder("resourcetype")
				.addRequiredResourceTypes(
						ResourceType.builder("required").build(tmc))
				.addConflictedResourceTypes(
						ResourceType.builder("conflict").build(tmc)).build(tmc);
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		assertEquals(3, tmc.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(2).getRequiredResourceTypes()
				.size());
		assertEquals(1, resourceTypeList.get(2).getConflictedResourceTypes()
				.size());

		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(2).getRequiredResourceTypes());
		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(
				resourceTypeList.get(2).getConflictedResourceTypes());

		assertEquals("required", requiredRTlist.get(0).getName());
		assertEquals("conflict", conflictedRTlist.get(0).getName());
	}

	@Test
	public void testCreateResourceTypeWithDailyAvailability() {
		ResourceType
				.builder("resourcetype")
				.addDailyAvailability(
						new TimeInterval(LocalTime.of(12, 00), LocalTime.of(17,
								00))).build(tmc);
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());
		assertEquals(LocalTime.of(12, 00), resourceTypeList.get(0)
				.getDailyAvailability().getBegin());
		assertEquals(LocalTime.of(17, 00), resourceTypeList.get(0)
				.getDailyAvailability().getEnd());
	}

	@Test
	public void cannotHaveNullResourceType() {
		ResourceType.builder("simple").build(tmc);
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(
				tmc.getAllResourceTypes());

		assertFalse(tmc.getResourceExpert().canHaveResource(
				resourceTypeList.get(0)));
	}

	@Test
	public void cannotHaveSameResourceType() {
		assertFalse(tmc.getResourceExpert().canHaveResource(null));

	}
}
