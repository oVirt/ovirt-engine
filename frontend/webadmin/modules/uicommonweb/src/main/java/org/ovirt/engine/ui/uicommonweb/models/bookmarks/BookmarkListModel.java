package org.ovirt.engine.ui.uicommonweb.models.bookmarks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class BookmarkListModel extends SearchableListModel<Bookmark, Bookmark> {

    private static class BookmarksComparator implements Comparator<Bookmark>, Serializable {

        @Override
        public int compare(Bookmark o1, Bookmark o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();
            if (name1 == null || name2 == null) {
                throw new IllegalArgumentException("Bookmark name cannot be null"); //$NON-NLS-1$
            }
            return name1.compareTo(name2);
        }
    }

    private static final BookmarksComparator COMPARATOR = new BookmarksComparator();

    public static final EventDefinition navigatedEventDefinition;

    public static final EventDefinition savedEventDefinition;

    private Event<EventArgs> privateNavigatedEvent;

    public Event<EventArgs> getNavigatedEvent() {
        return privateNavigatedEvent;
    }

    private void setNavigatedEvent(Event<EventArgs> value) {
        privateNavigatedEvent = value;
    }

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private boolean privateIsBookmarkInitiated;

    public boolean getIsBookmarkInitiated() {
        return privateIsBookmarkInitiated;
    }

    private void setIsBookmarkInitiated(boolean value) {
        privateIsBookmarkInitiated = value;
    }

    private Event<EventArgs> privateItemSavedEvent;

    public Event<EventArgs> getItemSavedEvent() {
        return privateItemSavedEvent;
    }

    private void setItemSavedEvent(Event<EventArgs> value) {
        privateItemSavedEvent = value;
    }

    static {
        navigatedEventDefinition = new EventDefinition("Navigated", BookmarkListModel.class); //$NON-NLS-1$
        savedEventDefinition = new EventDefinition("Saved", BookmarkListModel.class); //$NON-NLS-1$
    }

    public BookmarkListModel() {
        setNavigatedEvent(new Event<>(navigatedEventDefinition));
        setItemSavedEvent(new Event<>(savedEventDefinition));

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        setIsTimerDisabled(true);

        updateActionAvailability();
    }

    @Override
    protected boolean isSingleSelectionOnly() {
        return true;
    }

    public void executeBookmarksSearch() {
        setIsBookmarkInitiated(true);
        getSearchCommand().execute();
        setIsBookmarkInitiated(false);
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        Frontend.getInstance().runQuery(QueryType.GetAllBookmarks, new QueryParametersBase(), new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
            List<Bookmark> resultList = returnValue.getReturnValue();
            if (resultList != null) {
                Collections.sort(resultList, COMPARATOR);
            }

            // Prevent bookmark list updates from clearing selected bookmark
            setIsBookmarkInitiated(true);
            setItems(resultList);
            setIsBookmarkInitiated(false);
        }));
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBookmarksTitle());
        model.setHelpTag(HelpTag.remove_bookmark);
        model.setHashName("remove_bookmark"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        list.add(((Bookmark) getSelectedItem()).getName());
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onRemove() {

        Bookmark selectedBookmark = (Bookmark) getSelectedItem();
        BookmarksParametersBase parameters = new BookmarksParametersBase(selectedBookmark.getId());

        IFrontendActionAsyncCallback async = result -> postOnSave(result.getReturnValue());

        getWindow().startProgress();

        Frontend.getInstance().runAction(ActionType.RemoveBookmark, parameters, async);
    }

    public void edit() {
        org.ovirt.engine.core.common.businessentities.Bookmark bookmark =
                (org.ovirt.engine.core.common.businessentities.Bookmark) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        BookmarkModel model = new BookmarkModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editBookmarkTitle());
        model.setHelpTag(HelpTag.edit_bookmark);
        model.setHashName("edit_bookmark"); //$NON-NLS-1$
        model.setIsNew(false);
        model.getName().setEntity(bookmark.getName());
        model.getSearchString().setEntity(bookmark.getValue());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        BookmarkModel model = new BookmarkModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newBookmarkTitle());
        model.setHelpTag(HelpTag.new_bookmark);
        model.setHashName("new_bookmark"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getSearchString().setEntity(getSearchString());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onSave() {
        BookmarkModel model = (BookmarkModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        org.ovirt.engine.core.common.businessentities.Bookmark tempVar =
                new org.ovirt.engine.core.common.businessentities.Bookmark();
        tempVar.setId(model.getIsNew() ? Guid.Empty
                : ((org.ovirt.engine.core.common.businessentities.Bookmark) getSelectedItem()).getId());
        tempVar.setName(model.getName().getEntity());
        tempVar.setValue(model.getSearchString().getEntity());
        org.ovirt.engine.core.common.businessentities.Bookmark bookmark = tempVar;

        model.startProgress();

        Frontend.getInstance().runAction(model.getIsNew() ? ActionType.AddBookmark : ActionType.UpdateBookmark,
                new BookmarksOperationParameters(bookmark),
                result -> {

                    BookmarkListModel localModel = (BookmarkListModel) result.getState();
                    localModel.postOnSave(result.getReturnValue());

                },
                this);
    }

    public void postOnSave(ActionReturnValue returnValue) {
        getWindow().stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
            //Refresh the bookmarks.
            getSearchCommand().execute();
        }

        privateItemSavedEvent.raise(this, EventArgs.EMPTY);
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();

        if (getSelectedItem() != null && !getIsBookmarkInitiated()) {
            // Don't fire navigation events in response to the bookmark list updating itself
            setIsBookmarkInitiated(true);
            getNavigatedEvent().raise(this,
                    new BookmarkEventArgs((org.ovirt.engine.core.common.businessentities.Bookmark) getSelectedItem()));
            setIsBookmarkInitiated(false);
        }
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "BookmarkListModel"; //$NON-NLS-1$
    }
}
