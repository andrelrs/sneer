package sneer.bricks.pulp.reactive.counters.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.pulp.reactive.Register;
import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.pulp.reactive.Signals;
import sneer.bricks.pulp.reactive.counters.Counter;
import sneer.foundation.lang.Closure;

class CounterImpl implements Counter {

	private final Register<Integer> _countRegister;

	public CounterImpl(int initialValue) {
		_countRegister = my(Signals.class).newRegister(initialValue);
	}

	@Override
	synchronized
	public Signal<Integer> count() {
		return _countRegister.output();
	}

	@Override
	public Closure incrementer() {
		return conditionalIncrementer(true);
	}

	@Override
	public Closure conditionalIncrementer(final boolean condition) {
		return new Closure() { @Override public void run() {
			if (condition) increment();
		}};
	}

	synchronized
	private void increment() {
		_countRegister.setter().consume(count().currentValue() + 1);
	}

	@Override
	public Closure decrementer() {
		return conditionalDecrementer(true);
	}

	@Override
	public Closure conditionalDecrementer(final boolean condition) {
		return new Closure() { @Override public void run() {
			if (condition) decrement();
		}};
	}

	synchronized
	private void decrement() {
		_countRegister.setter().consume(count().currentValue() - 1);
	}

}
