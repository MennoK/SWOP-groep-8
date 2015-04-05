package taskManager;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class DeveloperExpertTester {

	private DeveloperExpert developerExpert;
	
	@Before
	public void SetUp(){
		developerExpert = new DeveloperExpert();
	}
	
	@Test
	public void createDeveloper() {
		developerExpert.createDeveloper("Bob");
		assertEquals(1, developerExpert.getAllDevelopers().size());
		ArrayList<Developer> devList = new ArrayList<Developer>();
		devList.addAll(developerExpert.getAllDevelopers());
		assertEquals("Bob", devList.get(0).getName());
	}
	
	@Test
	public void cannotHaveTheSameDeveloper(){
		developerExpert.createDeveloper("Bob");
		ArrayList<Developer> devList = new ArrayList<Developer>();
		devList.addAll(developerExpert.getAllDevelopers());
		assertFalse(developerExpert.canHaveDeveloper(devList.get(0)));
	}
	
	@Test 
	public void cannotHaveNullDeveloper(){
		assertFalse(developerExpert.canHaveDeveloper(null));
	}
}
