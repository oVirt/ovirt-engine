package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class SessionListModel extends ListWithSimpleDetailsModel<UserSession, UserSession>
        implements ISupportSystemTreeContext {

    private static final String CMD_TERMINATE = "Terminate"; //$NON-NLS-1$

    private UICommand terminateCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    @Inject
    public SessionListModel() {

        setTitle(ConstantsManager.getInstance().getConstants().userSessionsTitle());
        setHelpTag(HelpTag.engine_sessions);
        setApplicationPlace(WebAdminApplicationPlaces.sessionMainTabPlace);
        setHashName("sessions"); //$NON-NLS-1$

        setDefaultSearchString("Session:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.SESSION_OBJ_NAME, SearchObjects.SESSION_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.AllModes);

        setTerminateCommand(new UICommand(CMD_TERMINATE, this));

        terminateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public UICommand getTerminateCommand() {
        return terminateCommand;
    }

    private void setTerminateCommand(UICommand value) {
        terminateCommand = value;
    }

    @Override
    protected String getListName() {
        return "SessionListModel"; //$NON-NLS-1$
    }

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        if (systemTreeSelectedItem != value) {
            systemTreeSelectedItem = value;
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged() {
        terminateActionAvailability();
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        terminateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        terminateActionAvailability();
    }

    private void terminateActionAvailability() {
        Collection<UserSession> selectedSession = getSelectedItems();
        Collection<UserSession> selectedItems = (selectedSession != null) ? selectedSession : new ArrayList<UserSession>();

        getTerminateCommand().setIsExecutionAllowed(!selectedItems.isEmpty());
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("session"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters searchParameters =
                new SearchParameters(applySortOptions(getSearchString()), SearchType.Session, isCaseSensitiveSearch());
        searchParameters.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, searchParameters);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    private void terminateSession() {
        if (getConfirmWindow() != null) {
            return;
        }
        final TerminateSessionsModel confirmWindow = new TerminateSessionsModel(this);
        setConfirmWindow(confirmWindow);
        confirmWindow.initialize();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getTerminateCommand()) {
            terminateSession();
        }
    }

}
