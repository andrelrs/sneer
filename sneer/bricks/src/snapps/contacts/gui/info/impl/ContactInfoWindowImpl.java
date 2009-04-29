package snapps.contacts.gui.info.impl;

import static sneer.commons.environments.Environments.my;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import snapps.contacts.actions.ContactAction;
import snapps.contacts.actions.ContactActionManager;
import snapps.contacts.gui.ContactsGui;
import snapps.contacts.gui.info.ContactInfoWindow;
import sneer.commons.lang.Functor;
import sneer.hardware.cpu.exceptions.IllegalParameter;
import sneer.hardware.cpu.lang.Consumer;
import sneer.hardware.cpu.lang.PickyConsumer;
import sneer.pulp.contacts.Contact;
import sneer.pulp.contacts.ContactManager;
import sneer.pulp.internetaddresskeeper.InternetAddress;
import sneer.pulp.internetaddresskeeper.InternetAddressKeeper;
import sneer.pulp.reactive.Signal;
import sneer.pulp.reactive.Signals;
import sneer.skin.main.synth.scroll.SynthScrolls;
import sneer.skin.widgets.reactive.NotificationPolicy;
import sneer.skin.widgets.reactive.ReactiveWidgetFactory;
import sneer.skin.widgets.reactive.TextWidget;
import sneer.skin.windowboundssetter.WindowBoundsSetter;

class ContactInfoWindowImpl extends JFrame implements ContactInfoWindow{

	private final InternetAddressKeeper _addressKeeper = my(InternetAddressKeeper.class);
	
	private final JList _lstAddresses = new JList();
	private final JTextField _host = new JTextField();
	private final JTextField _port = new JTextField();
	private InternetAddress _selectedAdress;
	
	private boolean _isGuiInitialized = false;
	private TextWidget<JTextField> _txtNickname;

	ContactInfoWindowImpl() {
		addContactEditAction();
	}

	private void addContactEditAction() {
		my(ContactActionManager.class).addContactAction(new ContactAction(){
			@Override public boolean isEnabled() { return true; }
			@Override public boolean isVisible() { return true; }
			@Override public String caption() { return "Edit Contact...";}
			@Override public void run() {
				my(ContactInfoWindow.class).open();
		}}, true);
	}
	
	@Override
	public void open() {
		if(!_isGuiInitialized) {
			_isGuiInitialized = true;
			initGui();
			loadInternetAddressesForCurrentContact();
		}
		setVisible(true);
	}
	
	private void rebindContact() {
		if (!_isGuiInitialized) return;
		loadInternetAddressesForCurrentContact();
	}

