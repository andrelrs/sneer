package sneer.bricks.software.bricks.interception.tests.fixtures.staticinitializer;

import sneer.bricks.software.bricks.interception.tests.fixtures.nature.SomeInterceptingNature;
import sneer.foundation.brickness.Brick;

@Brick(SomeInterceptingNature.class)
public interface StaticInitializer {

	void foo();

}
