package org.ovirt.engine.ui.common.utils;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.TreeNodeInfo;

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
     * Returns DOM element ID for a form grid element.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param columnId
     *            Column ID that will be part of the resulting DOM element ID
     * @param rowId
     *            Row ID that will be part of the resulting DOM element ID
     */
    public static String createFormGridElementId(String prefix, int columnId, int rowId, String suffix) {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append("_"); //$NON-NLS-1$
        sb.append("col"); //$NON-NLS-1$
        sb.append(columnId);
        sb.append("_row"); //$NON-NLS-1$
        sb.append(rowId);
        sb.append(suffix);
        return sb.toString();
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
     *            Tree node object.
     * @param rootNodes
     *            Root nodes for the given tree, or {@code null} if there is single root node.
     */
    public static String createTreeCellElementId(String prefix, TreeNodeInfo node,
            List<? extends TreeNodeInfo> rootNodes) {
        String treeNodeId = getTreeNodeId(node, true);
        String treeNodeIdPrefix = prefix + "_root" + getRootNodeIndex(node, rootNodes); //$NON-NLS-1$
        return treeNodeId.isEmpty() ? treeNodeIdPrefix : treeNodeIdPrefix + "_" + treeNodeId; //$NON-NLS-1$
    }

    private static String getTreeNodeId(TreeNodeInfo node, boolean skipRootNode) {
        boolean isRootNode = node.getParent() == null;
        String id = (skipRootNode && isRootNode) ? "" : "node" + getTreeNodeIndex(node); //$NON-NLS-1$ //$NON-NLS-2$
        if (!isRootNode) {
            id = getTreeNodeId(node.getParent(), skipRootNode) + "_" + id; //$NON-NLS-1$
        }
        return id.startsWith("_") ? id.substring(1) : id; //$NON-NLS-1$
    }

    private static int getTreeNodeIndex(TreeNodeInfo node) {
        TreeNodeInfo parent = node.getParent();

        // Root node has index 0
        if (parent == null) {
            return 0;
        }

        // Locate node among its siblings
        List<? extends TreeNodeInfo> siblings = parent.getChildren();
        for (int i = 0; i < siblings.size(); i++) {
            if (node.equals(siblings.get(i))) {
                return i;
            }
        }

        // Node not found in parent's children (hierarchy mismatch)
        return -1;
    }

    private static int getRootNodeIndex(TreeNodeInfo node, List<? extends TreeNodeInfo> rootNodes) {
        TreeNodeInfo parent = node.getParent();

        // Locate node among possible roots
        if (rootNodes != null) {
            for (TreeNodeInfo root : rootNodes) {
                if (node.equals(root)) {
                    return rootNodes.indexOf(root);
                }
            }
        }

        // Not a root node, traverse up to parent
        if (parent != null) {
            return getRootNodeIndex(parent, rootNodes);
        }

        // Single root node found, return index 0
        return 0;
    }

}
