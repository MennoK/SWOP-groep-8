package utility;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class WorkTimeTester {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testEstimatedFinishTime() {
		LocalDateTime finishtime = WorkTime.getFinishTime(
				LocalDateTime.of(2015, 04, 22, 15, 0), Duration.ofHours(1));
		assertEquals(LocalDateTime.of(2015, 04, 22, 16, 0), finishtime);
	}

	@Test
	public void testDurationBetween() {
		LocalDateTime first = LocalDateTime.of(2015, 04, 22, 9, 0);
		LocalDateTime second = LocalDateTime.of(2015, 04, 23, 17, 0);
		assertEquals(Duration.ofHours(15),
				WorkTime.durationBetween(first, second));
	}

	@Test
	public void testEstimatedFinishTimeLong() {
		LocalDateTime finishtime = WorkTime.getFinishTime(
				LocalDateTime.of(2015, 04, 22, 15, 0), Duration.ofHours(19));
		assertEquals(LocalDateTime.of(2015, 04, 27, 9, 0), finishtime);
	}

	@Test
	public void testDurationBetweenLong() {
		LocalDateTime first = LocalDateTime.of(2015, 04, 22, 9, 0);
		LocalDateTime second = LocalDateTime.of(2015, 04, 27, 17, 0);
		assertEquals(Duration.ofHours(31),
				WorkTime.durationBetween(first, second));
	}

}
