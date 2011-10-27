package org.ovirt.engine.ui.userportal.client.util.messages;

import java.util.ArrayList;
import java.util.LinkedList;

public class MessageCenter {
	private LinkedList<Message> messages = new LinkedList<Message>();
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();

	private static final int MAX_MESSAGES = 100;

	public void notify(Message message) {
		this.messages.add(message);
		if (messages.size() > MAX_MESSAGES) {
			messages.removeFirst();
		}
		for (MessageListener listener : listeners) {
			listener.onMessage(message);
		}
	}

	public void addMessageListener(MessageListener listener) {
		this.listeners.add(listener);
	}

	public LinkedList<Message> getMessages() {
		return messages;
	}

	public interface MessageListener {
		void onMessage(Message message);
	}
}
