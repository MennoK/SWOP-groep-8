package UseCaseTests;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import TaskManager.Clock;

public class UseCase5AdvanceTime {
	private Clock clock;
	@Before
	public void setUp(){
		clock = new Clock(LocalDateTime.of(2015, 03, 01,01,00));
	}
	@Test
	public void testAdvanceTimeFutureSimpleTest() {
		clock.setTime(LocalDateTime.of(2016, 06, 06,06,06));
		assertEquals(LocalDateTime.of(2016, 06, 06,06,06), clock.getTime());
	}
	@Test(expected=IllegalArgumentException.class)
	public void testAdvanceTimePastSimpleTest() {
		clock.setTime(LocalDateTime.of(2012, 06, 06,06,06));
	}
	//TODO: implement, what changes when time moves on?
	@Test
	public void testAdvanceTimeFutureAndCheckChanges() {
		clock.setTime(LocalDateTime.of(2016, 06, 06,06,06));
		assertEquals(LocalDateTime.of(2016, 06, 06,06,06), clock.getTime());
	}
}
