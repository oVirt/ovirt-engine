package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.common.widget.tree.TreeModelWithElementId;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.tags.TagItemCell;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TagModelProvider extends DataBoundTabModelProvider<TagModel, TagListModel>
        implements SearchableTreeModelProvider<TagModel, TagListModel>, TreeModelWithElementId {

    private final DefaultSelectionEventManager<TagModel> selectionManager =
            DefaultSelectionEventManager.createDefaultManager();
    private final SingleSelectionModel<TagModel> selectionModel;

    private final Provider<TagPopupPresenterWidget> popupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    private final TagItemCell cell;

    @Inject
    public TagModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<TagPopupPresenterWidget> tagPopupPresenterWidgetProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.cell = new TagItemCell();
        this.popupProvider = tagPopupPresenterWidgetProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;

        // Create selection model
        selectionModel = new SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                TagModelProvider.this.setSelectedItems(Arrays.asList(selectionModel.getSelectedObject()));
            }
        });
    }

    @Override
    protected void initializeModelHandlers(final TagListModel model) {
        super.initializeModelHandlers(model);

        // Add model reset handler
        model.getResetRequestedEvent().addListener(new IEventListener<EventArgs>() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (model.getItems() == null) {
                    return;
                }
                Iterator<TagModel> iterator = model.getItems().iterator();
                if (iterator.hasNext()) {
                    TagModel root = model.cloneTagModel(iterator.next());
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
        }
    }

    @Override
    public void setSelectedItems(List<TagModel> items) {
        getModel().setSelectedItem(items.size() > 0 ? items.get(0) : null);
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T parent) {
        if (parent != null) {
            // Not a root node
            TagModel parentModel = (TagModel) parent;
            List<TagModel> children = parentModel.getChildren();
            return new DefaultNodeInfo<>(new ListDataProvider<>(children), cell, selectionModel, selectionManager, null);
        } else {
            // This is the root node
            return new DefaultNodeInfo<>(getDataProvider(), cell, selectionModel, selectionManager, null);
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

    @Override
    public void setElementIdPrefix(String elementIdPrefix) {
        cell.setElementIdPrefix(elementIdPrefix);
    }

}
