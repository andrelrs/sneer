package sneer.kernel.gui;

import java.net.URL;

import org.prevayler.Prevayler;

import sneer.kernel.business.BusinessSource;
import sneer.kernel.gui.contacts.ShowContactsScreenAction;
import wheel.io.ui.CancelledByUser;
import wheel.io.ui.TrayIcon;
import wheel.io.ui.User;
import wheel.io.ui.TrayIcon.Action;
import wheel.io.ui.impl.TrayIconImpl;
import wheel.io.ui.impl.ValueChangePane;
import wheel.lang.IntegerParser;

public class Gui {

	public Gui(User user, Prevayler prevayler) throws Exception {
		_user = user;

		_prevayler = prevayler;
		_business = PersistenceHandler.persistentProxyFor(_prevayler);

		URL icon = Gui.class.getResource("/sneer/kernel/gui/traymenu/yourIconGoesHere.png");
		_trayIcon = new TrayIconImpl(icon, _user.catcher());
	
		tryToRun();
	}


	private final User _user;
	private final TrayIcon _trayIcon;
	
	private final Prevayler _prevayler;
	private final BusinessSource _business;
	
	
	private void tryToRun() {
		//Refactor: remove this logic from the gui;
		if (_business.output().ownName() == null) nameChangeAction().run();

		_trayIcon.addAction(nameChangeAction());
		_trayIcon.addAction(addNewContactAction());
		_trayIcon.addAction(new ShowContactsScreenAction(_business));
		_trayIcon.addAction(sneerPortChangeAction());
		_trayIcon.addAction(exitAction());
	}


	private ValueChangePane sneerPortChangeAction() {
		String prompt = " Change this only if you know what you are doing." +
						"\n Sneer IP port to listen:";
		return new ValueChangePane("Sneer Port Configuration",prompt, _user, _business.output().sneerPort(), new IntegerParser(_business.sneerPortSetter()));
	}

	
	private Action nameChangeAction() {
		String prompt = " What is your name?" +
						"\n (You can change it any time you like)";
		return new ValueChangePane("Name Change",prompt, _user, _business.output().ownName(), _business.ownNameSetter());
	}

	private Action addNewContactAction() {
		return new CancellableAction(){

			public String caption() {
				return "Add New Contact";
			}

			@Override
			public void tryToRun() throws CancelledByUser {
				new NewContactAddition(_user, _business);
			}
		};
	}

	
	private Action exitAction() {
		return new Action(){

			public String caption() {
				return "Exit";
			}

			public void run() {
				System.exit(0);
			}
		};
	}

	
}
