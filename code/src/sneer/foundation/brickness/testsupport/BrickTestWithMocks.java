package sneer.foundation.brickness.testsupport;

import static sneer.foundation.environments.Environments.my;

import java.lang.reflect.Method;

import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.internal.ExpectationBuilder;
import org.junit.After;
import org.junit.runner.RunWith;

import sneer.foundation.environments.Environment;
import sneer.foundation.testsupport.CleanTestBase;

@RunWith(BrickTestWithMockRunner.class)
public abstract class BrickTestWithMocks extends CleanTestBase {

	private final Mockery _mockery = new JUnit4Mockery();

	
	{
		my(BrickTestRunner.class).instanceBeingInitialized(this);
	}

	
	protected BrickTestWithMocks() {
		super();
	}

	
	protected Sequence newSequence(String name) {
		return _mockery.sequence(name);
	}

	
	protected <T> T mock(Class<T> type) {
		return _mockery.mock(type);
	}

	
	protected <T> T mock(String name, Class<T> type) {
		return _mockery.mock(type, name);
	}

	
	protected void checking(ExpectationBuilder expectations) {
		_mockery.checking(expectations);
	}

	
	protected Environment newTestEnvironment(Object... bindings) {
		return my(BrickTestRunner.class).cloneTestEnvironment(bindings);
	}

	
	@After
	public void afterBrickTest() {
		my(BrickTestRunner.class).dispose();
	}

	
	@Override
	protected void afterFailedtest(Method method, Throwable thrown) {}

}