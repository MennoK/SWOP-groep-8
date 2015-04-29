package utility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class TimeSpanTester {

	LocalDateTime before1;
	LocalDateTime before2;
	LocalDateTime begin;
	LocalDateTime during1;
	LocalDateTime during2;
	LocalDateTime end;
	LocalDateTime after1;
	LocalDateTime after2;

	TimeSpan span;

	@Before
	public void setUp() throws Exception {
		before1 = LocalDateTime.of(2015, 4, 22, 8, 0);
		before2 = before1.plusHours(1);
		begin = before1.plusHours(2);
		during1 = before1.plusHours(3);
		during2 = before1.plusHours(4);
		end = before1.plusHours(5);
		after1 = before1.plusHours(6);
		after2 = before1.plusHours(7);
		span = new TimeSpan(begin, end);
	}

	@Test
	public void testOverlap() {
		assertFalse(span.overlaps(new TimeSpan(before1, before2)));
		assertFalse(span.overlaps(new TimeSpan(before2, begin)));
		assertTrue(span.overlaps(new TimeSpan(begin, during1)));
		assertTrue(span.overlaps(new TimeSpan(during1, during2)));
		assertTrue(span.overlaps(new TimeSpan(during2, end)));
		assertFalse(span.overlaps(new TimeSpan(end, after1)));
		assertFalse(span.overlaps(new TimeSpan(after1, after2)));

		assertTrue(span.overlaps(new TimeSpan(before2, during1)));
		assertTrue(span.overlaps(new TimeSpan(before2, end)));
		assertTrue(span.overlaps(new TimeSpan(before2, after1)));
		assertTrue(span.overlaps(new TimeSpan(begin, after1)));
		assertTrue(span.overlaps(new TimeSpan(during2, after1)));

		assertTrue(span.overlaps(new TimeSpan(begin, end)));
	}

}
