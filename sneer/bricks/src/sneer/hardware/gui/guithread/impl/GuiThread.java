package sneer.hardware.gui.guithread.impl;

import java.lang.reflect.InvocationTargetException;
import static sneer.commons.environments.Environments.my;


import javax.swing.SwingUtilities;

import sneer.commons.environments.Environment;
import sneer.commons.environments.Environments;
import sneer.hardware.gui.guithread.GuiThread;

class GuiThreadImpl implements GuiThread {
	
	@Override
	public void invokeAndWait(final Runnable runnable) { //Fix This method is called sometimes from swing's thread and other times from aplication's thread. Split the caller method (if it is possible), and delete this method.
		if(SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			strictInvokeAndWait(runnable);
	}
	
	@Override
	public void strictInvokeAndWait(final Runnable runnable) { //Fix Calling this from brick code is no longer necessary after the container is calling gui brick code only in the Swing thread.
		assertNotInGuiThread();
		try {
			SwingUtilities.invokeAndWait(envolve(runnable));
		} catch (InterruptedException e) {
			throw new sneer.commons.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		} catch (InvocationTargetException e) {
			throw new sneer.commons.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		}
	}

	@Override
	public void strictInvokeLater(Runnable runnable) { //Fix Calling this from brick code is no longer necessary after the container is calling gui brick code only in the Swing thread.
		assertNotInGuiThread();
		SwingUtilities.invokeLater(envolve(runnable));
	}

	@Override
	public void assertInGuiThread() {
		if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Should be running in the GUI thread."); 
	}

	private void assertNotInGuiThread() {
		if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Should NOT be running in the GUI thread."); 
	}

	@Override
	public void invokeLater(Runnable runnable) {
		SwingUtilities.invokeLater(envolve(runnable));
	}

	private Runnable envolve(final Runnable delegate) {
		final Environment environment = my(Environment.class);
		return new Runnable() { @Override public void run() {
			Environments.runWith(environment, delegate);
		}};
	}
}