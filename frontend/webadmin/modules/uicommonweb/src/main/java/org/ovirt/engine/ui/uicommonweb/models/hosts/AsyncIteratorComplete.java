package org.ovirt.engine.ui.uicommonweb.models.hosts;

public interface AsyncIteratorComplete<T> {

    void run(T item, Object value);
}
