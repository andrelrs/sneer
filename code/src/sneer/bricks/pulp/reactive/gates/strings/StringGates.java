package sneer.bricks.pulp.reactive.gates.strings;

import sneer.bricks.pulp.reactive.Signal;
import sneer.foundation.brickness.Brick;

@Brick
public interface StringGates {

	Signal<String> concat(Signal<?>... objects);

	Signal<String> concat(String separator, Signal<?>... objects);

}
