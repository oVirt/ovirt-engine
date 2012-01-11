package org.ovirt.engine.ui.common.widget;

/**
 * Widgets that implement this interface have a visual label associated with them.
 */
public interface HasLabel {

    /**
     * Returns the label of this widget.
     */
    String getLabel();

    /**
     * Sets the label of this widget.
     */
    void setLabel(String label);

}
