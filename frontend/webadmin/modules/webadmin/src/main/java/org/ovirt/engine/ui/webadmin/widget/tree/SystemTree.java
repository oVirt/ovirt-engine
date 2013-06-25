package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.action.AbstractActionStackPanelItem;
import org.ovirt.engine.ui.common.widget.action.SimpleActionPanel;
import org.ovirt.engine.ui.common.widget.tree.ElementIdCellTree;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.Style;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.Widget;

public class SystemTree extends AbstractActionStackPanelItem<SystemTreeModelProvider, SystemTreeModel, CellTree> {

    private static final SystemTreeResources res = GWT.create(SystemTreeResources.class);

    private static final int ALL_LEVELS = Integer.MAX_VALUE;
    private static final int ITEM_LEVEL = 3;

    interface WidgetUiBinder extends UiBinder<Widget, SystemTree> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<SystemTree> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    public SystemTree(SystemTreeModelProvider modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
        addActionButtons(modelProvider, constants);
        addModelListeners(modelProvider);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected SimpleActionPanel<SystemTreeModel> createActionPanel(SystemTreeModelProvider modelProvider) {
        return new SimpleActionPanel(modelProvider, modelProvider.getSelectionModel(),
                ClientGinjectorProvider.getEventBus());
    }

    private void addActionButtons(final SystemTreeModelProvider modelProvider, final ApplicationConstants constants) {
        actionPanel.addActionButton(new WebAdminButtonDefinition<SystemTreeModel>(constants.treeExpandAll()) {
            @Override
            protected UICommand resolveCommand() {
                return new UICommand(constants.treeExpandAll(), new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand command) {
                        TreeNode expandNode = findNode(getDataDisplayWidget().getRootTreeNode(),
                                modelProvider.getSelectionModel().getSelectedObject());
                        if (expandNode != null) {
                            expandTree(expandNode);
                        }
                    }
                });
            }
        });
        actionPanel.addActionButton(new WebAdminButtonDefinition<SystemTreeModel>(constants.treeCollapseAll()) {
            @Override
            protected UICommand resolveCommand() {
                return new UICommand(constants.treeCollapseAll(), new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand command) {
                        TreeNode collapseNode = findNode(getDataDisplayWidget().getRootTreeNode(),
                                modelProvider.getSelectionModel().getSelectedObject());
                        if (collapseNode != null) {
                            collapseTree(collapseNode);
                        }
                    }
                });
            }
        });
    }

    private void addModelListeners(final SystemTreeModelProvider modelProvider) {
        modelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // Reset selection in the model
                SystemTreeItemModel lastSelectedItem = modelProvider.getSelectionModel().getSelectedObject();
                modelProvider.getSelectionModel().setSelected(lastSelectedItem, false);
                expandTree(getDataDisplayWidget().getRootTreeNode(), ITEM_LEVEL);
            }
        });
    }

    @Override
    protected CellTree createDataDisplayWidget(SystemTreeModelProvider modelProvider) {
        CellTree display = new ElementIdCellTree<SystemTreeModelProvider>(modelProvider, null, res) {
            @Override
            protected void onLoad() {
                expandTree(getDataDisplayWidget().getRootTreeNode(), ITEM_LEVEL);
            }
        };
        display.setAnimationEnabled(true);
        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        return display;
    }

    private void expandTree(TreeNode node) {
        expandTree(node, ALL_LEVELS);
    }

    private void expandTree(TreeNode node, int expandUpToLevel) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            boolean expandNode = 0 < expandUpToLevel ? true : false;
            expandTree(node.setChildOpen(i, expandNode), expandUpToLevel - 1);
        }
    }

    private void collapseTree(TreeNode node) {
        TreeNode parent = node.getParent();
        if (parent == null) {
            node.setChildOpen(0, false);
        } else {
            parent.setChildOpen(node.getIndex(), false);
        }
    }

    private TreeNode findNode(TreeNode node, SystemTreeItemModel model) {
        TreeNode result = null;
        if (node == null) {
            return null;
        }

        int i = 0;
        while (result == null && i < node.getChildCount()) {
            if (model != null && model.equals(node.getChildValue(i))) {
                result = node.setChildOpen(i, true);
                break;
            }
            // Only check open nodes, otherwise they couldn't have been selected.
            if (node.isChildOpen(i)) {
                result = findNode(node.setChildOpen(i, true), model);
            }
            i++;
        }

        return result;
    }

    public interface SystemTreeResources extends CellTree.Resources {

        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTree.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SystemTree.css" })
        Style cellTreeStyle();

    }

}
