package org.ovirt.engine.ui.common.idhandler;

/**
 * Interface typically implemented by custom UI objects providing abstraction for setting DOM element IDs.
 *
 * @see WithElementId
 */
public interface HasElementId {

    /**
     * Applies the given DOM element ID to this object.
     *
     * @param elementId
     *            Element ID to set.
     */
    void setElementId(String elementId);

}
