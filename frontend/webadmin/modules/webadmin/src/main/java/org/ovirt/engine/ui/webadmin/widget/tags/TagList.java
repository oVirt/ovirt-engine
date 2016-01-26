package org.ovirt.engine.ui.webadmin.widget.tags;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.action.AbstractActionStackPanelItem;
import org.ovirt.engine.ui.common.widget.action.SimpleActionPanel;
import org.ovirt.engine.ui.common.widget.tree.ElementIdCellTree;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.Widget;

public class TagList extends AbstractActionStackPanelItem<TagModelProvider, TagListModel, CellTree> {

    interface WidgetUiBinder extends UiBinder<Widget, TagList> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<TagList> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    private static final TagTreeResources res = GWT.create(TagTreeResources.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public TagList(TagModelProvider modelProvider) {
        super(modelProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
        addActionButtons(modelProvider);
        addModelListeners(modelProvider);
    }

    @Override
    protected CellTree createDataDisplayWidget(TagModelProvider modelProvider) {
        CellTree display = new ElementIdCellTree<>(modelProvider, null, res);
        display.setAnimationEnabled(true);
        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        return display;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected SimpleActionPanel<TagListModel> createActionPanel(TagModelProvider modelProvider) {
        return new SimpleActionPanel(modelProvider, modelProvider.getSelectionModel(),
                ClientGinjectorProvider.getEventBus());
    }

    private void addActionButtons(final TagModelProvider modelProvider) {
        actionPanel.addActionButton(new WebAdminButtonDefinition<TagListModel>(constants.newTag()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getNewCommand();
            }
        });

        actionPanel.addActionButton(new WebAdminButtonDefinition<TagListModel>(constants.editTag()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getEditCommand();
            }
        });

        actionPanel.addActionButton(new WebAdminButtonDefinition<TagListModel>(constants.removeTag()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });
    }

    private void addModelListeners(final TagModelProvider modelProvider) {
        modelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                expandTree(getDataDisplayWidget().getRootTreeNode());
            }
        });
        modelProvider.getModel().getResetRequestedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                expandTree(getDataDisplayWidget().getRootTreeNode());
            }
        });
    }

    private void expandTree(TreeNode node) {
        if (node == null) {
            return;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            expandTree(node.setChildOpen(i, true));
        }
    }

    interface TagTreeResources extends CellTree.Resources {

        interface TableStyle extends CellTree.Style {
        }

        @Override
        @Source({ CellTree.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TagTree.css" })
        TableStyle cellTreeStyle();

    }

}
