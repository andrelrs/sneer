package sneer.bricks.hardware.cpu.threads.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.hardware.cpu.lang.contracts.Contract;
import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.hardware.cpu.threads.latches.Latch;
import sneer.bricks.hardware.cpu.threads.latches.Latches;
import sneer.bricks.pulp.events.pulsers.PulseSource;
import sneer.bricks.pulp.events.pulsers.Pulser;
import sneer.bricks.pulp.events.pulsers.Pulsers;
import sneer.foundation.lang.Closure;

class ThreadsImpl implements Threads {

	private static final Latches Latches = my(Latches.class);

	private final Latch _crash = Latches.produce();
	private final Pulser _crashedPulser = my(Pulsers.class).newInstance();
	static private boolean _isCrashing = false;

	@Override
	public void waitWithoutInterruptions(Object object) {
		try {
			object.wait();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sleepWithoutInterruptions(long milliseconds) {
		try {
			Thread.sleep(milliseconds);

		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}


	@Override
	public void joinWithoutInterruptions(Thread thread) {
		try {
			thread.join();

		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void startDaemon(final String threadName, final Closure closure) {
		if (_isCrashing) {
			Daemons.killQuietly(Thread.currentThread());
			return;
		}

		new Daemon(threadName, closure);
	}

	
	@Override
	public Contract startStepping(Closure steppable) {
		return startStepping(inferThreadName(), steppable);
	}

	
	@Override
	public Contract startStepping(String threadName, Closure steppable) {
		Stepper result = new Stepper(steppable);
		startDaemon(threadName, result);
		return result;
	}

	
	private String inferThreadName() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement element = stackTrace[3];
		String className = toSimpleClassName(element.getClassName());
		
		return className + "." + element.getMethodName() + "(" + Threads.class.getClassLoader() + ")"; 
	}

	private static String toSimpleClassName(String className) {
		return className.substring(className.lastIndexOf(".") + 1);
	}

	/**Waits until crashAllThreads() is called. */
	@Override
	public void waitUntilCrash() {
		_crash.waitTillOpen();
	}

	@Override
	public void crashAllThreads() {
		_isCrashing = true;

		Daemons.killAllInstances();

		_crashedPulser.sendPulse();
		_crash.open();
	}

	@Override
	public PulseSource crashed() {
		return _crashedPulser.output();
	}

}
