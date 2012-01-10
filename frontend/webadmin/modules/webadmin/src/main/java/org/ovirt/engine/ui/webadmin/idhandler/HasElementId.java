package org.ovirt.engine.ui.webadmin.idhandler;

/**
 * Interface typically implemented by custom UI objects that provides an abstract way of setting DOM element IDs.
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
