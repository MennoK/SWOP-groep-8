package taskManager;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import taskManager.TaskManClock;

public class TaskManClockTester {

	private TaskManClock taskManClock;

	@Before
	public void setUp() {
		taskManClock = new TaskManClock(LocalDateTime.of(2015, 03, 07, 01, 00));
	}

	@Test
	public void testSetTimeAfterCurrentTime() {
		try {
			taskManClock.setTime(LocalDateTime.of(2015, 03, 07, 02, 00));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		assertEquals(taskManClock.getTime(),
				LocalDateTime.of(2015, 03, 07, 02, 00));
	}

	@Test
	public void testSetTimeOnCurrentTime() {
		try {
			taskManClock.setTime(LocalDateTime.of(2015, 03, 07, 01, 00));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		assertEquals(taskManClock.getTime(),
				LocalDateTime.of(2015, 03, 07, 01, 00));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetTimeBeforeCurrentTime() {
		taskManClock.setTime(LocalDateTime.of(2015, 03, 06, 00, 00));
	}

}
