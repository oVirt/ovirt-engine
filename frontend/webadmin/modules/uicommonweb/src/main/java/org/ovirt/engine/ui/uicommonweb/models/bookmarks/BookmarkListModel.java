package org.ovirt.engine.ui.uicommonweb.models.bookmarks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class BookmarkListModel extends SearchableListModel
{

    private static class BookmarksComparator implements Comparator<Bookmark>, Serializable {

        @Override
        public int compare(Bookmark o1, Bookmark o2) {
            String name1 = o1.getbookmark_name();
            String name2 = o2.getbookmark_name();
            if (name1 == null || name2 == null) {
                throw new IllegalArgumentException("Bookmark name cannot be null"); //$NON-NLS-1$
            }
            return name1.compareTo(name2);
        }
    }

    private static final BookmarksComparator COMPARATOR = new BookmarksComparator();

    public static EventDefinition NavigatedEventDefinition;
    private Event privateNavigatedEvent;

    public Event getNavigatedEvent()
    {
        return privateNavigatedEvent;
    }

    private void setNavigatedEvent(Event value)
    {
        privateNavigatedEvent = value;
    }

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private boolean privateIsBookmarkInitiated;

    public boolean getIsBookmarkInitiated()
    {
        return privateIsBookmarkInitiated;
    }

    private void setIsBookmarkInitiated(boolean value)
    {
        privateIsBookmarkInitiated = value;
    }

    static
    {
        NavigatedEventDefinition = new EventDefinition("Navigated", BookmarkListModel.class); //$NON-NLS-1$
    }

    public BookmarkListModel()
    {
        setNavigatedEvent(new Event(NavigatedEventDefinition));

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        setIsTimerDisabled(true);

        UpdateActionAvailability();
    }

    public void executeBookmarksSearch() {
        setIsBookmarkInitiated(true);
        getSearchCommand().Execute();
        setIsBookmarkInitiated(false);
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel bookmarkListModel = (BookmarkListModel) model;
                List<Bookmark> resultList = (List<Bookmark>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                if (resultList != null) {
                    Collections.sort(resultList, COMPARATOR);
                }

                // Prevent bookmark list updates from clearing selected bookmark
                setIsBookmarkInitiated(true);
                bookmarkListModel.setItems(resultList);
                setIsBookmarkInitiated(false);
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllBookmarks, new VdcQueryParametersBase(), _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBookmarksTitle());
        model.setHashName("remove_bookmark"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().bookmarsMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            org.ovirt.engine.core.common.businessentities.Bookmark i =
                    (org.ovirt.engine.core.common.businessentities.Bookmark) item;
            list.add(i.getbookmark_name());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnRemove()
    {

        Bookmark selectedBookmark = (Bookmark) getSelectedItem();
        BookmarksParametersBase parameters = new BookmarksParametersBase(selectedBookmark.getbookmark_id());

        IFrontendActionAsyncCallback async = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                PostOnSave(result.getReturnValue());
            }
        };

        getWindow().StartProgress(null);

        Frontend.RunAction(VdcActionType.RemoveBookmark, parameters, async);
    }

    public void Edit()
    {
        org.ovirt.engine.core.common.businessentities.Bookmark bookmark =
                (org.ovirt.engine.core.common.businessentities.Bookmark) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        BookmarkModel model = new BookmarkModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editBookmarkTitle());
        model.setHashName("edit_bookmark"); //$NON-NLS-1$
        model.setIsNew(false);
        model.getName().setEntity(bookmark.getbookmark_name());
        model.getSearchString().setEntity(bookmark.getbookmark_value());

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        BookmarkModel model = new BookmarkModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newBookmarkTitle());
        model.setHashName("new_bookmark"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getSearchString().setEntity(getSearchString());

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnSave()
    {
        BookmarkModel model = (BookmarkModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        org.ovirt.engine.core.common.businessentities.Bookmark tempVar =
                new org.ovirt.engine.core.common.businessentities.Bookmark();
        tempVar.setbookmark_id(model.getIsNew() ? (Guid) NGuid.Empty
                : ((org.ovirt.engine.core.common.businessentities.Bookmark) getSelectedItem()).getbookmark_id());
        tempVar.setbookmark_name((String) model.getName().getEntity());
        tempVar.setbookmark_value((String) model.getSearchString().getEntity());
        org.ovirt.engine.core.common.businessentities.Bookmark bookmark = tempVar;

        model.StartProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddBookmark : VdcActionType.UpdateBookmark,
                new BookmarksOperationParameters(bookmark),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        BookmarkListModel localModel = (BookmarkListModel) result.getState();
                        localModel.PostOnSave(result.getReturnValue());

                    }
                },
                this);
    }

    public void PostOnSave(VdcReturnValueBase returnValue)
    {
        getWindow().StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
            // Cancel() triggers a force refresh
            // getSearchCommand().Execute();
        }
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        UpdateActionAvailability();

        if (getSelectedItem() != null && !getIsBookmarkInitiated())
        {
            // Don't fire navigation events in response to the bookmark list updating itself
            setIsBookmarkInitiated(true);
            getNavigatedEvent().raise(this,
                    new BookmarkEventArgs((org.ovirt.engine.core.common.businessentities.Bookmark) getSelectedItem()));
            setIsBookmarkInitiated(false);
        }
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "BookmarkListModel"; //$NON-NLS-1$
    }
}
