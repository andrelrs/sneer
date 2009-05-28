package sneer.kernel.container.tests;

import static sneer.commons.environments.Environments.my;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import sneer.brickness.Brickness;
import sneer.commons.environments.Environment;
import sneer.commons.environments.EnvironmentUtils;
import sneer.commons.environments.Environments;
import sneer.commons.lang.ByRef;
import sneer.commons.lang.exceptions.NotImplementedYet;
import sneer.hardware.gui.timebox.TimeboxedEventQueue;

@Ignore
public class GuiBrickTest extends Assert {
	
	@Test
	public void guiBrickRunsInSwingThread() throws Exception {
		final Environment environment = Brickness.newBrickContainer();
		final SomeGuiBrick brick = EnvironmentUtils.retrieveFrom(environment, SomeGuiBrick.class);
		assertSame(swingThread(), brick.currentThread());
	}
	
	@Test
	public void guiBrickRunsInContainerEnvironment() throws Exception {
		final Environment environment = Brickness.newBrickContainer();
		final SomeGuiBrick brick = EnvironmentUtils.retrieveFrom(environment, SomeGuiBrick.class);
		assertSame(environment, brick.currentEnvironment());
	}

	@Test
	public void injectedGuiBrickRunsInSwingThread() throws Exception {
		SomeGuiBrick binding = new SomeGuiBrick() {			
			@Override
			public Thread currentThread() {
				return Thread.currentThread();
			}

			@Override
			public void slowMethod() {
				throw new IllegalStateException();
			}

			@Override
			public Environment currentEnvironment() {
				throw new IllegalStateException();
			}
		};
		
		final Environment environment = Brickness.newBrickContainer(binding);

		final SomeGuiBrick brick = EnvironmentUtils.retrieveFrom(environment, SomeGuiBrick.class);
		
		assertSame(swingThread(), brick.currentThread());
	}

	@Test
	public void guiBrickCallbacksComeInSwingThread() throws Exception {
		throw new NotImplementedYet();
	}
	
	@Test
	public void testGuiBrickRunsInsideTimebox() throws Exception {
		Environments.runWith(Brickness.newBrickContainer(), new Runnable() { @Override public void run() {
			
			int timeoutForGuiEvents = 10;
			my(TimeboxedEventQueue.class).startQueueing(timeoutForGuiEvents);
	
			try {
				runInsideTimebox();
			} finally {
				my(TimeboxedEventQueue.class).stopQueueing();
			}
			
		}});
	}


	private void runInsideTimebox() {
		final Environment environment = Brickness.newBrickContainer();
		final SomeGuiBrick brick = EnvironmentUtils.retrieveFrom(environment, SomeGuiBrick.class);
		try {
			brick.slowMethod();
		} catch (Throwable expected) {
			return;
		}
		fail("timebox should have stopped the method");
	}

	@Test
	public void testNonGuiBrickRunsInCurrentThread() throws Exception {
		final SomeVanillaBrick brick = EnvironmentUtils.retrieveFrom(Brickness.newBrickContainer(), SomeVanillaBrick.class);
		assertSame(Thread.currentThread(), brick.brickThread());
	}
	
	private Thread swingThread() throws Exception {
		final ByRef<Thread> swingThread = ByRef.newInstance();
		SwingUtilities.invokeAndWait(new Runnable() { @Override public void run() {
			swingThread.value = Thread.currentThread();
		}});
		return swingThread.value;
	}
}
