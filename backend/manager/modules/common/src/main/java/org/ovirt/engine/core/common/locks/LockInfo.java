package org.ovirt.engine.core.common.locks;

import java.io.Serializable;
import java.util.Set;

public class LockInfo implements Serializable {
    private static final long serialVersionUID = -4454507217567224398L;

    private boolean exclusive;
    private Set<String> messages;

    public LockInfo() {
    }

    public LockInfo(boolean exclusive, Set<String> messages) {
        setExclusive(exclusive);
        setMessages(messages);
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Set<String> getMessages() {
        return messages;
    }

    public void setMessages(Set<String> messages) {
        this.messages = messages;
    }
}
