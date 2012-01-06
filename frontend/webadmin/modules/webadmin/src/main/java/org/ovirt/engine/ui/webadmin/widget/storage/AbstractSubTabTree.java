package org.ovirt.engine.ui.webadmin.widget.storage;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;

public abstract class AbstractSubTabTree<M extends SearchableListModel> extends Composite {

    protected final Tree tree;

    protected final ApplicationResources resources;

    @SuppressWarnings("unchecked")
    public AbstractSubTabTree() {
        tree = new Tree();
        initWidget(tree);

        resources = ClientGinjectorProvider.instance().getApplicationResources();
    }

    public void clearTree() {
        tree.clear();
    }

    public abstract void updateTree(final M listModel);

    protected class EmptyColumn extends TextColumn<EntityModel> {
        @Override
        public String getValue(EntityModel object) {
            return null;
        }
    }

    public interface TreeHeaderlessTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TreeHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }
}
