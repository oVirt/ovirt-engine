package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.event.logical.shared.AttachEvent;
import org.ovirt.engine.ui.common.uicommon.model.TreeNodeModel;
import org.ovirt.engine.ui.common.widget.tree.ElementIdCellTree;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTree;

/**
 * A tree for {@link org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel} Nodes
 */
public class EntityModelCellTree<T, M extends TreeNodeModel<T, M>> extends ElementIdCellTree<ModelListTreeViewModel<T, M>> {

    interface Resources {
        CellTree.Resources res = GWT.create(CellTree.BasicResources.class);
    }

    public EntityModelCellTree() {
        this(Resources.res);
    }

    public EntityModelCellTree(CellTree.Resources res) {
        super(new ModelListTreeViewModel<T, M>(), null, res);
        addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (!event.isAttached()) {
                    getTreeViewModel().removeHandlers();
                }
            }
        });
    }

}
