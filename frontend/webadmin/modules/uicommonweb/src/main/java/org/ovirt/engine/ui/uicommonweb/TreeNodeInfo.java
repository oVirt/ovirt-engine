package org.ovirt.engine.ui.uicommonweb;

import java.util.List;

/**
 * Adapter interface for providing tree node information.
 */
public interface TreeNodeInfo {

    /**
     * Returns the parent node, or {@code null} in case of root node.
     */
    TreeNodeInfo getParent();

    /**
     * Returns child nodes, or an empty list in case of leaf node.
     */
    List<? extends TreeNodeInfo> getChildren();

}
