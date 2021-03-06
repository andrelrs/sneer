package sneer.bricks.hardware.gui.trayicon;

import java.net.URL;

import sneer.bricks.pulp.reactive.Signal;
import sneer.foundation.brickness.Brick;

@Brick
public interface TrayIcons {

	TrayIcon newTrayIcon(URL userIcon, Signal<String> tooltip) throws SystemTrayNotSupported;

	void messageBalloon(String title, String message);

}