package sneer.bricks.software.code.classutils;

import java.io.File;

import sneer.foundation.brickness.Brick;

@Brick
public interface ClassUtils {

	File classFile(Class<?> clazz);
	File classpathRootFor(Class<?> clazz);

	String relativeClassFileName(Class<?> clazz);
	String relativeJavaFileName(Class<?> clazz);

}
