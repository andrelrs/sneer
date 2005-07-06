package sneer.remote;

import java.io.IOException;

import org.prevayler.foundation.Cool;
import org.prevayler.foundation.network.ObjectSocket;

class ParallelSocket {

	private final ObjectSocket _delegate;
	private final Object _writeMonitor = new Object();
	private final Object _readMonitor = new Object();

	private int _stamp = 1;
	private Envelope _envelopeRead = null;
	
	private String _reasonForClosing = null;

	ParallelSocket(ObjectSocket delegate) {
		_delegate = delegate;
	}

	public Object getReply(Object request) throws Exception {
		Envelope myEnvelope;
		synchronized (_writeMonitor) {
			myEnvelope = new Envelope(request, _stamp++);
			_delegate.writeObject(myEnvelope);
		}
		synchronized (_readMonitor) {
			try {
				while (true) {
					checkOpen();
					try {
						if (_envelopeRead == null) _envelopeRead = (Envelope)_delegate.readObject();
					} catch (Exception x) {
						closeBecauseOf(x);
						throw x;
					}
					if (_envelopeRead.stamp() == myEnvelope.stamp()) {
						Object result = _envelopeRead.contents();
						_envelopeRead = null;
						return result;
					}
					Cool.wait(_readMonitor);
				}
			} finally {
				_readMonitor.notifyAll();
			}
		}
	}

	private void checkOpen() throws IOException {
		if (_reasonForClosing != null) throw new IOException("Socket closed due to " + _reasonForClosing);
	}

	private void closeBecauseOf(Exception exception) {
		try {
			_delegate.close();
		} catch (IOException ignored) {
			//Exceptional socket state is already being treated.
		}
		_reasonForClosing = exception.getClass().toString() + " - " + exception.getMessage();
	}

}
