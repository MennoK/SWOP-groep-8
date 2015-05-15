package taskmanager;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class CompanyTester {

	TaskManController tmc;
	Company company;

	@Before
	public void SetUp() {
		
		this.tmc = new TaskManController(LocalDateTime.of(2015, 10, 10, 10, 10));
		this.company = tmc.getCompany();

	}

	@Test
	public void testCreateBranchOffice() {
		company.createBranchOffice("Baarle-Hertog");
		assertEquals(1, company.getAllBranchOffices().size());
		company.createBranchOffice("Brugge");
		assertEquals(2, company.getAllBranchOffices().size());
		ArrayList<BranchOffice> boList = new ArrayList<BranchOffice>();
		boList.addAll(company.getAllBranchOffices());
		assertEquals("Baarle-Hertog", boList.get(0).getLocation());
	}

	@Test
	public void testGetAllBranchOfficeWithNoBranchOfficeReturnsAnEmptySet() {
		assertTrue(company.getAllBranchOffices().isEmpty());
		assertFalse(company.getAllBranchOffices() == null);
	}

	@Test
	public void testCannotHaveTheSameBranchOffice() {
		company.createBranchOffice("Baarle-Hertog");
		ArrayList<BranchOffice> boList = new ArrayList<BranchOffice>();
		boList.addAll(company.getAllBranchOffices());
	}
}
