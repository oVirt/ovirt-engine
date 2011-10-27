package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SystemTree extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, SystemTree> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    Widget treeDisplayWidget;

    public SystemTree(SystemTreeModelProvider modelProvider) {
        this.treeDisplayWidget = createTreeDisplayWidget(modelProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    Widget createTreeDisplayWidget(SystemTreeModelProvider modelProvider) {
        SystemTreeResources res = GWT.create(SystemTreeResources.class);
        CellTree display = new CellTree(modelProvider, null, res);
        display.setAnimationEnabled(true);
        return display;
    }

    public interface SystemTreeResources extends CellTree.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTree.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SystemTree.css" })
        Style cellTreeStyle();
    }

}
