package org.ovirt.engine.ui.uicommonweb.models.hosts;

/**
 * Defines method to be executed in order to decide whether to continue iteration by AsyncIterator.
 */
public interface AsyncIteratorPredicate<T> {

    boolean match(T item, Object value);
}
