package taskmanager;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Resource;
import taskmanager.ResourceType;
import taskmanager.TaskManController;

public class ResourceTester {

	private TaskManController tmc;
	private ResourceType resourceType;

	@Before
	public void setUp() {
		tmc = new TaskManController(LocalDateTime.of(2000, 03, 05, 00, 00));
		resourceType = ResourceType.builder("name").build(tmc);
	}

	@Test
	public void testGetName() {
		resourceType.createResource("resource");
		ArrayList<Resource> resourceList = new ArrayList<Resource>();
		resourceList.addAll(resourceType.getAllResources());
		assertEquals("resource", resourceList.get(0).getName());
	}

}
