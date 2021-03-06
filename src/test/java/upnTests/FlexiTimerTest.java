package upnTests;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;  

import org.junit.Ignore;
import org.junit.Test;

import upsilon.node.dataStructures.ResultKarma;
import upsilon.node.util.FlexiTimer;
import upsilon.node.util.MutableFlexiTimer;
import upsilon.node.util.Util;

public class FlexiTimerTest {
	private static final long SECONDS_IN_A_DAY = 86400;

	@Test
	public void testBadResult() {
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(10), Duration.ofSeconds(100), Duration.ofSeconds(10), "");

		Assert.assertEquals(0, ft.getConsequtiveCount());
 
		ft.submitResult(ResultKarma.BAD);

		Assert.assertEquals(1, ft.getConsequtiveCount());
	}

	@Test
	@Ignore
	public void testDateBasedTimers() {
		long remaining;
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(10), Duration.ofSeconds(100), Duration.ofSeconds(10), "");

		Calendar before = new GregorianCalendar(2012, 1, 2, 6, 1, 1);
		Calendar after = new GregorianCalendar(2012, 1, 3, 6, 1, 1);

		ft.touch(before.getTime());
		remaining = ft.getSecondsRemaining(after.getTime());

		Assert.assertEquals(-FlexiTimerTest.SECONDS_IN_A_DAY, remaining, 1f);
		Assert.assertTrue(ft.isTouchedPassed(after.getTime()));

		ft.touch(after.getTime());
		remaining = ft.getSecondsRemaining(before.getTime());

		Assert.assertEquals(+FlexiTimerTest.SECONDS_IN_A_DAY, remaining, 1f);
		Assert.assertFalse(ft.isTouchedPassed(before.getTime()));
	}

	@Test
	@Ignore
	public void testDateTimer() {
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(1), Duration.ofSeconds(100), Duration.ofSeconds(10), "");

		Assert.assertFalse(ft.isTouchedPassed());
		
		ft.touch();
		Assert.assertEquals(1, ft.getSecondsRemaining());
 
		Util.lazySleep(Duration.ofMillis(10));  

		Assert.assertTrue(ft.isTouchedPassed());
		 
		Assert.assertNotNull(ft.toString());
	}

	@Test
	public void testLimitingBounds() {
		Assert.assertEquals(50, FlexiTimer.getIntWithinBounds(50, 0, 100));
		Assert.assertEquals(100, FlexiTimer.getIntWithinBounds(100, 0, 100));
		Assert.assertEquals(50, FlexiTimer.getIntWithinBounds(45, 50, 100));
		Assert.assertEquals(100, FlexiTimer.getIntWithinBounds(105, 50, 100)); 
		Assert.assertEquals(75, FlexiTimer.getIntWithinBounds(75, 0, 100));
	} 
 
	@Test
	public void testServiceDelay() {
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(10), Duration.ofSeconds(100), Duration.ofSeconds(10), "");

		Assert.assertEquals(0, ft.getConsequtiveCount());
		Assert.assertEquals(10, ft.getCurrentDelay().get(SECONDS));
 
		ft.submitResult(ResultKarma.GOOD);
		
		Assert.assertEquals(1, ft.getConsequtiveCount());
		Assert.assertEquals(20, ft.getCurrentDelay().get(SECONDS));
	} 

	@Test
	public void testTimerDelaysAfterBadResults() {
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(10), Duration.ofSeconds(100), Duration.ofSeconds(10), "test timer delays after bad results");

		Assert.assertEquals(10, ft.getCurrentDelay().get(SECONDS)); // initial
																			// delay
		ft.submitResult(ResultKarma.GOOD);
		Assert.assertEquals(20, ft.getCurrentDelay().get(SECONDS)); // one
																			// good
																			// result

		ft.submitResult(ResultKarma.BAD);
		Assert.assertEquals(10, ft.getCurrentDelay().get(SECONDS)); // reset
																			// result
																			// with
									 										// bad

		ft.submitResult(ResultKarma.GOOD, 7);
		Assert.assertEquals(70, ft.getCurrentDelay().get(SECONDS)); // 7
																			// good
																			// results
	}

	@Test
	public void testTimerDelaysAfterGoodResults() {
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(10), Duration.ofSeconds(30), Duration.ofSeconds(10), "");

		Assert.assertEquals(10, ft.getCurrentDelay().get(SECONDS));
		ft.submitResult(ResultKarma.GOOD, 999);
		Assert.assertEquals(30, ft.getCurrentDelay().get(SECONDS));
	} 

	@Test
	public void testTimerSetters() {
		MutableFlexiTimer ft = new MutableFlexiTimer(Duration.ofSeconds(10), Duration.ofSeconds(100), Duration.ofSeconds(10), "");

		ft.setMin(Duration.ofSeconds(10));
		Assert.assertEquals(10, ft.getMinimumDelay().get(SECONDS));

		ft.setMax(Duration.ofSeconds(100));
		Assert.assertEquals(100, ft.getMaximumDelay().get(SECONDS));

		ft.setName("test1");
		Assert.assertEquals("test1", ft.getName());
	}
}
