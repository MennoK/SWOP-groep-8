package taskmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalTime;

import org.junit.Test;

import taskmanager.ResourceType;
import utility.TimeInterval;

public class ResourceExpertTester extends TaskManTester {

	@Test
	public void testResourceTypeSetIsInitialized() {
		assertEquals(0, tmc.getAllResourceTypes().size());
	}

	@Test
	public void testCreateSimpleResourceType() {
		ResourceType resType = ResourceType.builder("simple").build(
				tmc.getActiveOffice());
		assertEquals(1, tmc.getAllResourceTypes().size());
		assertEquals("simple", resType.getName());
	}

	@Test
	public void testCreateResourceTypeWithConflictedResourceTypes() {
		ResourceType conflictingResType = ResourceType.builder("conflict")
				.build(tmc.getActiveOffice());

		ResourceType resType = ResourceType.builder("resourcetype")
				.addConflictedResourceTypes(conflictingResType)
				.build(tmc.getActiveOffice());

		assertEquals(2, tmc.getAllResourceTypes().size());
		assertEquals(1, resType.getConflictedResourceTypes().size());

		assertTrue(resType.getConflictedResourceTypes().contains(
				conflictingResType));
	}

	@Test
	public void testCreateResourceTypeWithRequiredResourceTypes() {
		ResourceType required = ResourceType.builder("required").build(
				tmc.getActiveOffice());
		ResourceType resType = ResourceType.builder("resourcetype")
				.addRequiredResourceTypes(required)
				.build(tmc.getActiveOffice());

		assertEquals(2, tmc.getAllResourceTypes().size());
		assertEquals(1, resType.getRequiredResourceTypes().size());
		assertTrue(resType.getRequiredResourceTypes().contains(required));
	}

	@Test
	public void testCreateResourceTypeWithRequiredAndConflictedResourceTypes() {
		ResourceType required = ResourceType.builder("required").build(
				tmc.getActiveOffice());
		ResourceType conflicting = ResourceType.builder("conflict").build(
				tmc.getActiveOffice());
		ResourceType resType = ResourceType.builder("resourcetype")
				.addRequiredResourceTypes(required)
				.addConflictedResourceTypes(conflicting)
				.build(tmc.getActiveOffice());

		assertEquals(3, tmc.getAllResourceTypes().size());
		assertEquals(1, resType.getRequiredResourceTypes().size());
		assertEquals(1, resType.getConflictedResourceTypes().size());

		assertTrue(resType.getRequiredResourceTypes().contains(required));
		assertTrue(resType.getConflictedResourceTypes().contains(conflicting));
	}

	@Test
	public void testCreateResourceTypeWithDailyAvailability() {
		TimeInterval dailyAvailability = new TimeInterval(LocalTime.of(12, 00),
				LocalTime.of(17, 00));
		ResourceType resType = ResourceType.builder("resourcetype")
				.addDailyAvailability(dailyAvailability)
				.build(tmc.getActiveOffice());
		assertEquals(LocalTime.of(12, 00), resType.getDailyAvailability()
				.getBegin());
		assertEquals(LocalTime.of(17, 00), resType.getDailyAvailability()
				.getEnd());
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannNotHaveSameType() {
		ResourceType resType = ResourceType.builder("simple").build(
				tmc.getActiveOffice());
		tmc.getActiveOffice().getResourceExpert().addResourceType(resType);
	}
}
