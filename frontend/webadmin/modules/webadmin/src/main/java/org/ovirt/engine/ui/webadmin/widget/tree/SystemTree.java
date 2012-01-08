package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.Style;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SystemTree extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, SystemTree> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    Widget treeDisplayWidget;

    @UiField
    ButtonBase refreshButton;

    @UiField
    ButtonBase expandButton;

    @UiField
    ButtonBase collapseButton;

    private final SystemTreeModelProvider modelProvider;

    private CellTree display;

    public SystemTree(SystemTreeModelProvider modelProvider, ApplicationConstants constants) {
        this.modelProvider = modelProvider;
        this.treeDisplayWidget = createTreeDisplayWidget(modelProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
    }

    private void localize(ApplicationConstants constants) {
        expandButton.setText(constants.treeExpandAll());
        collapseButton.setText(constants.treeCollapseAll());
    }

    Widget createTreeDisplayWidget(SystemTreeModelProvider modelProvider) {
        SystemTreeResources res = GWT.create(SystemTreeResources.class);
        display = new CellTree(modelProvider, null, res);
        display.setAnimationEnabled(true);
        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        return display;
    }

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent event) {
        modelProvider.getModel().ForceRefresh();
        expandTree(display.getRootTreeNode(), false);
    }

    @UiHandler("expandButton")
    void handleExpandButtonClick(ClickEvent event) {
        expandTree(display.getRootTreeNode(), true);
    }

    @UiHandler("collapseButton")
    void handlecollapseButtonClick(ClickEvent event) {
        expandTree(display.getRootTreeNode(), false);
    }

    private void expandTree(TreeNode node, boolean collapse) {
        if (node == null) {
            return;
        }

        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                expandTree(node.setChildOpen(i, collapse), collapse);
            }
        }
    }

    public interface SystemTreeResources extends CellTree.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTree.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SystemTree.css" })
        Style cellTreeStyle();
    }

}
