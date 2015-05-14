package taskmanager;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class CompanyTester {

	private Company company;

	@Before
	public void SetUp() {
		company = new Company();
	}

	@Test
	public void testCreateBranchOffice() {
		company.createBranchOffice("Baarle-Hertog");
		assertEquals(1, company.getAllBranchOffices().size());
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
