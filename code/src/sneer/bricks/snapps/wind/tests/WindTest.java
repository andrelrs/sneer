package sneer.bricks.snapps.wind.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.Test;

import sneer.bricks.expression.tuples.TupleSpace;
import sneer.bricks.hardware.clock.Clock;
import sneer.bricks.snapps.wind.Shout;
import sneer.bricks.snapps.wind.Wind;
import sneer.bricks.software.folderconfig.testsupport.BrickTestWithFiles;

public class WindTest extends BrickTestWithFiles {

	private static final int YEAR_ONE = 1000 * 60 * 60 * 24 * 356;
	private final Wind _subject = my(Wind.class);
	
	@Test(timeout = 4000)
	public void oldShoutsAreNotHeard() {
		Shout ahhh = new Shout("AHHH!!!");

		my(Clock.class).advanceTimeTo(YEAR_ONE);
		tupleSpace().acquire(ahhh);

		Shout choo = new Shout("CHOOO!!!");
		tupleSpace().acquire(choo);

		tupleSpace().waitForAllDispatchingToFinish();

		assertTrue(_subject.shoutsHeard().currentElements().contains(choo));
		assertEquals(1, _subject.shoutsHeard().currentElements().size());
	}

	
	@Test(timeout = 4000)
	public void testSortedShoutsHeard() {
		my(Clock.class).advanceTimeTo(15);
		tupleSpace().acquire(new Shout(""+15));

		for (int i = 30; i > 20; i--) {
			my(Clock.class).advanceTimeTo(i);
			tupleSpace().acquire(new Shout(""+i));
		}
		
		for (int i = 10; i > 0; i--) {
			my(Clock.class).advanceTimeTo(i);
			tupleSpace().acquire(new Shout(""+i));
		}

		tupleSpace().waitForAllDispatchingToFinish();
		Shout previousShout = null;
		for (Shout shout : _subject.shoutsHeard()) {
			
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