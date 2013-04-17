package org.ovirt.engine.ui.common.utils;

import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.TreeNodeModel;

import com.google.gwt.cell.client.Cell.Context;

public class ElementIdUtils {

    /**
     * Returns DOM element ID, based on prefix and custom (dynamic) value.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param value
     *            Custom value used to extend the prefix.
     */
    public static String createElementId(String prefix, String value) {
        String sanitizedValue = value.replaceAll("[^\\w]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        return prefix + "_" + sanitizedValue; //$NON-NLS-1$
    }

    /**
     * Returns DOM element ID for a table cell element.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param columnId
     *            Column ID that will be part of the resulting DOM element ID, or {@code null} to use column index
     *            value.
     * @param context
     *            Table cell context object.
     */
    public static String createTableCellElementId(String prefix, String columnId, Context context) {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append("_"); //$NON-NLS-1$
        sb.append(columnId != null ? columnId : "col" + String.valueOf(context.getColumn())); //$NON-NLS-1$
        sb.append("_row"); //$NON-NLS-1$
        sb.append(String.valueOf(context.getIndex()));
        return sb.toString();
    }

    /**
     * Returns DOM element ID for a tree cell element.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param node
     *            Tree node model object.
     * @param rootNodes
     *            Root node(s) for the given tree.
     */
    public static <M extends TreeNodeModel<?, M>> String createTreeCellElementId(String prefix,
            M node, List<M> rootNodes) {
        String treeNodeId = getTreeNodeId(node, true);
        String treeNodeIdPrefix = prefix + "_root" + getRootNodeIndex(node, rootNodes); //$NON-NLS-1$
        return treeNodeId.isEmpty() ? treeNodeIdPrefix : treeNodeIdPrefix + "_" + treeNodeId; //$NON-NLS-1$
    }

    private static <M extends TreeNodeModel<?, M>> String getTreeNodeId(M node, boolean skipRootNode) {
        boolean isRootNode = node.getParent() == null;
        String id = (skipRootNode && isRootNode) ? "" : "node" + node.getIndex(); //$NON-NLS-1$ //$NON-NLS-2$
        if (!isRootNode) {
            id = getTreeNodeId(node.getParent(), skipRootNode) + "_" + id; //$NON-NLS-1$
        }
        return id.startsWith("_") ? id.substring(1) : id; //$NON-NLS-1$
    }

    private static <M extends TreeNodeModel<?, M>> int getRootNodeIndex(M node, List<M> rootNodes) {
        for (M root : rootNodes) {
            if (node == root) {
                return rootNodes.indexOf(root);
            }
        }
        if (node.getParent() != null) {
            return getRootNodeIndex(node.getParent(), rootNodes);
        }
        return -1;
    }

}
