package org.ovirt.engine.ui.userportal.client.util.messages;

import java.util.Date;

public class Message {
	protected String title;
	protected String detailed;
	protected Date createdAt = new Date();
	protected Severity severity;
	
	public enum Severity { Debug, Info, Warning, Error };

	public Message(String title, Severity severity) {
        this.title = title;
        this.severity = severity;
    }

    public Message(String title, String detailed, Severity severity) {
        this.title = title;
        this.detailed = detailed;
        this.severity = severity;
    }
}
