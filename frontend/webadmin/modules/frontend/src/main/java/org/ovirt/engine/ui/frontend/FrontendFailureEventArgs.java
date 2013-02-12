package org.ovirt.engine.ui.frontend;

import java.util.List;

import org.ovirt.engine.ui.uicompat.EventArgs;

public class FrontendFailureEventArgs extends EventArgs {

    private Message message;
    private List<Message> messages;

    public FrontendFailureEventArgs(Message errorMessage) {
        this.message = errorMessage;
    }

    public FrontendFailureEventArgs(List<Message> errorMessages) {
        this.messages = errorMessages;
    }

    public Message getMessage() {
        return message;
    }

    public List<Message> getMessages() {
        return messages;
    }

}
