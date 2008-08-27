package sneer.pulp.contacts;

import sneer.kernel.container.Brick;
import wheel.lang.exceptions.IllegalParameter;
import wheel.reactive.lists.ListRegister;
import wheel.reactive.lists.ListSignal;


public interface ContactManager extends Brick {
    
	ListSignal<Contact> contacts();

	boolean isNicknameAlreadyUsed(String nickname);
	
	Contact contactGiven(String nickname);

	/** @throws IllegalParameter if there already is a Contact with that nickname.*/
	Contact addContact(String nickname) throws IllegalParameter;

	/** @throws IllegalParameter if there already is a Contact with newNickname.*/
	void changeNickname(Contact contact, String newNickname) throws IllegalParameter;
	
	/** @throws IllegalParameter if nickname not used.*/
	void removeContact(String nickname) throws IllegalParameter;

	ListRegister<Contact> getContacts();
	

}