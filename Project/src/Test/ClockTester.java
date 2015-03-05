package Test;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


import org.junit.Before;
import org.junit.Test;

import TaskManager.Clock;

public class ClockTester {

	private Clock clock;
	
	@Before
	public void setUp() {
		clock = new Clock(LocalDateTime.of(2015, 03, 07,01,00));
	}
	
	@Test
	public void testSetTimeAfterCurrentTime(){
		clock.setTime(LocalDateTime.of(2015, 03, 07,02,00));
		assertEquals(clock.getTime(),LocalDateTime.of(2015, 03, 07,02,00));
	}

	@Test
	public void testSetTimeOnCurrentTime(){
		clock.setTime(LocalDateTime.of(2015, 03, 07,01,00));
		assertEquals(clock.getTime(), LocalDateTime.of(2015, 03, 07,01,00));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetTimeBeforeCurrentTime(){
		clock.setTime(LocalDateTime.of(2015, 03, 06,00,00));		
	}

}
