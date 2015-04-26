package taskManager;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class ResourceTester {

	private ResourceType resourceType;
	
	@Before
	public void setUp() {
		ResourceExpert resourceExpert = new ResourceExpert();
		resourceType = ResourceType.builder("name").build(resourceExpert);
	}
	
	@Test
	public void testGetName(){
		resourceType.createResource("resource");
		ArrayList<Resource> resourceList = new ArrayList<Resource>();
		resourceList.addAll(resourceType.getAllResources());
		assertEquals("resource", resourceList.get(0).getName());
	}

}
