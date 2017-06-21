package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.dispatch.annotation.GenEvent;

public class TagModelProvider extends DataBoundTabModelProvider<TagModel, TagListModel> {

    @GenEvent
    public class TagActivationChange {

        List<TagModel> activeTags;

    }

    private final Provider<TagPopupPresenterWidget> popupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public TagModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<TagPopupPresenterWidget> tagPopupPresenterWidgetProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.popupProvider = tagPopupPresenterWidgetProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    protected void initializeModelHandlers(final TagListModel model) {
        super.initializeModelHandlers(model);

        model.getSelectedItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                // The selectedItemsChangedEvent only gets fired when activating/deactiving a tag. Changing
                // the selection in the tag tree does not fire this event.
                TagActivationChangeEvent.fire(TagModelProvider.this, activeTagList(model.getRootNode()));
            }

            private List<TagModel> activeTagList(TagModel model) {
                List<TagModel> result = new ArrayList<>();
                if (model.getSelection()) {
                    result.add(model);
                }
                for (TagModel child : model.getChildren()) {
                    result.addAll(activeTagList(child));
                }
                return result;
            }

        });
        getModel().getSelectionModel().addSelectionChangeHandler(event -> {
            TagModelProvider.this.setSelectedItems(getModel().getSelectionModel().getSelectedObjects());
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
