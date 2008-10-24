package snapps.wind.impl;

import snapps.wind.Shout;
import snapps.wind.Wind;
import sneer.kernel.container.Inject;
import sneer.pulp.tuples.TupleSpace;
import wheel.lang.Omnivore;
import wheel.reactive.lists.ListRegister;
import wheel.reactive.lists.ListSignal;
import wheel.reactive.lists.impl.ListRegisterImpl;

class WindImpl implements Wind, Omnivore<Shout> {

	@Inject
	static private TupleSpace _environment;
	
	private final ListRegister<Shout> _shoutsHeard = new ListRegisterImpl<Shout>();

	{
		_environment.addSubscription(Shout.class, this);
	}

	@Override
	public ListSignal<Shout> shoutsHeard() {
		return _shoutsHeard.output();
	}

	@Override
	public void consume(Shout shout) {
		_shoutsHeard.adder().consume(shout);
	}


	@Override
	public Omnivore<String> megaphone() {
		return new Omnivore<String>(){ @Override public void consume(String phrase) {
			shout(phrase);
		}};
	}

	private void shout(String phrase) {
		_environment.publish(new Shout(phrase));
	}

}
