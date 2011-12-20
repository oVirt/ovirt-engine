package org.ovirt.engine.ui.webadmin.idhandler;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Base class for generated {@link ElementIdHandler} implementations.
 * <p>
 * Provides an abstraction for handling different field types with regard to setting DOM element IDs.
 */
public abstract class BaseElementIdHandler {

    /**
     * Applies a DOM element ID to the given object.
     * 
     * @param obj
     *            Object for which to set the element ID.
     * @param elementId
     *            Element ID to set.
     */
    protected void setElementId(Object obj, String elementId) {
        if (obj instanceof HasElementId) {
            ((HasElementId) obj).setElementId(elementId);
        } else if (obj instanceof UIObject) {
            ((UIObject) obj).getElement().setId(elementId);
        } else if (obj instanceof Element) {
            ((Element) obj).setId(elementId);
        }
    }

}
