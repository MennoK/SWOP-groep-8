package useCase;

import java.time.LocalDateTime;

import taskmanager.BranchOffice;
import taskmanager.Developer;
import taskmanager.TaskManController;

public class UseCaseTestBasis {

	protected LocalDateTime now;
	protected TaskManController tmc;
	protected Developer activeUser;

	public void setUpTMC(LocalDateTime now) {
		this.now = now;
		tmc = new TaskManController(now);// BranchOffice(now);
		BranchOffice here = tmc.createBranchOffice("here",
				tmc.getTaskManClock());
		tmc.logIn(here);
		activeUser = tmc.createDeveloper("Jef");
		tmc.logIn(activeUser);
	}

}
