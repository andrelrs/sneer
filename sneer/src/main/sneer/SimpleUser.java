//Copyright (C) 2005 Klaus Wuestefeld
//This is free software. It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the license distributed along with this file for more details.

package sneer;

import java.io.IOException;

import sneer.Sneer.User;
import sneer.life.JpgImage;

public abstract class SimpleUser implements User {

	public String confirmName(String currentName) {
		return answer("Your name", currentName);
	}

	public int confirmServerPort(int currentPort) {
		acknowledge(
			"To connect to you, your friends will have to know" +
			" your IP address.\n" +
			"\n" +
			"To discover your own IP address, you" +
			" can point your browser to www.whatsmyip.org or" +
			" www.whatsmyip.info"
		);
		
		String prompt =
			"To connect to you, your friends will also have to" +
			" know your IP port number. You can change it if you know" +
			" what you're doing.\n" +
			"\n" +
			"Suggested port number:";

		String portString = answer(prompt, "" + currentPort);
		return Integer.parseInt(portString);
	}

	public String thoughtOfTheDay(String currentThought) {
		return answer("Thought of the Day", currentThought);
	}

	public String giveNickname() {
		return answer("Give your new contact a nickname", "friend");
	}

	public String informTcpAddress(String defaultAddress) {
		return answer("What is your contact's address? host:port", defaultAddress);
	}

	public JpgImage confirmPicture(JpgImage picture) {
		if (!confirm("Do you want to change your picture?")) return picture;

		String path = answer("File path to your new picture:", "");
		
		try {
			return new JpgImage(path);
		} catch (IOException e) {
			lamentException(e);
			return picture;
		}
	}
	
	public void lamentException(Exception e) {
		e.printStackTrace();
		lamentError(e.toString());
	}

	public String writePublicMessage() {
		return answer("Write your public message: ", "");
	}
	
	abstract protected String answer(String prompt, String defaultAnswer);

	abstract protected boolean confirm(String proposition);

	abstract protected void lamentError(String message);

}
