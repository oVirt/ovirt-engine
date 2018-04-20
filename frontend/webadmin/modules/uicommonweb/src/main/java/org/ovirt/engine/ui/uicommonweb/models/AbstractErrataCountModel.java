package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.HasErrata;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

/**
 * Model object representing counts (summary info) about errata for a VM or a Host.
 *
 * @see org.ovirt.engine.core.common.businessentities.Erratum
 * @see ErrataCounts
 * @see HostErrataCountModel
 * @see VmErrataCountModel
 */
public abstract class AbstractErrataCountModel extends EntityModel<HasErrata> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    public static final String SHOW_SECURITY_COMMAND = "ShowSecurityErrata"; //$NON-NLS-1$
    public static final String SHOW_BUGS_COMMAND = "ShowBugsErrata"; //$NON-NLS-1$
    public static final String SHOW_ENHANCEMENTS_COMMAND = "ShowEnhancementsErrata"; //$NON-NLS-1$

    protected static final String CLOSE = "Close"; //$NON-NLS-1$

    // commands that can be performed on this Model
    private UICommand showSecurityCommand;
    private UICommand showBugsCommand;
    private UICommand showEnhancementsCommand;

    private EntityModel<ErrataCounts> errataCounts;

    private Guid guid;

    private String filterCommand;

    public AbstractErrataCountModel() {
        showSecurityCommand = new UICommand(SHOW_SECURITY_COMMAND, this);
        showBugsCommand = new UICommand(SHOW_BUGS_COMMAND, this);
        showEnhancementsCommand = new UICommand(SHOW_ENHANCEMENTS_COMMAND, this);

        errataCounts = new EntityModel<>();
    }

    @Override
    public void executeCommand(UICommand command) {
        if (CLOSE.equals(command.getName())) {
            cancel();

        } else if (SHOW_SECURITY_COMMAND.equals(command.getName()) ||
                SHOW_BUGS_COMMAND.equals(command.getName()) ||
                SHOW_ENHANCEMENTS_COMMAND.equals(command.getName())) {
            showErrataListWithDetailsPopup(command.getName());
        } else {
            super.executeCommand(command);
        }
    }

    private void cancel() {
        setWindow(null);
    }

    public UICommand getShowSecurityCommand() {
        return showSecurityCommand;
    }

    public UICommand getShowBugsCommand() {
        return showBugsCommand;
    }

    public UICommand getShowEnhancementsCommand() {
        return showEnhancementsCommand;
    }

    public void setErrataCounts(ErrataCounts errataCounts) {
        this.errataCounts.setEntity(errataCounts);
        // ^ publishes an EntityChange event, bus notifies the Presenter
    }

    public ErrataCounts getErrataCounts() {
        return this.errataCounts.getEntity();
    }

    public void addErrataCountsChangeListener(IEventListener<? super EventArgs> listener) {
        this.errataCounts.getEntityChangedEvent().addListener(listener);
    }

    public void addPropertyChangeListener(IEventListener<PropertyChangedEventArgs> listener) {
        getPropertyChangedEvent().addListener(listener);
    }

    public void runQuery(Guid guid) {
        startProgress("getCount"); //$NON-NLS-1$
        AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<>(returnValue -> {
            stopProgress();
            ErrataCounts resultEntity = returnValue.getReturnValue();
            //Set message to null to make sure the actual setMessage creates an event.
            setMessage(null);
            if (resultEntity != null && returnValue.getSucceeded()) {
                setErrataCounts(resultEntity);
            } else {
                setMessage(
                        constants.katelloProblemRetrievingErrata()  + " " + returnValue.getExceptionMessage()); //$NON-NLS-1$
            }
        });
        asyncQuery.setHandleFailure(true);
        Frontend.getInstance().runQuery(getQueryType(), new GetErrataCountsParameters(guid), asyncQuery);
    }

    public void setGuid(Guid id) {
        this.guid = id;
    }

    public Guid getGuid() {
        return guid;
    }

    protected void initCommands(Model m) {
        UICommand closeCommand = UICommand.createDefaultOkUiCommand(CLOSE, this);
        closeCommand.setIsCancel(true);
        m.getCommands().add(closeCommand);
    }

    @Override
    public String getHashName() {
        return "errata"; //$NON-NLS-1$
    }

    public String getFilterCommand() {
        return filterCommand;
    }

    public void setFilterCommand(String filterCommand) {
        this.filterCommand = filterCommand;
    }

    protected abstract QueryType getQueryType();

    protected abstract void showErrataListWithDetailsPopup(String filterCommand);

    protected void showErrataListWithDetailsPopup(String filterCommand, String title) {
        if (getWindow() != null) {
            return;
        }

        HostErrataCountModel transferObj = new HostErrataCountModel();
        transferObj.setFilterCommand(filterCommand);
        transferObj.setTitle(title);
        transferObj.setGuid(getGuid());

        setWindow(transferObj);
        initCommands(transferObj);
    }
}
