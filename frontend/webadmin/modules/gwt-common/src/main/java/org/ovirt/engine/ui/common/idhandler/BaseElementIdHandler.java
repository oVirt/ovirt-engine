package org.ovirt.engine.ui.common.idhandler;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Base class for generated {@link ElementIdHandler} implementations.
 * <p>
 * Provides an abstraction for handling different field types with regard to setting DOM element IDs.
 *
 * @param <T>
 *            The type of an object that contains {@literal @WithElementId} fields.
 *
 * @see ElementIdHandler
 */
public abstract class BaseElementIdHandler<T> implements ElementIdHandler<T> {

    private String idExtension = ""; //$NON-NLS-1$

    @Override
    public void setIdExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return;
        }

        this.idExtension = "_" + extension; //$NON-NLS-1$
    }

    protected final void setElementId(Object obj, String elementId) {
        setExtendedElementId(obj, elementId + idExtension);
    }

    /**
     * Applies the generated (and possibly extended) DOM element ID to the given object.
     *
     * @param obj
     *            Object for which to set the element ID.
     * @param elementId
     *            Element ID to set.
     */
    protected void setExtendedElementId(Object obj, String elementId) {
        if (obj instanceof HasElementId) {
            ((HasElementId) obj).setElementId(elementId);
        } else if (obj instanceof UIObject) {
            ((UIObject) obj).getElement().setId(elementId);
        } else if (obj instanceof Element) {
            ((Element) obj).setId(elementId);
        }
    }

}
