package sneer.installer;

import static sneer.main.SneerCodeFolders.BIN;
import static sneer.main.SneerCodeFolders.CODE;
import static sneer.main.SneerCodeFolders.SNEER_HOME;
import static sneer.main.SneerCodeFolders.SRC;
import static sneer.main.SneerFolders.LOG_FILE;
import static sneer.main.SneerFolders.OWN_CODE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

class Installation {

	private final URL _sneerJar		= this.getClass().getResource("/sneer.jar");
	private final URL _ownJar		= this.getClass().getResource("/own.jar");

	private JWindow _splashScreen;

	Installation() throws Exception {
		showSplashScreen();
		resetDirectories();
		updateCode();
		createOwnProjectIfNecessary();
		closeSplashScreen();
	}

	private void showSplashScreen() {
		_splashScreen = new JWindow();
		Image image = Toolkit.getDefaultToolkit().createImage(Installation.class.getResource("dogfood.png"));
		ImageIcon icon = new ImageIcon(image);
		_splashScreen.setLayout(new BorderLayout());
		_splashScreen.add(new JLabel(icon), BorderLayout.CENTER);

		int imgWidth = 600;
		int imgHeight = 300;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
		Point basePoint = new Point(
			(int) ((screenSize.getWidth() - imgWidth) / 2), 
			(int) ((screenSize.getHeight() - imgHeight) / 2)
		);

		_splashScreen.setBounds(basePoint.x, basePoint.y, imgWidth, imgHeight);
		_splashScreen.setVisible(true);
	}

	private void resetDirectories() throws IOException {
		if(!SNEER_HOME.exists())
			SNEER_HOME.mkdirs();

		deleteFolder(SRC);
		deleteFolder(BIN);
		SRC.mkdirs();
		BIN.mkdirs();
	}

	private void deleteFolder(File folder) throws IOException {
        if (!folder.exists()) return;

        for (File file : folder.listFiles())
        	recursiveDelete(file);

        if (!folder.delete()) throw new IOException(("Unable to delete folder " + folder + "."));
    }

    private void recursiveDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteFolder(file);
            return;
        }

        if (!file.delete())  throw new IOException(("Unable to delete file: " + file));
    }

	private void updateCode() throws IOException {
		extractFiles(extractJar(_sneerJar, "sneer"), CODE);
	}

	private File extractJar(URL url, String prefix) throws IOException {
		File file =  File.createTempFile(prefix, "jar");
		file.deleteOnExit();

		InputStream input = url.openStream();
		IOUtils.copyToFile(input, file);
		input.close();
		return file;
	}

	private void extractFiles(File src, File toDir) throws IOException {
		if(!(src.exists()))
			throw new IOException("File '" + src.getAbsolutePath() + "' not found!");	

		FileInputStream inputStream = new FileInputStream(src);
		extractFiles(src, toDir, inputStream);
		inputStream.close();
	}

	private void extractFiles(File src, File toDir, FileInputStream inputStream) throws IOException {
		JarInputStream jis = new JarInputStream(inputStream);
		JarFile jar = new JarFile(src);
		JarEntry entry = null;

        while ((entry = jis.getNextJarEntry()) != null) {
        	File file = new File(toDir, entry.getName());

        	if(entry.isDirectory()) {
        		file.mkdirs();
				continue;
        	}
        	IOUtils.writeEntry(jar, entry, file);
        }
	}

	private void createOwnProjectIfNecessary() throws IOException {
		if(OWN_CODE.exists()) return;

		IOUtils.write(LOG_FILE, "jar file url: " + _ownJar.toString());
		File file = extractJar(_ownJar, "own");
		extractFiles(file, OWN_CODE.getParentFile());		
	}

	private void closeSplashScreen() {
		_splashScreen.setVisible(false);
		_splashScreen.dispose();
	}

	public static void main(String[] args) throws Exception {
		new Installation();
	}

}