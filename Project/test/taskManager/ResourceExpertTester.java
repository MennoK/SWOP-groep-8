package taskManager;

import static org.junit.Assert.*;

import java.time.LocalTime;
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
		resourceExpert.resourceTypeBuilder("simple").build();
		assertEquals(1, resourceExpert.getAllResourceTypes().size());
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>();
		resourceTypeList.addAll(resourceExpert.getAllResourceTypes());
		assertEquals("simple", resourceTypeList.get(0).getName());
	}

	@Test 
	public void testCreateResourceTypeWithConflictedResourceTypes(){
		resourceExpert.resourceTypeBuilder("conflict").build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		resourceExpert.resourceTypeBuilder("resourcetype").addConflictedResourceTypes(resourceTypeList.get(0)).build();
		resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());
	
		assertEquals(2, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(1).getConflictedResourceTypes().size());

		List<ResourceType> conflictedRTlist = new ArrayList<ResourceType>(resourceTypeList.get(1).getConflictedResourceTypes());

		assertEquals("conflict", conflictedRTlist.get(0).getName());
	}

	@Test 
	public void testCreateResourceTypeWithRequiredResourceTypes(){
		resourceExpert.resourceTypeBuilder("resourcetype").addRequiredResourceTypes(resourceExpert.resourceTypeBuilder("required").build()).build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		assertEquals(2, resourceExpert.getAllResourceTypes().size());
		assertEquals(1, resourceTypeList.get(1).getRequiredResourceTypes().size());

		List<ResourceType> requiredRTlist = new ArrayList<ResourceType>(resourceTypeList.get(1).getRequiredResourceTypes());
		
		assertEquals("required", requiredRTlist.get(0).getName());
	}

	@Test 
	public void testCreateResourceTypeWithRequiredAndConflictedResourceTypes(){
		resourceExpert.resourceTypeBuilder("resourcetype").addRequiredResourceTypes(resourceExpert.resourceTypeBuilder("required").build()).addConflictedResourceTypes(resourceExpert.resourceTypeBuilder("conflict").build()).build();
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
	public void testCreateResourceTypeWithDailyAvailability(){
		resourceExpert.resourceTypeBuilder("resourcetype").addDailyAvailability(new TimeInterval(LocalTime.of(12, 00), LocalTime.of(17, 00))).build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());
		assertEquals(LocalTime.of(12, 00), resourceTypeList.get(0).getDailyAvailability().getBegin());
		assertEquals(LocalTime.of(17, 00), resourceTypeList.get(0).getDailyAvailability().getEnd());
	}

	@Test
	public void cannotHaveNullResourceType(){
		resourceExpert.resourceTypeBuilder("simple").build();
		List<ResourceType> resourceTypeList = new ArrayList<ResourceType>(resourceExpert.getAllResourceTypes());

		assertFalse(resourceExpert.canHaveResource(resourceTypeList.get(0)));
	}

	@Test 
	public void cannotHaveSameResourceType(){
		assertFalse(resourceExpert.canHaveResource(null));

	}
}
