package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;

/**
 * A Model of Tree Nodes
 *
 * @param <T>
 *            The Tree Node Type
 * @param <M>
 *            The TreeNodeModel Type
 */
public interface TreeNodeModel<T, M extends TreeNodeModel<T, M>> extends HasSelectionHandlers<M> {

    /**
     * Get the Node direct children
     *
     */
    List<M> getChildren();

    /**
     * The Node name
     *
     */
    String getName();

    /**
     * Get the Node selection state
     *
     */
    boolean getSelected();

    /**
     * Is the Node editable
     *
     */
    boolean isEditable();

    /**
     * Set the Node Selection state
     *
     * @return
     *
     */
    void setSelected(boolean value);

}
