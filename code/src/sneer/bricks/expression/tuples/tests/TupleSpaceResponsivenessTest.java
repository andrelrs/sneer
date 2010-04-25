package sneer.bricks.expression.tuples.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.Test;

import sneer.bricks.expression.tuples.TupleSpace;
import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.hardware.cpu.threads.mocks.ThreadsMock;
import sneer.bricks.software.folderconfig.tests.BrickTest;
import sneer.foundation.brickness.testsupport.Bind;
import sneer.foundation.lang.ByRef;
import sneer.foundation.lang.Consumer;

public class TupleSpaceResponsivenessTest extends BrickTest {

	@Bind private final ThreadsMock _threads = new ThreadsMock();
	
	private final TupleSpace _subject = my(TupleSpace.class);
	
	@Test (timeout = 1000)
	public void test() {
		final ByRef<Boolean> wasPublished = ByRef.newInstance(false);
		@SuppressWarnings("unused")	WeakContract contract = _subject.addSubscription(TestTuple.class, new Consumer<TestTuple>() { @Override public void consume(TestTuple value) {
			wasPublished.value = true;
		}});

		final TestTuple tuple = new TestTuple(42);
		_subject.acquire(tuple);
		
		assertFalse(wasPublished.value);
		_threads.getStepper(0).run();
		assertTrue(wasPublished.value);
	}
	
}

