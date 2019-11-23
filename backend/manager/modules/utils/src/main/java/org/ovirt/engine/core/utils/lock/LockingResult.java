package org.ovirt.engine.core.utils.lock;

import java.util.Set;

public final class LockingResult {
    private static final LockingResult SUCCESS = new LockingResult(true, Set.of());
    private static final LockingResult FAILED = new LockingResult(false, Set.of());

    private final boolean acquired;
    private final Set<String> messages;

    private LockingResult(boolean acquired, Set<String> messages) {
        this.acquired = acquired;
        this.messages = messages;
    }

    public boolean isAcquired() {
        return acquired;
    }

    public Set<String> getMessages() {
        return messages;
    }

    public static LockingResult success() {
        return SUCCESS;
    }

    public static LockingResult fail() {
        return FAILED;
    }

    public static LockingResult fail(Set<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return FAILED;
        }
        return new LockingResult(false, messages);
    }
}
