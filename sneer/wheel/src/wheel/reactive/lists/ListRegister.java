package wheel.reactive.lists;

import wheel.lang.Omnivore;
import wheel.reactive.CollectionRegisterBase;

public interface ListRegister<T> extends CollectionRegisterBase {

	ListSignal<T> output();

	void add(T element);
	Omnivore<T> adder();

	void remove(T element);
	void removeAt(int index);
	
	void replace(int index, T newElement);
	
}