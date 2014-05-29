package org.ovirt.engine.ui.uicommonweb.models.vms;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.HasAdElementReconnectPermissionParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class ConsoleModel extends EntityModel<VM> {
    public static final String GET_ATTACHMENT_SERVLET_URL = BaseContextPathData.getInstance().getPath()
            + "services/attachment/"; //$NON-NLS-1$

    protected static final int TICKET_VALIDITY_SECONDS = 120;

    private static String EJECT_LABEL;

    public static String getEjectLabel() {
        if (EJECT_LABEL == null) {
            EJECT_LABEL = "[" + ConstantsManager.getInstance().getConstants().eject() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return EJECT_LABEL;
    }

    public static final EventDefinition errorEventDefinition;
    private Event<ErrorCodeEventArgs> privateErrorEvent;

    public Event<ErrorCodeEventArgs> getErrorEvent()
    {
        return privateErrorEvent;
    }

    private void setErrorEvent(Event<ErrorCodeEventArgs> value)
    {
        privateErrorEvent = value;
    }

    private UICommand privateConnectCommand;

    public UICommand getConnectCommand()
    {
        return privateConnectCommand;
    }

    private void setConnectCommand(UICommand value)
    {
        privateConnectCommand = value;
    }

    private boolean isConnected;

    public boolean getIsConnected()
    {
        return isConnected;
    }

    public void setIsConnected(boolean value)
    {
        if (isConnected != value)
        {
            isConnected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsConnected")); //$NON-NLS-1$
        }
    }

    private boolean forceVmStatusUp;

    public boolean getForceVmStatusUp()
    {
        return forceVmStatusUp;
    }

    public void setForceVmStatusUp(boolean value)
    {
        if (forceVmStatusUp != value)
        {
            forceVmStatusUp = value;
            onPropertyChanged(new PropertyChangedEventArgs("ForceVmStatusUp")); //$NON-NLS-1$
        }
    }

    @Override
    public VM getEntity()
    {
        return (VM) super.getEntity();
    }

    /**
     * This attribute is a workaround for displaying popup dialogs
     * in console models.
     */
    private final Model parentModel;

    protected Model getParentModel() {
        return parentModel;
    }

    static
    {
        errorEventDefinition = new EventDefinition("Error", ConsoleModel.class); //$NON-NLS-1$
    }

    protected ConsoleModel(VM myVm, Model parentModel) {
        this.parentModel = parentModel;
        setEntity(myVm);

        setErrorEvent(new Event<ErrorCodeEventArgs>(errorEventDefinition));

        setConnectCommand(new UICommand("Connect", this)); //$NON-NLS-1$
    }

    protected abstract void connect();

    public abstract boolean canBeSelected();

    public boolean canConnect() {
        if (!canBeSelected()) { // cannot be even selected
            return false;
        }

        return getForceVmStatusUp()
                ? getEntity().getStatus() == VMStatus.Up
                : isVmRunning();
    }

    private boolean isVmRunning() {
        switch (getEntity().getStatus()) {
            case PoweringUp:
            case Up:
            case RebootInProgress:
            case PoweringDown:
            case Paused:
                return true;

            default:
                return false;
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getConnectCommand()) {
            connect();
        }
    }

    /**
     * Executes given command. The confirmation dialog is displayed when it's
     * not safe to take over the console, which is when
     *  - allow console reconnect is disabled AND
     *  - there is an active console user who is different from current portal user and has not reconnect permissions AND
     *  - current portal user has not reconnect permissions (this is to prevent extra information dialog. backend
     *    validation will not allow connecting this user and frontend will display warning message anyway)
     *
     * @param command
     */
    protected void executeCommandWithConsoleSafenessWarning(final UICommand command) {
        VM vm = getEntity();
        if (vm.getAllowConsoleReconnect() || vm.getConsoleCurentUserName() == null ||
            Frontend.getInstance().getLoggedInUser().getId().equals(vm.getConsoleUserId())) {
            command.execute();
            return;
        }

        //now we ask if the currently connected user has permission to reconnect (async)
        final HasAdElementReconnectPermissionParameters consoleUserReconnectPermParams =
                new HasAdElementReconnectPermissionParameters(vm.getConsoleUserId(),
                        vm.getId());

        final HasAdElementReconnectPermissionParameters portalUserReconnectPermParams =
                new HasAdElementReconnectPermissionParameters(Frontend.getInstance().getLoggedInUser().getId(),
                        vm.getId());

        final AsyncQuery portalUserReconnectPermissionQuery = new AsyncQuery();
        portalUserReconnectPermissionQuery.setModel(this);
        portalUserReconnectPermissionQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                boolean returnValue = ((VdcQueryReturnValue)result).getReturnValue();
                if (returnValue) {
                    displayConsoleConnectConfirmPopup(command);
                } else {
                    command.execute(); //user will be stopped by backend validation
                }
            }
        };

        final AsyncQuery consoleUserReconnectPermissionQuery = new AsyncQuery();
        consoleUserReconnectPermissionQuery.setModel(this);
        consoleUserReconnectPermissionQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                boolean returnValue = ((VdcQueryReturnValue)result).getReturnValue();
                if (returnValue) {
                    command.execute();
                } else {
                    Frontend.getInstance().runQuery(VdcQueryType.HasAdElementReconnectPermission, portalUserReconnectPermParams, portalUserReconnectPermissionQuery);
                }
            }
        };

        Frontend.getInstance().runQuery(VdcQueryType.HasAdElementReconnectPermission, consoleUserReconnectPermParams, consoleUserReconnectPermissionQuery);
    }

    private void displayConsoleConnectConfirmPopup(final UICommand onConfirmCommand) {
        ConfirmationModel model = new ConfirmationModel();
        parentModel.setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmConsoleConnect());
        model.setHelpTag(HelpTag.confirm_console_connect);
        model.setHashName("confirm_console_connect"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().confirmConsoleConnectMessage());

        UICommand confirmAndCloseCommand = new UICommand("Confirm", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                onConfirmCommand.execute();
                parentModel.setWindow(null);
            }
        });
        confirmAndCloseCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        confirmAndCloseCommand.setIsDefault(true);
        model.getCommands().add(confirmAndCloseCommand);

        UICommand cancelCommand = new UICommand("Cancel", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                parentModel.setWindow(null);
            }
        });
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    public static void makeConsoleConfigRequest(String fileName, String contentType, String configFileContent) {
        final FlowPanel innerPanel = new FlowPanel();
        innerPanel.add(buildTextArea("contenttype", contentType));//$NON-NLS-1$
        innerPanel.add(buildTextArea("content", configFileContent));//$NON-NLS-1$
        innerPanel.add(buildTextArea("encodingtype", "plain"));//$NON-NLS-1$ $NON-NLS-2$

        final FormPanel formPanel = new FormPanel(); //$NON-NLS-1$
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.getElement().setId("conform" + Double.valueOf(Math.random()).toString());//$NON-NLS-1$
        formPanel.setWidget(innerPanel);
        formPanel.setAction(GET_ATTACHMENT_SERVLET_URL + fileName);
        formPanel.setEncoding(FormPanel.ENCODING_URLENCODED);
        formPanel.setVisible(false);

        // clean-up after form submit
        formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                RootPanel.get().remove(formPanel);
            }
        });

        RootPanel.get().add(formPanel);
        FormElement.as(formPanel.getElement()).submit();
    }

    private static TextArea buildTextArea(String name, String value) {
        TextArea textArea = new TextArea();
        textArea.setName(name);
        textArea.setValue(value);

        return textArea;
    }

    protected String getToggleFullScreenKeys() {
        return (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ConsoleToggleFullScreenKeys);
    }

    protected String getReleaseCursorKeys() {
        return (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ConsoleReleaseCursorKeys);
    }

    protected String getClientTitle() {
        String releaseCursorKeys = getReleaseCursorKeys();
        String releaseCursorKeysTranslated =
                AsyncDataProvider.getInstance().getComplexValueFromSpiceRedKeysResource((releaseCursorKeys != null) ? releaseCursorKeys
                                                                                                : "shift+f12"); //$NON-NLS-1$

        String releaseCursorMsg = ""; //$NON-NLS-1$

        if (!StringHelper.isNullOrEmpty(releaseCursorKeysTranslated)) {
            releaseCursorMsg = " - " + ConstantsManager.getInstance() //$NON-NLS-1$
                    .getMessages().pressKeyToReleaseCursor(releaseCursorKeysTranslated);
        }

        return getEntity().getName() + ":%d" + releaseCursorMsg; //$NON-NLS-1$
    }

    protected Integer extractDisplayPort(GraphicsType graphicsType) {
        GraphicsInfo graphicsInfo = getEntity().getGraphicsInfos().get(graphicsType);
        if (graphicsInfo != null && graphicsInfo.getPort() != null) {
            return graphicsInfo.getPort();
        }
        return 0;
    }
}