	private void initGui() {
		setTitle("Contact Info:");

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Internet Adresses:"));
		JLabel labNickname = new JLabel("Nickname:");
		JLabel labPort = new JLabel("Port:");
		JLabel labHost = new JLabel("Host:");
		
		Signal<String> nickname = my(Signals.class).adaptSignal(my(ContactsGui.class).selectedContact(), new Functor<Contact, Signal<String>>() { @Override public Signal<String> evaluate(Contact contact) {
			return contact.nickname();
		}});
		
		PickyConsumer<String> setter = new PickyConsumer<String>(){@Override public void consume(String value) throws IllegalParameter {
			my(ContactManager.class).nicknameSetterFor(contact()).consume(value);
		}};
		
		_txtNickname = my(ReactiveWidgetFactory.class).newTextField(nickname, setter, NotificationPolicy.OnEnterPressedOrLostFocus);
		
		JScrollPane scroll = my(SynthScrolls.class).create();
		scroll.getViewport().add(_lstAddresses);

		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e) {
			newInternetAddress();
		}});
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e) {
			saveInternetAddress();
		}});
		
		JButton btnDel = new JButton("Delete");
		btnDel.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e) {
			delInternetAddress();
		}});
		
		setGridBagLayout(panel, labNickname, labPort, labHost, scroll, btnNew, btnSave, btnDel);

		listenToSignals();
		addListSelectionListestener();

		this.setSize(400, 310);
		my(WindowBoundsSetter.class).setBestBounds(this);
	}

	private void setGridBagLayout(JPanel panel, JLabel labNickname, JLabel labPort, JLabel labHost, JScrollPane scroll, JButton btnNew, JButton btnSave, JButton btnDel) {
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(labNickname,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,0,5), 0, 0) );

		getContentPane().add(_txtNickname.getComponent(),  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5,0,0,5), 0, 0) );
		
		getContentPane().add(panel,  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0) );
		
		panel.setLayout(new GridBagLayout());
		panel.add(scroll,  new GridBagConstraints(0, 1, 12, 1, 1.0, 1.0, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0) );
		
		panel.add(labHost,  new GridBagConstraints(0, 2, 1,1, 0.0,0.0, 
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,5,0,5), 0, 0) );

		panel.add(_host,  new GridBagConstraints(1, 2, 7, 1, 2.0,0.0, 
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0) );

		panel.add(labPort,  new GridBagConstraints(9, 2, 1,1, 0.0,0.0, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,5,5,5), 0, 0) );
		
		panel.add(_port,  new GridBagConstraints(10, 2, 2,1, 0.0,0.0, 
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,5), 0, 0) );
		
		panel.add(btnNew,  new GridBagConstraints(7,3, 1,1, 0.0,0.0, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0) );

		panel.add(btnSave,  new GridBagConstraints(8,3, 3, 1, 0.0,0.0, 
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0) );

		panel.add(btnDel,  new GridBagConstraints(11,3, 1,1, 0.0,0.0, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0) );
	}
	
	private void saveInternetAddress() {
		//Fix: change to reactive
		InternetAddress address = _selectedAdress;
		if(address == null || _host.getText().trim().length()==0) return;
		
		_addressKeeper.remove(address);
		newInternetAddress();
	}
	
	private void listenToSignals() {
		my(Signals.class).receive(this, new Consumer<Contact>(){ @Override public void consume(Contact value) {
			rebindContact();
		}}, my(ContactsGui.class).selectedContact());
	}

	private void addListSelectionListestener() {
		_lstAddresses.getSelectionModel().addListSelectionListener(
				new ListSelectionListener(){ @Override public void valueChanged(ListSelectionEvent e) {
					InternetAddressPrettyPrinter wrapper = (InternetAddressPrettyPrinter) _lstAddresses.getSelectedValue();
					if(wrapper==null){
						cleanFields();
						return;
					}
					
					_selectedAdress = wrapper._address;
					_host.setText(_selectedAdress.host());
					_port.setText(""+_selectedAdress.port());
				}});
	}
	
	private void delInternetAddress() {
		InternetAddressPrettyPrinter wrapper = (InternetAddressPrettyPrinter) _lstAddresses.getSelectedValue();
		if(wrapper==null) return;
		
		InternetAddress address = wrapper._address;
		_addressKeeper.remove(address);
		((DefaultListModel)_lstAddresses.getModel()).removeElement(wrapper);
	}
	
	private void newInternetAddress() {
		String host = _host.getText();
		if(host==null || host.trim().length()==0) return;
		
		int port = Integer.parseInt(_port.getText());
		_addressKeeper.add(contact(), host, port);
		loadInternetAddressesForCurrentContact() ;
		cleanFields();
	}

	private void cleanFields() {
		_host.setText("");
		_port.setText("");
	}
	
	private void loadInternetAddressesForCurrentContact() {
		DefaultListModel model = new DefaultListModel();
		List<InternetAddress> elements = _addressKeeper.addresses().currentElements();
		for (InternetAddress internetAddress : elements) {
			if(internetAddress.contact() == contact())
				model.addElement(new InternetAddressPrettyPrinter(internetAddress));
		}
		_lstAddresses.setModel(model);
	}
	
	private Contact contact() {
		return my(ContactsGui.class).selectedContact().currentValue();
	}
	
	private class InternetAddressPrettyPrinter{
		final InternetAddress _address;
		
		public InternetAddressPrettyPrinter(InternetAddress address) {
			_address = address;
		}
		
		@Override 
		public String toString() {
			return _address.host()+" : "+_address.port();
		}
	}
}