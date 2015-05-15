package useCase;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import taskmanager.TaskManController;
import taskmanager.Developer;
import taskmanager.BranchOffice;

import java.time.LocalDateTime;

public class UseCase0LogIn {

    private TaskManController tmc;
    private LocalDateTime now;
    private Developer me;
    private BranchOffice here;


    @Before
    public void SetUp() {
        now = LocalDateTime.of(1,2,3,4,5);
        tmc = new TaskManController(now);
        here = tmc.createBranchOffice("here");
        tmc.logIn(here);
        me = tmc.createDeveloper("ik");
        tmc.logOut();
    }


    @Test
    public void logInTest() {
        assertNull(tmc.getActiveOffice());
        assertNull(tmc.getActiveDeveloper());
        tmc.logIn(here);
        assertNotNull(tmc.getActiveOffice());
        assertNull(tmc.getActiveDeveloper());
        tmc.logIn(me);
        assertNotNull(tmc.getActiveOffice());
        assertNotNull(tmc.getActiveDeveloper());
    }
}
