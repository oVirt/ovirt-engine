package org.ovirt.engine.ui.common.widget.tree;

import org.ovirt.engine.ui.common.idhandler.HasElementId;

import com.google.gwt.user.cellview.client.CellTree;

/**
 * A {@link CellTree} that supports assigning DOM element IDs to tree nodes through {@link HasElementId} interface.
 */
public class ElementIdCellTree<M extends TreeModelWithElementId> extends CellTree implements HasElementId {

    public <T> ElementIdCellTree(M viewModel, T rootValue, Resources resources) {
        super(viewModel, rootValue, resources);
        setDefaultNodeSize(Integer.MAX_VALUE);
    }

    @Override
    public void setElementId(String elementId) {
        getTreeViewModel().setElementIdPrefix(elementId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public M getTreeViewModel() {
        return (M) super.getTreeViewModel();
    }

}
