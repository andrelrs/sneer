package sneer.bricks.software.bricks.compiler.impl;

import java.io.File;
import java.io.IOException;

import sneer.bricks.software.bricks.compiler.BrickCompilerException;
import sneer.bricks.software.bricks.compiler.Builder;

class BuilderImpl implements Builder {

	@Override
	public void build(File srcFolder, File destFolder) throws IOException, BrickCompilerException {
		new Build(srcFolder, destFolder);
	}
	
}
