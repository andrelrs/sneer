package sneer.foundation.brickness.impl;

interface BrickClassLoader {
	
	public enum Kind {
		IMPL,
		LIBS
	}

	String brickName();

	Kind kind();

}
