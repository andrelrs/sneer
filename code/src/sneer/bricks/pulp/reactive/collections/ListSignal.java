package sneer.bricks.pulp.reactive.collections;

import java.util.List;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.foundation.lang.Consumer;

public interface ListSignal<T> extends CollectionSignal<T> {
	
	T currentGet(int index);
	int currentIndexOf(T element);
	@Override
	List<T> currentElements();
	
	WeakContract addListReceiver(Consumer<? super ListChange<T>> receiver);
	/**Same as addListReceiver() but implemented as a visitor for convenience. */
	WeakContract addListReceiverAsVisitor(ListChange.Visitor<T> visitor);
}