package Test;

import static org.junit.Assert.*;

import java.time.LocalDateTime;


import TaskManager.InvalidTimeException;
import TaskManager.TaskManClock;
import org.junit.Before;
import org.junit.Test;

public class TaskManClockTester {

	private TaskManClock taskManClock;
	
	@Before
	public void setUp() {
		taskManClock = new TaskManClock(LocalDateTime.of(2015, 03, 07,01,00));
	}
	
	@Test
	public void testSetTimeAfterCurrentTime(){
		try {
			taskManClock.setTime(LocalDateTime.of(2015, 03, 07,02,00));
		} catch (InvalidTimeException e) {
			e.printStackTrace();
		}
		assertEquals(taskManClock.getTime(),LocalDateTime.of(2015, 03, 07,02,00));
	}

	@Test
	public void testSetTimeOnCurrentTime(){
		try {
			taskManClock.setTime(LocalDateTime.of(2015, 03, 07,01,00));
		} catch (InvalidTimeException e) {
			e.printStackTrace();
		}
		assertEquals(taskManClock.getTime(), LocalDateTime.of(2015, 03, 07,01,00));
	}

	@Test(expected=InvalidTimeException.class)
	public void testSetTimeBeforeCurrentTime() throws InvalidTimeException{
		taskManClock.setTime(LocalDateTime.of(2015, 03, 06,00,00));
	}

}
