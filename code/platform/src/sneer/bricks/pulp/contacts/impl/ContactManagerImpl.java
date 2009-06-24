package sneer.bricks.pulp.contacts.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.pulp.contacts.Contact;
import sneer.bricks.pulp.contacts.ContactManager;
import sneer.bricks.pulp.reactive.collections.CollectionSignals;
import sneer.bricks.pulp.reactive.collections.ListRegister;
import sneer.bricks.pulp.reactive.collections.ListSignal;
import sneer.bricks.snapps.contacts.stored.ContactStore;
import sneer.foundation.lang.PickyConsumer;
import sneer.foundation.lang.exceptions.NotImplementedYet;
import sneer.foundation.lang.exceptions.Refusal;

class ContactManagerImpl implements ContactManager {
    
    final ListRegister<Contact> _contacts = my(CollectionSignals.class).newListRegister();
    
    ContactManagerImpl(){
		restoreContacts();
    }

	private void restoreContacts() {
		try {
			for (String nick : my(ContactStore.class).getRestoredNicks())
				addContact(nick);
		} catch (Refusal e) {
			throw new NotImplementedYet(e); // Fix Handle this exception.
		}
	}
    
	@Override
	synchronized public Contact addContact(String nickname) throws Refusal {
		nickname.toString();
		
		checkAvailability(nickname);
		
		Contact result = doAddContact(nickname);

		my(ContactStore.class).save();
		
		return result;
	}

	private Contact doAddContact(String nickname) {
		Contact result = new ContactImpl(nickname); 
		_contacts.add(result);
		return result;
	}

	private void checkAvailability(String nickname) throws Refusal {
		if (isNicknameAlreadyUsed(nickname))
			throw new Refusal("Nickname " + nickname + " is already being used.");
	}

	@Override
	public ListSignal<Contact> contacts() {
		return _contacts.output();
	}

	@Override
	synchronized public boolean isNicknameAlreadyUsed(String nickname) {
		return contactGiven(nickname) != null;
	}

	@Override
	synchronized public Contact contactGiven(String nickname) {
		for (Contact candidate : contacts())
			if (candidate.nickname().currentValue().equals(nickname))
				return candidate;

		return null;
	}

	synchronized private void changeNickname(Contact contact, String newNickname) throws Refusal {
		checkAvailability(newNickname);
		((ContactImpl)contact).nickname(newNickname);
		my(ContactStore.class).save();
	}

	@Override
	public void removeContact(Contact contact) {
		_contacts.remove(contact);
		my(ContactStore.class).save();
	}
	
	@Override
	public PickyConsumer<String> nicknameSetterFor(final Contact contact) {
		return new PickyConsumer<String>(){ @Override public void consume(String newNickname) throws Refusal {
			changeNickname(contact, newNickname);
		}};
	}

	@Override
	public Contact produceContact(String nickname) {
		if (contactGiven(nickname) == null) doAddContact(nickname);
		return contactGiven(nickname);
	}
}