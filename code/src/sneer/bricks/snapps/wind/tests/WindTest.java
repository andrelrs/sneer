package sneer.bricks.snapps.wind.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.Test;

import sneer.bricks.expression.tuples.TupleSpace;
import sneer.bricks.hardware.clock.Clock;
import sneer.bricks.snapps.chat.ChatMessage;
import sneer.bricks.snapps.wind.Wind;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;

public class WindTest extends BrickTestBase {

	private static final long YEAR_ONE = 1000l * 60l * 60l * 24l * 356l;
	private final Wind _subject = my(Wind.class);
	
	@Test(timeout = 4000)
	public void oldShoutsAreNotHeard() {
		ChatMessage ahhh = new ChatMessage("AHHH!!!");

		my(Clock.class).advanceTimeTo(YEAR_ONE);
		tupleSpace().acquire(ahhh);

		ChatMessage choo = new ChatMessage("CHOOO!!!");
		tupleSpace().acquire(choo);

		tupleSpace().waitForAllDispatchingToFinish();

		assertTrue(_subject.shoutsHeard().currentElements().contains(choo));
		assertEquals(1, _subject.shoutsHeard().currentElements().size());
	}

	
	@Test(timeout = 4000)
	public void testSortedShoutsHeard() {
		my(Clock.class).advanceTimeTo(15);
		tupleSpace().acquire(new ChatMessage(""+15));

		for (int i = 30; i > 20; i--) {
			my(Clock.class).advanceTimeTo(i);
			tupleSpace().acquire(new ChatMessage(""+i));
		}
		
		for (int i = 10; i > 0; i--) {
			my(Clock.class).advanceTimeTo(i);
			tupleSpace().acquire(new ChatMessage(""+i));
		}

		tupleSpace().waitForAllDispatchingToFinish();
		ChatMessage previousShout = null;
		for (ChatMessage shout : _subject.shoutsHeard()) {
			
			if (previousShout == null) {
				previousShout = shout;
				continue;
			}
			
			assertTrue(previousShout.publicationTime < shout.publicationTime);
			previousShout = shout;
		}

		assertEquals(21, _subject.shoutsHeard().size().currentValue().intValue());
	}

	private TupleSpace tupleSpace() {
		return my(TupleSpace.class);
	}
}