package sneer.bricks.hardware.io.log.notifier.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.hardware.io.log.filter.LogFilter;
import sneer.bricks.hardware.io.log.formatter.LogFormatter;
import sneer.bricks.hardware.io.log.notifier.LogNotifier;
import sneer.bricks.hardware.io.log.worker.LogWorker;
import sneer.bricks.hardware.io.log.worker.LogWorkerHolder;
import sneer.bricks.pulp.events.EventNotifier;
import sneer.bricks.pulp.events.EventNotifiers;
import sneer.bricks.pulp.events.EventSource;

public class LogNotifierImpl implements LogNotifier {

	private final LogFilter _filter = my(LogFilter.class);
	private final LogFormatter _formatter = my(LogFormatter.class);
	private final EventNotifier<String> _loggedMessages = my(EventNotifiers.class).newInstance();
	
	{
		my(LogWorkerHolder.class).setWorker(new LogWorker() { @Override public void log(String message, Object... messageInsets) {
			notifyEntry(_formatter.format(message, messageInsets));
		}});
	}
	
	private void notifyEntry(String msg){
		if(_filter.acceptLogEntry(msg))
			_loggedMessages.notifyReceivers(msg);
	}

	@Override
	public EventSource<String> loggedMessages() {
		return _loggedMessages.output();
	}
}
