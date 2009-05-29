package sneer.brickness.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sneer.brickness.Nature;

/** To be implemented.*/
class ClassLoaderForBrickLibs extends ClassLoaderWithNatures {

	private static final URL[] ARRAY_OF_URL = new URL[0];

	ClassLoaderForBrickLibs(File classpath, String implPackage, List<Nature> natures, ClassLoader next) {
		super(jars(classpath, implPackage), next, natures);
	}

	private static URL[] jars(File classpath, String implPackage) {
		List<URL> result = new ArrayList<URL>();
		
		File libDir = new File(classpath, implPackage.replace(".", "/") + "/lib");

		for (File candidate : libDir.listFiles())
			if (candidate.getName().endsWith(".jar"))
				result.add(toURL(candidate));
		
		sortAlphabetically(result);

		return result.toArray(ARRAY_OF_URL);
	}

	private static void sortAlphabetically(List<URL> list) {
		Collections.sort(list, new Comparator<URL>() { @Override public int compare(URL url1, URL url2) {
			return url1.getPath().compareTo(url2.getPath());
		}});
	}

	private static URL toURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected boolean isEagerToLoad(String className) {
		return findResource(className.replace(".", "/") + ".class") != null; //OPTIMIZE: Cache this and use it to load the class.
	}	


}