package sneer.bricks.skin.widgets.reactive.impl;

import static sneer.foundation.environments.Environments.my;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;

import sneer.bricks.hardware.ram.ref.weak.keeper.WeakReferenceKeeper;
import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.skin.main.synth.Synth;
import sneer.bricks.skin.widgets.reactive.TextWidget;
import sneer.foundation.lang.Consumer;
import sneer.foundation.lang.PickyConsumer;

class RLabelImpl extends RPanel<JLabel> implements TextWidget<JLabel>{

	private final JLabel _textComponent = new JLabel();
	private final Signal<?> _source;
	private final PickyConsumer<? super String> _setter;

	@SuppressWarnings("unused")	private final Object _referenceToAvoidGc;

	RLabelImpl(Signal<?> text){
		this(text, null);
	}

	RLabelImpl(Signal<?> source, PickyConsumer<? super String> setter) {
		_setter = setter;
		_source = source;

		_referenceToAvoidGc = source.addReceiver(new Consumer<Object>() {@Override public void consume(final Object value) {
			textComponent().setText(valueToString(value));
		}});

		initComponents();
	}
	
	public RLabelImpl(Signal<String> source, String synthName) {
		this(source);
		my(Synth.class).attach(_textComponent);
		_textComponent.setName(synthName);
	}

	private String valueToString(final Object value) {
		return (value==null)?"":value.toString();
	}
	
	private void initComponents() {
		setLayout(new GridBagLayout());
		GridBagConstraints c;
		c = new GridBagConstraints(0,0,1,1,1.0,1.0,
				GridBagConstraints.EAST, 
				GridBagConstraints.BOTH,
				new Insets(0,0,0,0),0,0);
		setOpaque(false);
		textComponent().setText(valueToString(_source.currentValue()));
		add(textComponent(), c);
	}

	@Override
	public JLabel getMainWidget() {
		return textComponent();
	}
	
	@Override
	public JComponent[] getWidgets() {
		return new JComponent[]{textComponent()};
	}

	@Override
	public Signal<?> output() {
		return _source;
	}

	@Override
	public PickyConsumer<? super String> setter() {
		if(_setter==null)
			throw new sneer.foundation.lang.exceptions.NotImplementedYet(); // Implement
		
		return _setter;
	}
	
	public JLabel textComponent() {
		return my(WeakReferenceKeeper.class).keep(_textComponent, this);
	}
}