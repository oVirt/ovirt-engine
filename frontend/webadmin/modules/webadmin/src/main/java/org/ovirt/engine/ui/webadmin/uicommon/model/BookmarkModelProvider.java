package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class BookmarkModelProvider extends DataBoundTabModelProvider<Bookmark, BookmarkListModel> {

    private final Provider<BookmarkPopupPresenterWidget> popupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public BookmarkModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<BookmarkPopupPresenterWidget> bookmarkPopupPresenterWidgetProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.popupProvider = bookmarkPopupPresenterWidgetProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    protected void initializeModelHandlers(BookmarkListModel model) {
        super.initializeModelHandlers(model);

        // Clear tag selection when a tag is saved/edited/deleted
        model.getItemSavedEvent().addListener((ev, sender, args) -> clearSelection());
        getModel().getSelectionModel().addSelectionChangeHandler(event -> {
            BookmarkModelProvider.this.getModel().setSelectedItem(getModel().getSelectionModel().getFirstSelectedObject());
        });
    }

    void clearSelection() {
        getModel().getSelectionModel().clear();
    }

    @Override
    protected void updateDataProvider(List<Bookmark> items) {
        super.updateDataProvider(items);
    }

    @Override
    public void addDataDisplay(HasData<Bookmark> display) {
        super.addDataDisplay(display);
        display.setSelectionModel(getModel().getSelectionModel());
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
