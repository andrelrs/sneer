package wheel.io.ui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

import wheel.lang.Timebox;

public class TimeboxedEventQueue extends EventQueue {

	private static TimeboxedEventQueue _singleton;

	public static void startQueueing(int timeboxDuration) {
		if (_singleton != null) throw new IllegalStateException();
		_singleton = new TimeboxedEventQueue(timeboxDuration);
		
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(_singleton);
	}
	public static void stopQueueing() {
		_singleton.pop();
	}


	private TimeboxedEventQueue(int timeboxDuration) {
		_timebox = new QueueTimebox(timeboxDuration);
	}

	private final QueueTimebox _timebox;
	

	
	@Override
	protected void dispatchEvent(final AWTEvent event) {
		_timebox.setEventAndRun(event);
	}
	
	
	private final class QueueTimebox extends Timebox {
		
		private QueueTimebox(int timeboxDuration) {
			super(timeboxDuration, false);
		}

		AWTEvent _event;

		@Override 
		protected void runInTimebox() {
			TimeboxedEventQueue.super.dispatchEvent(_event);
		}
		
		private void setEventAndRun(AWTEvent event){
			_event = event;
			this.run();
		}
	}
}