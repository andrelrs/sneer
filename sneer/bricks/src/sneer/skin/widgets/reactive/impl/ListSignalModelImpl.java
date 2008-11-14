package sneer.skin.widgets.reactive.impl;

import javax.swing.AbstractListModel;

import sneer.kernel.container.Inject;
import sneer.pulp.reactive.signalchooser.SignalChooser;
import sneer.pulp.reactive.signalchooser.SignalChooserManager;
import sneer.pulp.reactive.signalchooser.SignalChooserManagerFactory;
import sneer.skin.widgets.reactive.ListSignalModel;
import wheel.io.ui.GuiThread;
import wheel.lang.Omnivore;
import wheel.reactive.Signal;
import wheel.reactive.lists.ListSignal;
import wheel.reactive.lists.impl.VisitingListReceiver;

public class ListSignalModelImpl<T> extends AbstractListModel implements ListSignalModel<T>{

	@Inject
	private static SignalChooserManagerFactory _signalChooserManagerFactory;
	
	private final ListSignal<T> _input;
	
	@SuppressWarnings("unused")
	private ModelChangeReceiver _modelChangeReceiverToAvoidGc;

	@SuppressWarnings("unused")
	private SignalChooserManager<T> _signalChooserManagerToAvoidGc;
	
	ListSignalModelImpl(ListSignal<T> input, SignalChooser<T> chooser) {
		_input = input;
		_modelChangeReceiverToAvoidGc = new ModelChangeReceiver(_input);
		_signalChooserManagerToAvoidGc = _signalChooserManagerFactory.newManager(input, chooser, 
			new Omnivore<T>(){ @Override public void consume(T element) {
				elementChanged(element);
			}});
	}

	private class ModelChangeReceiver extends VisitingListReceiver<T> {

		private ModelChangeReceiver(ListSignal<T> input) {
			super(input);
		}

		@Override
		public void elementAdded(final int index, T value) {
			GuiThread.invokeAndWait(new Runnable(){ @Override public void run() {
				fireIntervalAdded(ListSignalModelImpl.this, index, index);
			}});		
		}

		@Override
		public void elementRemoved(final int index, T value) {
			GuiThread.invokeAndWait(new Runnable(){ @Override public void run() {
				fireIntervalRemoved(ListSignalModelImpl.this, index, index);
			}});		
		}

		@Override
		public void elementReplaced(final int index, T oldValue, T newValue) {
			contentsChanged(index);
		}

		@Override
		public void elementInserted(final int index, final T value) {
			GuiThread.invokeAndWait(new Runnable(){ @Override public void run() {
				fireIntervalAdded(ListSignalModelImpl.this, index, index);
			}});		
		}

		@Override
		public void elementMoved(final int oldIndex, final int newIndex, T element) {
			GuiThread.invokeAndWait(new Runnable(){ @Override public void run() {
				fireContentsChanged(ListSignalModelImpl.this, oldIndex, newIndex);
			}});
		}
	}
	
	public int getSize() {
		Signal<Integer> size = _input.size();
		return size.currentValue();
	}
	
	public T getElementAt(int index) {
		return _input.currentGet(index);
	}

	private void contentsChanged(final int index) {
		GuiThread.invokeAndWait(new Runnable(){ @Override public void run() {
			fireContentsChanged(ListSignalModelImpl.this, index, index);
		}});
	}
	
	public void elementChanged(T element) {
		if(element==null) return;
		int i = 0;
		for (T candidate : _input) {  //Optimize
			if (candidate == element)
				contentsChanged(i);
			i++;
		}
	}	
	private static final long serialVersionUID = 1L;
}