package sneer.bricks.softwaresharing.demolisher.impl;


import java.io.IOException;

import sneer.bricks.hardware.cpu.crypto.Hash;
import sneer.bricks.softwaresharing.BrickHistory;
import sneer.bricks.softwaresharing.demolisher.Demolisher;
import sneer.foundation.lang.CacheMap;

class DemolisherImpl implements Demolisher {

	@Override
	public void demolishBuildingInto(CacheMap<String,BrickHistory> bricksByName, Hash srcFolderHash, boolean isCurrent) throws IOException {
		new Demolition(bricksByName, srcFolderHash, isCurrent);
	}

}
