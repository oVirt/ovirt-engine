package org.ovirt.engine.ui.common.view;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Base class for views having a single content slot for displaying child contents.
 */
public abstract class AbstractSingleSlotView extends AbstractView {

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == getContentSlot()) {
            setContent(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    /**
     * Returns the slot object associated with the view content area.
     */
    protected abstract Object getContentSlot();

    /**
     * Sets the child widget into the view content area.
     */
    protected abstract void setContent(IsWidget content);

}
