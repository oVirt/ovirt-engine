package org.ovirt.engine.core.common.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class PerHostMessages {
    Map<Guid, List<String>> messages;

    public PerHostMessages() {
        messages = new HashMap<>();
    }

    public void addMessage(Guid hostId, String message) {
        ensureHost(hostId);
        messages.get(hostId).add(message);
    }

    private void ensureHost(Guid hostId) {
        if (!messages.containsKey(hostId)) {
            messages.put(hostId, new ArrayList<String>());
        }
    }

    public void addMessages(Guid hostId, Collection<? extends String> message) {
        ensureHost(hostId);
        messages.get(hostId).addAll(message);
    }

    public Map<Guid, List<String>> getMessages() {
        return messages;
    }

    public List<String> getMessages(Guid hostId) {
        if (messages.containsKey(hostId)) {
            return messages.get(hostId);
        }
        else {
            return null;
        }
    }
}
