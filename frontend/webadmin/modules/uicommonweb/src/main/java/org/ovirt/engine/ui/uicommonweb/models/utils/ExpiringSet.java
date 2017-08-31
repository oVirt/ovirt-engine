package org.ovirt.engine.ui.uicommonweb.models.utils;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.Timer;

/**
 * Set items of which are automatically removed after a period of time.
 */
public class ExpiringSet<T> {

    private final Set<T> set = new HashSet<>();
    private final int removalDelaySec;

    public ExpiringSet(int removalDelaySec) {
        this.removalDelaySec = removalDelaySec;
    }

    public void add(final T item, final RemovalAction<T> removalAction) {
        set.add(item);
        new Timer() {
            @Override
            public void run() {
                final boolean itemWasPresent = set.remove(item);
                if (!itemWasPresent) {
                    return;
                }
                removalAction.itemRemoved(item);
            }
        }.schedule(removalDelaySec * 1000);
    }

    public boolean contains(T item) {
        return set.contains(item);
    }

    @FunctionalInterface
    public interface RemovalAction<T> {
        void itemRemoved(T removedItem);
    }
}
