package org.ovirt.engine.ui.common.uicommon.model;

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
     */
    List<M> getChildren();

    /**
     * Returns the parent node, or {@code null} in case of root node.
     */
    M getParent();

    /**
     * Returns the index of this node relative to the parent node, or 0 in case of root node.
     */
    int getIndex();

    /**
     * The Node name
     */
    String getName();

    /**
     * Get the Node selection state
     */
    boolean getSelected();

    /**
     * Is the Node editable
     */
    boolean isEditable();

    /**
     * Set the Node Selection state
     */
    void setSelected(boolean value);

}
