package taskmanager;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Resource;
import taskmanager.ResourceType;

public class ResourceTester extends TaskManTester {

	private ResourceType resourceType;

	@Before
	public void setUp() {
		super.setUp();
		resourceType = ResourceType.builder("name")
				.build(tmc.getActiveOffice());
	}

	@Test
	public void testGetName() {
		Resource res = resourceType.createResource("resource");
		assertTrue(resourceType.getAllResources().contains(res));
	}

}
