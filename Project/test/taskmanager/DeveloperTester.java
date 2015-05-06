package taskmanager;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import taskmanager.Developer;
import taskmanager.DeveloperExpert;

public class DeveloperTester {

	private Developer dev;
	private DeveloperExpert developerExpert;
	
	@Before
	public void setUp(){
		//new developerExpert and create a new developer
		developerExpert = new DeveloperExpert();
		developerExpert.createDeveloper("Bob");
		ArrayList<Developer> devList = new ArrayList<Developer>();
		devList.addAll(developerExpert.getAllDevelopers());
		dev = devList.get(0);
	}
	@Test
	public void testGetName() {
		assertEquals("Bob", dev.getName());
	}

}
