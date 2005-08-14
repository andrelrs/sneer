package sneer.life;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class JpgImage implements Serializable {

	private final byte[] _jpegFileBytes;

	public JpgImage(String path) throws IOException {
		InputStream file = new FileInputStream(path);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		byte[] block = new byte[4096 * 10];
		while (true) {
			int bytesRead = file.read(block);
			if (bytesRead == -1) break;
			bytes.write(block, 0, bytesRead);
		}
		
		_jpegFileBytes = bytes.toByteArray();
	}

	public InputStream jpegFileContents() {
		return new ByteArrayInputStream(_jpegFileBytes);
	}

	private static final long serialVersionUID = 1L;

}
