package org.ovirt.engine.ui.common.widget.tree;

import com.google.gwt.view.client.TreeViewModel;

/**
 * A {@link TreeViewModel} that supports assigning DOM element IDs to tree nodes for better accessibility.
 */
public interface TreeModelWithElementId extends TreeViewModel {

    /**
     * Sets the element ID prefix to use for tree node elements.
     */
    void setElementIdPrefix(String elementIdPrefix);

}
