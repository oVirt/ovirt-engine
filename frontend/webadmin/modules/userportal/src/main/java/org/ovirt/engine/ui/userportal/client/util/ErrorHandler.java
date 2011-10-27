package org.ovirt.engine.ui.userportal.client.util;

import org.ovirt.engine.ui.userportal.client.UserPortal;
import org.ovirt.engine.ui.userportal.client.util.messages.Message;

public class ErrorHandler {
	public void handleError(String message) {
		handleError(message, null);
	}

	public void handleError(String message, Throwable t) {
		Message errorMessage = new Message(message, (t == null) ? null
				: t.toString(), Message.Severity.Error);
		UserPortal.getMessageCenter().notify(errorMessage);

		if (t != null) {
			t.printStackTrace();
		}
	}

}
