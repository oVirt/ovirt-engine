package org.ovirt.engine.ui.webadmin.section.main.view.popup.user;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.models.users.EventNotificationModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user.ManageEventsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundTreePopupView;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SimpleSelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Inject;

public class ManageEventsPopupView extends AbstractModelBoundTreePopupView<EventNotificationModel>
        implements ManageEventsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<EventNotificationModel, ManageEventsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ManageEventsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ManageEventsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final CellTree.Resources res = GWT.create(AssignTagTreeResources.class);

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTree<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> tree;

    @UiField
    @Path(value = "email.entity")
    @WithElementId("email")
    StringEntityModelTextBoxEditor emailEditor;

    @UiField
    @Ignore
    Label titleLabel;

    @UiField
    @Ignore
    @WithElementId
    Button expandAllButton;

    @UiField
    @Ignore
    @WithElementId
    Button collapseAllButton;

    @UiField
    @Ignore
    Label infoLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ManageEventsPopupView(EventBus eventBus) {
        super(eventBus);
        initTree();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initExpandButtons();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    void localize() {
        emailEditor.setLabel(constants.manageEventsPopupEmailLabel());
        titleLabel.setText(constants.manageEventsPopupTitleLabel());
        expandAllButton.setText(constants.treeExpandAll());
        collapseAllButton.setText(constants.treeCollapseAll());
        infoLabel.setText(constants.manageEventsPopupInfoLabel());
    }

    private void initTree() {
        tree = new EntityModelCellTree<>(res);
    }

    @Override
    protected ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> getTreeViewModel() {
        return tree.getTreeViewModel();
    }

    private void initExpandButtons() {
        expandAllButton.addClickHandler(event -> expandTree());

        collapseAllButton.addClickHandler(event -> collapseTree());
    }

    @Override
    public void edit(EventNotificationModel object) {
        driver.edit(object);

        // Listen to Properties
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            EventNotificationModel model = (EventNotificationModel) sender;
            String propertyName = args.propertyName;
            if ("EventGroupModels".equals(propertyName)) { //$NON-NLS-1$
                updateTree(model);
            }
        });
    }

    private void updateTree(EventNotificationModel model) {
        // Get tag node list
        ArrayList<SelectionTreeNodeModel> tagTreeNodes = model.getEventGroupModels();

        // Get tree view model
        ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> modelListTreeViewModel =
                tree.getTreeViewModel();

        // Set root nodes
        List<SimpleSelectionTreeNodeModel> rootNodes = SimpleSelectionTreeNodeModel.fromList(tagTreeNodes);
        modelListTreeViewModel.setRoots(rootNodes);

        // Update tree data
        AsyncDataProvider<SimpleSelectionTreeNodeModel> asyncTreeDataProvider =
                modelListTreeViewModel.getAsyncTreeDataProvider();
        asyncTreeDataProvider.updateRowCount(rootNodes.size(), true);
        asyncTreeDataProvider.updateRowData(0, rootNodes);
    }

    private void expandTree() {
        if (tree != null) {
            expandTree(tree.getRootTreeNode(), true);
        }
    }

    private void collapseTree() {
        if (tree != null) {
            expandTree(tree.getRootTreeNode(), false);
        }
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

    @Override
    public EventNotificationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    interface AssignTagTreeResources extends CellTree.Resources {

        interface TableStyle extends CellTree.Style {
        }

        @Override
        @Source("org/ovirt/engine/ui/webadmin/css/NotificationsTree.css")
        TableStyle cellTreeStyle();

    }

}
