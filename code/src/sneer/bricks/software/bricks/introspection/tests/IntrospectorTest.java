package sneer.bricks.software.bricks.introspection.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.Test;

import sneer.bricks.software.bricks.introspection.Introspector;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;

public class IntrospectorTest extends BrickTestBase {
	
	@Test
	public void brickInterfaceFor() {
		final Introspector introspector = my(Introspector.class);
		assertSame(
				Introspector.class,
				introspector.brickInterfaceFor(introspector));
	}

}
