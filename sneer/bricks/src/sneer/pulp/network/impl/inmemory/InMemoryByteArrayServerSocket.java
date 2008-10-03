package sneer.pulp.network.impl.inmemory;

import java.io.IOException;

import sneer.pulp.network.ByteArrayServerSocket;
import sneer.pulp.network.ByteArraySocket;
import wheel.lang.Threads;


public class InMemoryByteArrayServerSocket implements ByteArrayServerSocket {

	private ByteArraySocket _clientSide;

	public synchronized ByteArraySocket accept() throws IOException {
		
		if (_clientSide != null) throw new IOException("Port already in use.");
		InMemoryByteArraySocket result = new InMemoryByteArraySocket();
		_clientSide = result.counterpart();
		
		notifyAll(); //Notifies all client threads.
		Threads.waitWithoutInterruptions(this);

		return result;
	}

	synchronized ByteArraySocket openClientSocket() {
		while (_clientSide == null) Threads.waitWithoutInterruptions(this);

		ByteArraySocket result = _clientSide;
        _clientSide = null;
        notifyAll(); //Notifies the server thread (necessary) and eventual client threads (harmless).
        return result;
	}

	@Override
	public void crash() {
		throw new wheel.lang.exceptions.NotImplementedYet();
	}
}