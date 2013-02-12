package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.tags.TagItemCell;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TagModelProvider extends DataBoundTabModelProvider<TagModel, TagListModel> implements SearchableTreeModelProvider<TagModel, TagListModel> {

    private final DefaultSelectionEventManager<TagModel> selectionManager =
            DefaultSelectionEventManager.createDefaultManager();
    private final SingleSelectionModel<TagModel> selectionModel;

    private final Provider<TagPopupPresenterWidget> popupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    private final ApplicationResources resources;
    private final ApplicationTemplates templates;

    private CellTree display;

    @Inject
    public TagModelProvider(ClientGinjector ginjector) {
        super(ginjector);
        this.resources = ginjector.getApplicationResources();
        this.templates = ginjector.getApplicationTemplates();

        this.popupProvider = ginjector.getTagPopupPresenterWidgetProvider();
        this.removeConfirmPopupProvider = ginjector.getRemoveConfirmPopupProvider();

        // Create selection model
        selectionModel = new SingleSelectionModel<TagModel>();
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                TagModelProvider.this.setSelectedItems(Arrays.asList(selectionModel.getSelectedObject()));
            }
        });
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        // Add model reset handler
        getModel().getResetRequestedEvent().addListener(new IEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                Iterator<TagModel> iterator = getModel().getItems().iterator();
                if (iterator.hasNext()) {
                    TagModel root = getModel().CloneTagModel(iterator.next());
                    updateDataProvider(Arrays.asList(root));
                }
            }
        });
    }

    @Override
    protected void updateDataProvider(List<TagModel> items) {
        // Update data provider only for non-empty data
        if (!items.isEmpty()) {
            super.updateDataProvider(items);
            expandTree();
        }
    }

    private void expandTree() {
        if (display != null) {
            expandTree(display.getRootTreeNode());
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

    public void setDisplay(CellTree display) {
        this.display = display;
        expandTree();
    }

    @Override
    public TagListModel getModel() {
        return getCommonModel().getTagList();
    }

    @Override
    public void setSelectedItems(List<TagModel> items) {
        getModel().setSelectedItem(items.size() > 0 ? items.get(0) : null);
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T parent) {
        TagItemCell cell = new TagItemCell(resources, templates);

        if (parent != null) {
            // Not a root node
            TagModel parentModel = (TagModel) parent;
            List<TagModel> children = parentModel.getChildren();
            return new DefaultNodeInfo<TagModel>(new ListDataProvider<TagModel>(children),
                    cell, selectionModel, selectionManager, null);
        } else {
            // This is the root node
            return new DefaultNodeInfo<TagModel>(getDataProvider(),
                    cell, selectionModel, selectionManager, null);
        }
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value != null) {
            TagModel itemModel = (TagModel) value;
            List<TagModel> children = itemModel.getChildren();

            if (children != null) {
                return children.isEmpty();
            }
        }

        return false;
    }

    public SingleSelectionModel<TagModel> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TagListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewCommand()
                || lastExecutedCommand == getModel().getEditCommand()) {
            return popupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(TagListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
