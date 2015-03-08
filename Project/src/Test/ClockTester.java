package Test;

import static org.junit.Assert.*;

import java.time.LocalDateTime;


import TaskManager.TaskManClock;
import org.junit.Before;
import org.junit.Test;

public class ClockTester {

	private TaskManClock taskManClock;
	
	@Before
	public void setUp() {
		taskManClock = new TaskManClock(LocalDateTime.of(2015, 03, 07,01,00));
	}
	
	@Test
	public void testSetTimeAfterCurrentTime(){
		taskManClock.setTime(LocalDateTime.of(2015, 03, 07,02,00));
		assertEquals(taskManClock.getTime(),LocalDateTime.of(2015, 03, 07,02,00));
	}

	@Test
	public void testSetTimeOnCurrentTime(){
		taskManClock.setTime(LocalDateTime.of(2015, 03, 07,01,00));
		assertEquals(taskManClock.getTime(), LocalDateTime.of(2015, 03, 07,01,00));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetTimeBeforeCurrentTime(){
		taskManClock.setTime(LocalDateTime.of(2015, 03, 06,00,00));
	}

}
