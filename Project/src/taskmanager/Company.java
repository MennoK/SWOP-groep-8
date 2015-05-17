package taskmanager;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A company consists of multiple branch offices.
 * 
 * @author menno
 *
 */
public class Company {

	private Set<BranchOffice> branchOffices;
	private ImmutableClock clock;

	/**
	 * Default constructor of Company class. Initializes a new set of branch
	 * offices
	 */
	Company(ImmutableClock clock) {
		this.clock = clock;
		this.branchOffices = new LinkedHashSet<BranchOffice>();
	}

	/**
	 * Creates a new branch office with the given name. and adds the new branch
	 * office to the set of all branch offices
	 * 
	 * @param name
	 *            : given name
	 */
	BranchOffice createBranchOffice(String location) {
		BranchOffice branchOffice = new BranchOffice(location, clock);
		branchOffices.add(branchOffice);
		return branchOffice;
	}

	/**
	 * @return branchOffices : set of all branchOffices
	 */
	Set<BranchOffice> getAllBranchOffices() {
		return branchOffices;
	}

}
