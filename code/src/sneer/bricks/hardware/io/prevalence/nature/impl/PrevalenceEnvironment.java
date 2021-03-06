package sneer.bricks.hardware.io.prevalence.nature.impl;

import static sneer.bricks.hardware.io.prevalence.nature.impl.PrevaylerHolder.building;
import static sneer.foundation.environments.Environments.my;
import sneer.bricks.hardware.io.prevalence.flag.PrevalenceFlag;
import sneer.bricks.hardware.io.prevalence.nature.Prevalent;
import sneer.foundation.brickness.Brick;
import sneer.foundation.brickness.Nature;
import sneer.foundation.environments.Environment;
import sneer.foundation.environments.NonBlockingEnvironment;
import sneer.foundation.lang.Producer;

class PrevalenceEnvironment implements Environment {

	static final PrevalenceEnvironment INSTANCE = new PrevalenceEnvironment();
	static private final PrevalenceFlag FLAG = new PrevalenceFlag() { @Override public boolean isInsidePrevalence() {
		return true;
	}};

	
	private PrevalenceEnvironment() {}
	
	
	private final NonBlockingEnvironment _environment = (NonBlockingEnvironment)my(Environment.class); 

	
	@Override
	public <T> T provide(Class<T> brick) {
		if (PrevalenceFlag.class.isAssignableFrom(brick)) return (T)FLAG;
		
		if (!isPrevalent(brick))
			return _environment.provide(brick); //Could be a test environment.

		_environment.provideWithoutBlocking(brick); //This avoids deadlock in the case some other thread is already providing this brick and waiting for transaction log replay.
		return building().waitForInstance(brick);
	}

	
	<T> T provide(Class<T> prevalentBrick, Producer<T> instantiator) {
		T result = building().get(prevalentBrick, instantiator);
		if (!my(PrevalenceFlag.class).isInsidePrevalence())
			PrevaylerHolder.waitForTransactionLogReplayIfNecessary();
		return result;
	}
	
	
	private boolean isPrevalent(Class<?> brick) {
		for (Class<? extends Nature> nature : brick.getAnnotation(Brick.class).value())
			if (nature == Prevalent.class) return true;
			
		return false;
	}
	
}