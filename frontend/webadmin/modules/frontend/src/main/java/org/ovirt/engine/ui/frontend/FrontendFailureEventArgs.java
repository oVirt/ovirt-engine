package org.ovirt.engine.ui.frontend;

import java.util.List;

import org.ovirt.engine.ui.uicompat.EventArgs;

public class FrontendFailureEventArgs extends EventArgs {

    private List<Message> messages;

    public FrontendFailureEventArgs(List<Message> errorMessages) {
        this.messages = errorMessages;
    }

    public List<Message> getMessages() {
        return messages;
    }

}
