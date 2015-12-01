package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class BookmarkModelProvider extends DataBoundTabModelProvider<Bookmark, BookmarkListModel> {

    private final SingleSelectionModel<Bookmark> selectionModel;

    private final Provider<BookmarkPopupPresenterWidget> popupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    private final SystemTreeModelProvider treeModelProvider;
    private final TagModelProvider tagModelProvider;

    @Inject
    private Provider<CommonModel> commonModelProvider;

    @Inject
    public BookmarkModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<BookmarkPopupPresenterWidget> bookmarkPopupPresenterWidgetProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            SystemTreeModelProvider treeModelProvider, TagModelProvider tagModelProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.popupProvider = bookmarkPopupPresenterWidgetProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
        this.treeModelProvider = treeModelProvider;
        this.tagModelProvider = tagModelProvider;

        // Create selection model
        selectionModel = new SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Bookmark selectedObject = selectionModel.getSelectedObject();
                List<Bookmark> selectedItems = selectedObject != null
                        ? new ArrayList<>(Arrays.asList(selectedObject))
                        : new ArrayList<Bookmark>();
                BookmarkModelProvider.this.setSelectedItems(selectedItems);
            }
        });
    }

    @Override
    protected void initializeModelHandlers(BookmarkListModel model) {
        super.initializeModelHandlers(model);

        // Clear selection when a system tree node is selected
        treeModelProvider.getModel().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (treeModelProvider.getModel().getSelectedItem() != null) {
                    clearSelection();
                }
            }
        });

        // Clear selection when a tag tree node is pinned
        tagModelProvider.getModel().getSelectedItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (tagModelProvider.getModel().getSelectedItems() != null
                        && !tagModelProvider.getModel().getSelectedItems().isEmpty()) {
                    clearSelection();
                }
            }
        });

        // Clear selection when a new tab is selected
        commonModelProvider.get().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (commonModelProvider.get().getSelectedItem() != null) {
                    clearSelection();
                }
            }
        });

        // Clear selection when the search string is updated
        commonModelProvider.get().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender,
                    PropertyChangedEventArgs args) {
                if ("SearchString".equals(args.propertyName)) { //$NON-NLS-1$
                    clearSelection();
                }
            }
        });

        // Clear tag selection when a tag is saved/edited/deleted
        model.getItemSavedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                clearSelection();
            }
        });

    }

    void clearSelection() {
        if (selectionModel.getSelectedObject() != null && !getModel().getIsBookmarkInitiated()) {
            selectionModel.setSelected(selectionModel.getSelectedObject(), false);
        }
    }

    @Override
    protected void updateDataProvider(List<Bookmark> items) {
        super.updateDataProvider(items);
    }

    public SingleSelectionModel<Bookmark> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void addDataDisplay(HasData<Bookmark> display) {
        super.addDataDisplay(display);
        display.setSelectionModel(selectionModel);
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(BookmarkListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewCommand()
                || lastExecutedCommand == getModel().getEditCommand()) {
            return popupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(BookmarkListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
