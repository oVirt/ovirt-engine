package org.ovirt.engine.ui.common.uicommon.model;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.TreeNodeInfo;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;

/**
 * A Model of Tree Nodes
 *
 * @param <T>
 *            The Tree Node Type
 * @param <M>
 *            The TreeNodeModel Type
 */
public interface TreeNodeModel<T, M extends TreeNodeModel<T, M>> extends TreeNodeInfo, HasSelectionHandlers<M> {

    @Override
    M getParent();

    @Override
    List<M> getChildren();

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
