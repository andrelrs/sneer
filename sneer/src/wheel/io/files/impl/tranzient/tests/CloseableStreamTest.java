package wheel.io.files.impl.tranzient.tests;

import java.io.IOException;

import wheel.io.files.impl.Closeable;
import wheel.io.files.impl.Closeable.Listener;
import junit.framework.TestCase;

public abstract class CloseableStreamTest extends TestCase {

	private Closeable _subject;
	private Closeable _lastClosedStream;

	@Override
	protected void setUp() throws Exception {
		_subject = createSubject();
	}

	protected abstract Closeable createSubject();

	public void testClose() throws IOException {
		_subject.notifyOnClose(new Listener(){
			public void streamClosed(Closeable stream) {
				_lastClosedStream = stream;
			}
		});
		_subject.close();
		assertSame(_subject, _lastClosedStream);
	}

}
