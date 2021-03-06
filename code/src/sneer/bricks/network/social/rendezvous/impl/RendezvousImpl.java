package sneer.bricks.network.social.rendezvous.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.hardware.gui.actions.Action;
import sneer.bricks.identity.name.OwnName;
import sneer.bricks.identity.seals.contacts.ContactSeals;
import sneer.bricks.network.computers.sockets.connections.Call;
import sneer.bricks.network.computers.sockets.connections.ConnectionManager;
import sneer.bricks.network.social.Contacts;
import sneer.bricks.network.social.attributes.Attributes;
import sneer.bricks.network.social.rendezvous.Rendezvous;
import sneer.bricks.pulp.blinkinglights.BlinkingLights;
import sneer.bricks.pulp.blinkinglights.Light;
import sneer.bricks.pulp.blinkinglights.LightType;
import sneer.bricks.pulp.reactive.Signal;
import sneer.foundation.lang.Consumer;
import sneer.foundation.lang.exceptions.Refusal;

class RendezvousImpl implements Rendezvous {
	@SuppressWarnings("unused")	private WeakContract _refToAvoidGc;

	{
		_refToAvoidGc = my(ConnectionManager.class).unknownCallers().addReceiver(new Consumer<Call>() { @Override public void consume(Call call) {
			receiveCallFromUnknownCaller(call);
		}});
	}

	private void receiveCallFromUnknownCaller(final Call call) {
		Signal<String> ownName = my(Attributes.class).myAttributeValue(OwnName.class);
		if (ownName.equals("Klaus Wuestefeld") || ownName.equals("Igor Arouca")) {
			acceptCall(call);
			return;
		}
		
		final Light light = my(BlinkingLights.class).prepare(LightType.GOOD_NEWS);
		light.addAction(new Action() {
			@Override public String caption() {return "Accept";}
			@Override public void run() {acceptCall(call); my(BlinkingLights.class).turnOffIfNecessary(light);}
		});
		light.addAction(new Action() {
			@Override public String caption() {return "Reject";}
			@Override public void run() {my(BlinkingLights.class).turnOffIfNecessary(light);}
		});
		my(BlinkingLights.class).turnOnIfNecessary(light, getMessage(call), getHelpMessage());
	}

	protected void acceptCall(Call call) {
		my(Contacts.class).produceContact(call.callerName());
		try {
			my(ContactSeals.class).put(call.callerName(), call.callerSeal());
		} catch (Refusal e) {
			Light light = my(BlinkingLights.class).prepare(LightType.ERROR);
			my(BlinkingLights.class).turnOnIfNecessary(light, e); //Refactor: create BlinkingLights.turnOn(LightType, FriendlyException)
		}
	}

	private String getMessage(Call call) {
		return call.callerName() + " wants to connect to you.";
	}

	private String getHelpMessage() {
		return "Do you want to accept this request and allow this person to connect to you?\n\nIf you reject it now, you can manually add this contact later.";
	}
}
