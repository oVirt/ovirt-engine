package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.action.AbstractActionStackPanelItem;
import org.ovirt.engine.ui.common.widget.action.SimpleActionPanel;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeModel;
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

    interface WidgetUiBinder extends UiBinder<Widget, SystemTree> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final SystemTreeModelProvider modelProvider;

    private CellTree display;

    public SystemTree(SystemTreeModelProvider modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        this.modelProvider = modelProvider;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        addActionButtons(modelProvider, constants);
        addModelListeners(modelProvider);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected SimpleActionPanel<SystemTreeModel> createActionPanel(SystemTreeModelProvider modelProvider) {
        return new SimpleActionPanel(modelProvider, modelProvider.getSelectionModel(),
                ClientGinjectorProvider.instance().getEventBus());
    }

    private void addActionButtons(final SystemTreeModelProvider modelProvider, final ApplicationConstants constants) {

        actionPanel.addActionButton(new WebAdminButtonDefinition<SystemTreeModel>(constants.treeExpandAll()) {
            @Override
            protected UICommand resolveCommand() {
                return new UICommand(constants.treeExpandAll(), new BaseCommandTarget() {
                    @Override
                    public void ExecuteCommand(UICommand command) {
                        expandTree(display.getRootTreeNode(), true);
                    }
                });
            }
        });

        actionPanel.addActionButton(new WebAdminButtonDefinition<SystemTreeModel>(constants.treeCollapseAll()) {
            @Override
            protected UICommand resolveCommand() {
                return new UICommand(constants.treeCollapseAll(), new BaseCommandTarget() {
                    @Override
                    public void ExecuteCommand(UICommand command) {
                        expandTree(display.getRootTreeNode(), false);
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

                // Collapse tree on refresh
                expandTree(display.getRootTreeNode(), false, 2);
            }
        });
    }

    @Override
    protected CellTree createDataDisplayWidget(SystemTreeModelProvider modelProvider) {
        SystemTreeResources res = GWT.create(SystemTreeResources.class);
        display = new CellTree(modelProvider, null, res) {
            protected void onLoad() {
                expandTree(display.getRootTreeNode(), false, 2);
            }
        };
        display.setAnimationEnabled(true);
        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        return display;
    }

    private void expandTree(TreeNode node, boolean expand) {
        expandTree(node, expand, 0);
    }

    private void expandTree(TreeNode node, boolean expand, int expandFromLevel) {
        if (node == null) {
            return;
        }

        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                boolean expandNode = 0 < expandFromLevel ? !expand : expand;
                expandTree(node.setChildOpen(i, expandNode), expand, expandFromLevel - 1);
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
