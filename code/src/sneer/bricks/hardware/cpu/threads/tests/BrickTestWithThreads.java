package sneer.bricks.hardware.cpu.threads.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.After;

import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.hardware.io.log.tests.BrickTestWithLogger;
import sneer.foundation.environments.Environment;
import sneer.foundation.environments.Environments;
import sneer.foundation.lang.Closure;

public abstract class BrickTestWithThreads extends BrickTestWithLogger {

	@After
	public void afterBrickTestWithTreads() {
		crash();
	}

	protected void crash(Environment environment) {
		Environments.runWith(environment, new Closure() { @Override public void run() {
			crash();
		}});
	}

	private void crash() {
		my(Threads.class).crashAllThreads();
	}

}
