package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.List;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SimpleSelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Inject;

public class AssignTagsPopupView extends AbstractModelBoundTreePopupView<TagListModel>
        implements AssignTagsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<TagListModel, AssignTagsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AssignTagsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AssignTagsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final CellTree.Resources res = GWT.create(AssignTagTreeResources.class);

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTree<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> tree;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public AssignTagsPopupView(EventBus eventBus) {
        super(eventBus);
        initTree();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    void localize() {
    }

    private void initTree() {
        tree = new EntityModelCellTree<>(res);
    }

    @Override
    public void edit(TagListModel object) {
        driver.edit(object);

        // Listen to Properties
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            TagListModel model = (TagListModel) sender;
            String propertyName = args.propertyName;
            if ("SelectionNodeList".equals(propertyName)) { //$NON-NLS-1$
                updateTree(model);
            }
        });
    }

    @Override
    protected ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> getTreeViewModel() {
        return tree.getTreeViewModel();
    }

    private void updateTree(TagListModel model) {
        // Get tag node list
        List<SelectionTreeNodeModel> tagTreeNodes = model.getSelectionNodeList();

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

        // Expand tree nodes
        expandTree();
    }

    private void expandTree() {
        if (tree != null) {
            expandTree(tree.getRootTreeNode());
        }
    }

    private void expandTree(TreeNode node) {
        if (node == null) {
            return;
        }

        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                expandTree(node.setChildOpen(i, true));
            }
        }
    }

    @Override
    public TagListModel flush() {
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
        @Source("org/ovirt/engine/ui/webadmin/css/AssignTagTree.css")
        TableStyle cellTreeStyle();

    }

}
