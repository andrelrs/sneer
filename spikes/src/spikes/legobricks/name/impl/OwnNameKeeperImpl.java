package spikes.legobricks.name.impl;

import spikes.legobricks.name.OwnNameKeeper;
import wheel.lang.Omnivore;
import wheel.reactive.Signal;
import wheel.reactive.Register;
import wheel.reactive.impl.SourceImpl;

public class OwnNameKeeperImpl implements OwnNameKeeper {

	private Register<String> _name = new SourceImpl<String>(null);
	
	@Override
	public Signal<String> name() {
		return _name.output();
	}

	@Override
	public Omnivore<String> nameSetter() {
		return _name.setter();
	}

}
