package org.ovirt.engine.ui.uicommonweb;

/**
 * Interface to be implemented by disposable objects.
 */
public interface HasCleanup {

    /**
     * Cleans up the object, e.g. removing listener references that are no longer needed.
     */
    void cleanup();

}
