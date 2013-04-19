package org.ovirt.engine.ui.common.idhandler;

/**
 * Interface typically implemented by custom UI objects providing abstraction for retrieving DOM element IDs.
 *
 * @see HasElementId
 */
public interface ProvidesElementId {

    /**
     * Returns DOM element ID that was previously {@linkplain HasElementId#setElementId applied} to this object.
     */
    String getElementId();

}
