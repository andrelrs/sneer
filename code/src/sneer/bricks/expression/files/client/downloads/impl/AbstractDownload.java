package sneer.bricks.expression.files.client.downloads.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;

import sneer.bricks.expression.files.client.downloads.Download;
import sneer.bricks.expression.files.client.downloads.TimeoutException;
import sneer.bricks.expression.files.map.FileMap;
import sneer.bricks.expression.tuples.Tuple;
import sneer.bricks.expression.tuples.TupleSpace;
import sneer.bricks.hardware.clock.Clock;
import sneer.bricks.hardware.clock.timer.Timer;
import sneer.bricks.hardware.cpu.crypto.Hash;
import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.hardware.cpu.threads.latches.Latch;
import sneer.bricks.hardware.cpu.threads.latches.Latches;
import sneer.bricks.hardware.io.files.atomic.dotpart.DotParts;
import sneer.bricks.hardware.io.log.Logger;
import sneer.bricks.identity.seals.Seal;
import sneer.bricks.pulp.blinkinglights.BlinkingLights;
import sneer.bricks.pulp.blinkinglights.LightType;
import sneer.bricks.pulp.reactive.Register;
import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.pulp.reactive.Signals;
import sneer.foundation.lang.Closure;

abstract class AbstractDownload implements Download {

	private static int ACTIVITY_TIMEOUT = 30 * 1000;
	private static int DURATION_TIMEOUT = 30 * 60 * 1000;

	static final int REQUEST_INTERVAL = 15 * 1000;
	
	private static final String DOT_PART = my(DotParts.class).dotPartExtention();
	

	protected File _path;
	protected final File _actualPath;
	final long _lastModified;
	final Hash _hash;

	private final Seal _source;


	private long _startTime;
	private long _lastActivityTime;

	private final Register<Integer> _progress = my(Signals.class).newRegister(0);

	private final Latch _isFinished = my(Latches.class).produce();
	private Register<Boolean> _finished = my(Signals.class).newRegister(false);

	private Exception _exception;

	private final Runnable _toCallWhenFinished;

	private WeakContract _timerContract;


	AbstractDownload(File path, long lastModified, Hash hashOfFile, Seal source, Runnable toCallWhenFinished) {
		_actualPath = path;

		_path = dotPartFor(path);
		_lastModified = lastModified;
		_hash = hashOfFile;

		_source = source; 

		_toCallWhenFinished = toCallWhenFinished;

		my(Logger.class).log("Downloading: {} Hash:", _actualPath, _hash);

		finishIfLocallyAvailable();
	}

	
	abstract protected void subscribeToContents();
	abstract protected Tuple requestToPublishIfNecessary();
	abstract protected boolean isWaitingForActivity();

	abstract protected Object mappedContentsBy(Hash hashOfContents);
	abstract protected void finishWithLocalContents(Object contents) throws IOException, TimeoutException;
	
	abstract protected void updateFileMap();


	void start() {
		if (isFinished()) return;

		subscribeToContents();
		_startTime = my(Clock.class).time().currentValue();
		_lastActivityTime = _startTime;
		startSendingRequests();
	}


	@Override	public File file() {	return _actualPath; }
	@Override	public Hash hash() { return _hash; }
	@Override	public Seal source() {	return _source; }
	@Override	public Signal<Integer> progress() { return _progress.output(); }


	protected void setProgress(float newValue) {
		_progress.setter().consume((int) (100 * newValue));
	}


	@Override
	public void waitTillFinished() throws IOException, TimeoutException {
		_isFinished.waitTillOpen();
		if (_exception == null) return;
		if (_exception instanceof IOException) throw (IOException) _exception;
		if (_exception instanceof TimeoutException) throw (TimeoutException) _exception;
		throw new IllegalStateException("Unexpected exception type: " + _exception.getClass(), _exception);
	}


	@Override
	public void dispose() {
		finishWith(new IOException("Download disposed: " + _actualPath));
	}


	@Override
	public Signal<Boolean> finished() {
		return _finished.output();
	}


	@Override
	public boolean hasFinishedSuccessfully() {
		if (!isFinished()) return false;
		return _exception == null;
	}


	private File dotPartFor(File path) {
		try {
			return my(DotParts.class).openDotPartFor(path);
		} catch (IOException e) {
			finishWith(e);
			return null;
		}
	}


	private void publishRequestIfNecessary() {
		if (isFinished()) return;
		Tuple request = requestToPublishIfNecessary();
		if (request == null) return;
		publish(request);
	}


	protected void publish(Tuple request) {
		my(TupleSpace.class).acquire(request);
	}


	boolean isFinished() {
		return _isFinished.isOpen();
	}


	void finishWith(Exception e) {
		_exception = e;
		my(Logger.class).log("Download failed with: {} message: {}", _exception.getClass(), _exception.getMessage());
		finish();
	}


	void finishWithSuccess() throws IOException {
		my(DotParts.class).closeDotPart(_path, _lastModified);
		updateFileMap();
		finish();
		my(BlinkingLights.class).turnOn(LightType.GOOD_NEWS, _actualPath.getName() + " downloaded!", _actualPath.getAbsolutePath(), 10000);
	}


	void finish() {
		stopSendingRequests();
		_isFinished.open();
		_finished.setter().consume(true);
		if (_toCallWhenFinished != null) _toCallWhenFinished.run();
	}


	private void stopSendingRequests() {
		if (_timerContract == null) return;
		_timerContract.dispose();
	}


	void startSendingRequests() {
		_timerContract = my(Timer.class).wakeUpNowAndEvery(REQUEST_INTERVAL, new Closure() { @Override public void run() {
			checkForActivityTimeOut();
			checkForDurationTimeOut();
			publishRequestIfNecessary();
		}});
	}


	private void finishIfLocallyAvailable() {
		String mappedPath = my(FileMap.class).getPath(_hash);
		if (mappedPath == null) return;
		if (mappedPath.contains(DOT_PART)) return; //Optimize Downloads that include identical files in different folders will download all of them redundantly. The problem is .part files can be renamed to their actual name at any moment. 

		Object mappedContents = mappedContentsBy(_hash);
		try {
			finishWithLocalContents(mappedContents);
		} catch (Exception e) {
			finishWith(e);
		}
	}


	protected void recordActivity() {
		_lastActivityTime = my(Clock.class).time().currentValue();
	}


	private void checkForDurationTimeOut() {
		Long currentTime = my(Clock.class).time().currentValue();
		if (currentTime - _startTime > DURATION_TIMEOUT) timeout("Duration");
	}

	
	private void checkForActivityTimeOut() {
		if(!isWaitingForActivity())
			return;
		Long currentTime = my(Clock.class).time().currentValue();
		if (currentTime - _lastActivityTime > ACTIVITY_TIMEOUT) timeout("Activity");
	}

	
	private void timeout(String timeoutCase) {
		finishWith(new TimeoutException(timeoutCase + " Timeout downloading " + _actualPath.getAbsolutePath()));
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_hash == null) ? 0 : _hash.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AbstractDownload other = (AbstractDownload) obj;
		if (_hash == null) {
			if (other._hash != null)
				return false;
		} else if (!_hash.equals(other._hash))
			return false;
		return true;
	}


	@Override
	protected void finalize() throws Throwable {
		my(Logger.class).log("Download garbage collected: " + _path);
		this.dispose();
	}

}
