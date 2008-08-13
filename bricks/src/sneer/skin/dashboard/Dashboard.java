package sneer.skin.dashboard;

import java.awt.Container;

import sneer.skin.laf.LafContainer;
import sneer.skin.viewmanager.Snapp;

public interface Dashboard extends LafContainer{

	Container getRootPanel();
	Container getContentPanel();
	SnappFrame installSnapp(Snapp snapp);
	void moveSnapp(int index, SnappFrame frame);
	void moveSnappUp(SnappFrame frame);
	void moveSnappDown(SnappFrame frame);
	
}
