package spikes.sneer.bricks.pulp.own.tagline.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.Test;

import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;
import spikes.sneer.bricks.pulp.own.tagline.OwnTaglineKeeper;

public class OwnTaglineKeeperTest extends BrickTestBase {

	private final OwnTaglineKeeper _taglineKeeper = my(OwnTaglineKeeper.class);
	
	@Test
	public void test() throws Exception {

		_taglineKeeper.taglineSetter().consume("Zubs1");
		assertEquals("Zubs1", _taglineKeeper.tagline().currentValue());
		
		_taglineKeeper.taglineSetter().consume("Zubs2");
		assertEquals("Zubs2", _taglineKeeper.tagline().currentValue());
		
	}
}
