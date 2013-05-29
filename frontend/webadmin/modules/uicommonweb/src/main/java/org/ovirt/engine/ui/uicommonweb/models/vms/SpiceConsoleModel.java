package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class SpiceConsoleModel extends ConsoleModel implements IFrontendMultipleQueryAsyncCallback {

    public enum ClientConsoleMode { Native, Plugin, Auto, Html5 }

    public static EventDefinition SpiceDisconnectedEventDefinition;
    public static EventDefinition SpiceConnectedEventDefinition;
    public static EventDefinition SpiceMenuItemSelectedEventDefinition;
    public static EventDefinition UsbAutoShareChangedEventDefinition;
    public static EventDefinition wanColorDepthChangedEventDefinition;
    public static EventDefinition wanDisableEffectsChangeEventDefinition;

    private SpiceMenu menu;
    private ISpice privatespice;
    private ClientConsoleMode consoleMode;

    private void setspice(ISpice value) {
        privatespice = value;
    }

    public ClientConsoleMode getClientConsoleMode() {
        return consoleMode;
    }

    static {
        SpiceDisconnectedEventDefinition = new EventDefinition("SpiceDisconnected", SpiceConsoleModel.class); //$NON-NLS-1$
        SpiceConnectedEventDefinition = new EventDefinition("SpiceConnected", SpiceConsoleModel.class); //$NON-NLS-1$
        SpiceMenuItemSelectedEventDefinition = new EventDefinition("SpiceMenuItemSelected", SpiceConsoleModel.class); //$NON-NLS-1$
        UsbAutoShareChangedEventDefinition = new EventDefinition("UsbAutoShareChanged", SpiceConsoleModel.class); //$NON-NLS-1$
        wanColorDepthChangedEventDefinition = new EventDefinition("ColorDepthChanged", SpiceConsoleModel.class); //$NON-NLS-1$
        wanDisableEffectsChangeEventDefinition = new EventDefinition("DisableEffectsChange", SpiceConsoleModel.class); //$NON-NLS-1$
    }

    public SpiceConsoleModel() {
        setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());

        setSpiceImplementation(
                ClientConsoleMode.valueOf((String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ClientModeSpiceDefault)));
    }

    public ISpice getspice() {
        return privatespice;
    }

    /**
     * Sets implementation of ISpice which will be used (either implementation
     * that uses spice browser plugin or the one using configuration servlet)
     * and performs sets initial configuration as well (different for WA/UP).
     *
     * Default mode is "Auto" (spice browser plugin is used only if it is
     * installed).
     *
     */
    public void setSpiceImplementation(ClientConsoleMode consoleMode) {
        this.consoleMode = consoleMode;

        switch (consoleMode) {
            case Native:
                setspice((ISpice) TypeResolver.getInstance().resolve(ISpiceNative.class));
                break;
            case Plugin:
                setspice((ISpice) TypeResolver.getInstance().resolve(ISpicePlugin.class));
                break;
            case Html5:
                setspice((ISpice) TypeResolver.getInstance().resolve(ISpiceHtml5.class));
                break;
            default:
                ISpicePlugin pluginSpice = (ISpicePlugin) TypeResolver.getInstance().resolve(ISpicePlugin.class);
                setspice(pluginSpice.detectBrowserPlugin() ? pluginSpice
                        : (ISpice) TypeResolver.getInstance().resolve(ISpiceNative.class));
            break;
        }

        getConfigurator().configure(getspice());

        if (!getspice().getConnectedEvent().getListeners().contains(this)) {
            getspice().getConnectedEvent().addListener(this);
        }
    }

    @Override
    protected void connect() {
        if (getEntity() != null) {
            getLogger().debug("Connecting to Spice console..."); //$NON-NLS-1$
            if (!getspice().getIsInstalled()) {
                getLogger().info("Spice client is not installed."); //$NON-NLS-1$
                getspice().install();
                return;
            }

            // Check a spice version.
            if (getConfigurator().getIsAdmin()
                    && getspice().getCurrentVersion().compareTo(getspice().getDesiredVersion()) < 0)
            {
                getLogger().info("Spice client version is not as desired (" + getspice().getDesiredVersion() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                getspice().install();
                return;
            }

            // Don't connect if there VM is not running on any host.
            if (getEntity().getRunOnVds() == null)
            {
                return;
            }

            // If it is not windows or SPICE guest agent is not installed, make sure the WAN options are disabled.
            if (!AsyncDataProvider.isWindowsOsType(getEntity().getVmOsId()) || !getEntity().getHasSpiceDriver()) {
                getspice().setWanOptionsEnabled(false);
            }

            // make sure to not send the ctrl+alt+delete and TaskMgrExecution if not supported
            ConsoleUtils consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
            if (!consoleUtils.isCtrlAltDelEnabled()) {
                getspice().setSendCtrlAltDelete(false);
                getspice().setNoTaskMgrExecution(false);
            }

            UICommand setVmTicketCommand = new UICommand("setVmCommand", new BaseCommandTarget() { //$NON-NLS-1$
                @Override
                public void executeCommand(UICommand uiCommand) {
                    setVmTicket();
                }
            });
            executeCommandWithConsoleSafenessWarning(setVmTicketCommand);

        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.equals(getspice().getDisconnectedEvent())) {
            spice_Disconnected(sender, (ErrorCodeEventArgs) args);
        }
        else if (ev.equals(getspice().getConnectedEvent())) {
            spice_Connected(sender);
        }
        else if (ev.equals(getspice().getMenuItemSelectedEvent())) {
            spice_MenuItemSelected(sender, (SpiceMenuItemEventArgs) args);
        }
    }

    private void spice_MenuItemSelected(Object sender, SpiceMenuItemEventArgs e) {
        if (getEntity() != null) {
            // SpiceMenuCommandItem item = menu.Descendants()
            // .OfType<SpiceMenuCommandItem>()
            // .FirstOrDefault(a => a.Id == e.MenuItemId);
            SpiceMenuCommandItem item = null;
            for (SpiceMenuItem a : menu.descendants()) {
                if (a.getClass() == SpiceMenuCommandItem.class && a.getId() == e.getMenuItemId()) {
                    item = (SpiceMenuCommandItem) a;
                    break;
                }
            }
            if (item != null) {
                if (StringHelper.stringsEqual(item.getCommandName(), CommandPlay)) {
                    // use sysprep iff the vm is not initialized and vm has Win OS
                    boolean reinitialize =
                            !getEntity().isInitialized() && AsyncDataProvider.isWindowsOsType(getEntity().getVmOsId());
                    RunVmParams tempVar = new RunVmParams(getEntity().getId());
                    tempVar.setRunAsStateless(getEntity().isStateless());
                    tempVar.setReinitialize(reinitialize);
                    Frontend.RunMultipleAction(VdcActionType.RunVm,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { tempVar })));

                } else if (StringHelper.stringsEqual(item.getCommandName(), CommandSuspend)) {
                    Frontend.RunMultipleAction(VdcActionType.HibernateVm,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new VmOperationParameterBase(getEntity().getId()) })));

                } else if (StringHelper.stringsEqual(item.getCommandName(), CommandStop)) {
                    Frontend.RunMultipleAction(VdcActionType.ShutdownVm,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new ShutdownVmParameters(getEntity().getId(),
                                    true) })));

                } else if (StringHelper.stringsEqual(item.getCommandName(), CommandChangeCD))                 {
                    Frontend.RunMultipleAction(VdcActionType.ChangeDisk,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new ChangeDiskCommandParameters(getEntity().getId(),
                                    StringHelper.stringsEqual(item.getText(), EjectLabel) ? "" : item.getText()) }))); //$NON-NLS-1$
                }
            }
        }
    }

    private void spice_Disconnected(Object sender, ErrorCodeEventArgs e) {
        getspice().getDisconnectedEvent().removeListener(this);
        getspice().getMenuItemSelectedEvent().removeListener(this);

        setIsConnected(false);
        updateActionAvailability();

        if (e.getErrorCode() > 100) {
            getErrorEvent().raise(this, e);
        }
    }

    private void spice_Connected(Object sender) {
        setIsConnected(true);
        updateActionAvailability();
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) { //$NON-NLS-1$
            cancel();
        }
    }



    @Override
    protected void updateActionAvailability() {
        super.updateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(!getIsConnected() && getEntity() != null
                && getEntity().getDisplayType() == DisplayType.qxl
                && isVmConnectReady());
    }

    private void executeQuery(final VM vm) {
        AsyncQuery _asyncQuery0 = new AsyncQuery();
        _asyncQuery0.setModel(this);

        _asyncQuery0.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model0, Object result0) {
                SpiceConsoleModel thisSpiceConsoleModel = (SpiceConsoleModel) model0;
                VM thisVm = thisSpiceConsoleModel.getEntity();

                StorageDomain isoDomain = null;
                if (result0 != null) {
                    isoDomain = (StorageDomain) result0;
                }

                ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
                queryTypeList.add(VdcQueryType.GetConfigurationValue);
                queryTypeList.add(VdcQueryType.GetConfigurationValue);
                queryTypeList.add(VdcQueryType.GetConfigurationValue);
                queryTypeList.add(VdcQueryType.GetConfigurationValue);
                queryTypeList.add(VdcQueryType.GetVdsCertificateSubjectByVmId);
                queryTypeList.add(VdcQueryType.GetCACertificate);
                queryTypeList.add(VdcQueryType.GetConfigurationValue);
                queryTypeList.add(VdcQueryType.GetConfigurationValue);

                ArrayList<VdcQueryParametersBase> parametersList =
                        new ArrayList<VdcQueryParametersBase>();
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SSLEnabled, AsyncDataProvider.getDefaultConfigurationVersion()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.CipherSuite, AsyncDataProvider.getDefaultConfigurationVersion()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceSecureChannels,
                        thisVm.getVdsGroupCompatibilityVersion().toString()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.EnableSpiceRootCertificateValidation, AsyncDataProvider.getDefaultConfigurationVersion()));
                parametersList.add(new IdQueryParameters(thisVm.getId()));
                parametersList.add(new VdcQueryParametersBase());
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceToggleFullScreenKeys, AsyncDataProvider.getDefaultConfigurationVersion()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceReleaseCursorKeys,
                        AsyncDataProvider.getDefaultConfigurationVersion()));

                if (isoDomain != null) {
                    queryTypeList.add(VdcQueryType.GetImagesListByStoragePoolId);

                    GetImagesListByStoragePoolIdParameters getIsoParams =
                            new GetImagesListByStoragePoolIdParameters(vm.getStoragePoolId(), ImageFileType.ISO);
                    parametersList.add(getIsoParams);
                }

                Frontend.RunMultipleQueries(queryTypeList, parametersList, thisSpiceConsoleModel);
            }
        };

        AsyncDataProvider.getIsoDomainByDataCenterId(_asyncQuery0, vm.getStoragePoolId());
    }

    private String ticket;

    @Override
    public void executed(FrontendMultipleQueryAsyncResult result) {
        List<VdcQueryReturnValue> returnValues = result.getReturnValues();

        boolean success = true;
        for (VdcQueryReturnValue returnValue : returnValues) {
            if (!returnValue.getSucceeded()) {
                success = false;
                break;
            }
        }

        if (!success) {
            boolean enableSpiceRootCertificateValidation = (Boolean) result.getReturnValues().get(3).getReturnValue();
            VdcQueryReturnValue caCertificateReturnValue = result.getReturnValues().get(5);

            // If only the caCertificate query failed - ignore failure (goto onSuccess)
            if (!caCertificateReturnValue.getSucceeded() && !enableSpiceRootCertificateValidation) {
                // Verify that all queries (except caCertificate) succeeded
                // If succeeded goto 'onSuccess'; Otherwise, 'onFailure'.
                for (VdcQueryReturnValue returnValue : returnValues) {
                    if (!returnValue.getSucceeded() && returnValue != caCertificateReturnValue) {
                        return;
                    }
                }
            }
        }

        String cipherSuite = null;
        String spiceSecureChannels = null;

        boolean isSSLEnabled = (Boolean) returnValues.get(0).getReturnValue();
        if (isSSLEnabled) {
            cipherSuite = (String) returnValues.get(1).getReturnValue();
            spiceSecureChannels = (String) returnValues.get(2).getReturnValue();
        }

        String certificateSubject = ""; //$NON-NLS-1$
        String caCertificate = ""; //$NON-NLS-1$

        if ((Boolean) returnValues.get(3).getReturnValue()) {
            certificateSubject = (String) returnValues.get(4).getReturnValue();
            caCertificate = (String) returnValues.get(5).getReturnValue();
        }

        getspice().setHost(getEntity().getDisplayIp());
        getspice().setSmartcardEnabled(getEntity().isSmartcardEnabled());
        getspice().setPort((getEntity().getDisplay() == null ? 0 : getEntity().getDisplay()));
        getspice().setPassword(ticket);
        getspice().setNumberOfMonitors(getEntity().getNumOfMonitors());
        getspice().setGuestHostName(getEntity().getVmHost().split("[ ]", -1)[0]); //$NON-NLS-1$
        if (getEntity().getDisplaySecurePort() != null)
        {
            getspice().setSecurePort(getEntity().getDisplaySecurePort());
        }
        if (!StringHelper.isNullOrEmpty(spiceSecureChannels)) {
            getspice().setSslChanels(spiceSecureChannels);
        }
        if (!StringHelper.isNullOrEmpty(cipherSuite)) {
            getspice().setCipherSuite(cipherSuite);
        }

        getspice().setHostSubject(certificateSubject);
        getspice().setTrustStore(caCertificate);

        String toggleFullScreenKeys = (String) returnValues.get(6).getReturnValue();
        String releaseCursorKeys = (String) returnValues.get(7).getReturnValue();
        String ctrlAltDel = "ctrl+alt+del"; //$NON-NLS-1$
        String ctrlAltEnd = "ctrl+alt+end"; //$NON-NLS-1$

        String releaseCursorKeysTranslated =
                AsyncDataProvider.getComplexValueFromSpiceRedKeysResource((releaseCursorKeys != null) ? releaseCursorKeys
                        : "shift+f12"); //$NON-NLS-1$

        getspice().setTitle(getEntity().getName()
                + ":%d" //$NON-NLS-1$
                + (StringHelper.isNullOrEmpty(releaseCursorKeysTranslated) ? "" : (" - " + //$NON-NLS-1$ //$NON-NLS-2$
                        ConstantsManager.getInstance()
                                .getMessages()
                                .pressKeyToReleaseCursor(releaseCursorKeysTranslated))));

        String spiceProxy = (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.SpiceProxyDefault);
        boolean spiceProxyGloballyConfigured = spiceProxy != null && !"".equals(spiceProxy);
        boolean spiceProxyEnabledForThisVm = getspice().isSpiceProxyEnabled();
        spiceProxy = spiceProxyGloballyConfigured && spiceProxyEnabledForThisVm  ? spiceProxy : null; //$NON-NLS-1$
        getspice().setSpiceProxy(spiceProxy);

        // If 'AdminConsole' is true, send true; otherwise, false should be sent only for VMs with SPICE driver
        // installed.
        getspice().setAdminConsole(getConfigurator().getSpiceAdminConsole() ? true : !getEntity().getHasSpiceDriver());

        // Update 'UsbListenPort' value
        getspice().setUsbListenPort(getConfigurator().getIsUsbEnabled()
                && getEntity().getUsbPolicy() == UsbPolicy.ENABLED_LEGACY ? getConfigurator().getSpiceDefaultUsbPort()
                : getConfigurator().getSpiceDisableUsbListenPort());

        getspice().setToggleFullscreenHotKey(toggleFullScreenKeys);
        getspice().setReleaseCursorHotKey(releaseCursorKeys);

        getspice().setLocalizedStrings(new String[] {
                ConstantsManager.getInstance().getConstants().usb(),
                ConstantsManager.getInstance()
                        .getConstants()
                        .usbDevicesNoUsbdevicesClientSpiceUsbRedirectorNotInstalled() });

        // Create menu.
        int id = 1;
        menu = new SpiceMenu();

        SpiceMenuContainerItem changeCDItem =
                new SpiceMenuContainerItem(id, ConstantsManager.getInstance().getConstants().changeCd());
        id++;

        ArrayList<String> isos = new ArrayList<String>();

        if (returnValues.size() > 8) {
            ArrayList<RepoImage> repoList =
                    (ArrayList<RepoImage>) returnValues.get(8).getReturnValue();
            for (RepoImage repoImage : repoList) {
                isos.add(repoImage.getRepoImageId());
            }
        }

        isos =
                isos.size() > 0 ? isos
                        : new ArrayList<String>(Arrays.asList(new String[] { ConstantsManager.getInstance()
                                .getConstants()
                                .noCds() }));

        Collections.sort(isos);

        for (String fileName : isos) {
            changeCDItem.getItems().add(new SpiceMenuCommandItem(id, fileName, CommandChangeCD));
            id++;
        }
        changeCDItem.getItems().add(new SpiceMenuCommandItem(id, EjectLabel, CommandChangeCD));
        id++;

        menu.getItems().add(changeCDItem);
        menu.getItems().add(new SpiceMenuSeparatorItem(id));
        id++;
        menu.getItems().add(new SpiceMenuCommandItem(id, ConstantsManager.getInstance()
                .getConstants()
                .playSpiceConsole(), CommandPlay));
        id++;
        menu.getItems().add(new SpiceMenuCommandItem(id, ConstantsManager.getInstance()
                .getConstants()
                .suspendSpiceConsole(), CommandSuspend));
        id++;
        menu.getItems().add(new SpiceMenuCommandItem(id, ConstantsManager.getInstance()
                .getConstants()
                .stopSpiceConsole(), CommandStop));

        getspice().setMenu(menu.toString());

        getspice().setGuestID(getEntity().getId().toString());

        // Subscribe to events.
        getspice().getDisconnectedEvent().addListener(this);
        getspice().getMenuItemSelectedEvent().addListener(this);

        if (StringHelper.isNullOrEmpty(getEntity().getDisplayIp())
                || StringHelper.stringsEqual(getEntity().getDisplayIp(), "0")) //$NON-NLS-1$
        {
            determineIpAndConnect(getEntity().getId());
        }
        else {
            // Try to connect.
            spiceConnect();
        }
    }

    private void determineIpAndConnect(Guid vmId) {
        if (vmId == null) {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                SpiceConsoleModel spiceConsoleModel = (SpiceConsoleModel) model;
                String address =
                        (String) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                spiceConsoleModel.getspice().setHost(address);
                spiceConsoleModel.spiceConnect();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetManagementInterfaceAddressByVmId,
                new IdQueryParameters(vmId),
                _asyncQuery);
    }

    private void setVmTicket() {
        // Create ticket for single sign on.
        Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(), null, 120),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        SpiceConsoleModel spiceConsoleModel = (SpiceConsoleModel) result.getState();
                        spiceConsoleModel.postSendVmTicket(result.getReturnValue());

                    }
                }, this);
    }

    public void postSendVmTicket(VdcReturnValueBase returnValue) {
        if (returnValue == null || !returnValue.getSucceeded()) {
            return;
        }

        ticket = (String) returnValue.getActionReturnValue();

        // Only if the VM has agent and we connect through user-portal
        // we attempt to perform SSO (otherwise an error will be thrown)
        if (!getConfigurator().getIsAdmin() && getEntity().getHasAgent()
                && getEntity().getStatus() == VMStatus.Up) {
            getLogger().info("SpiceConsoleManager::Connect: Attempting to perform SSO on Desktop " //$NON-NLS-1$
                    + getEntity().getName());

            Frontend.RunAction(VdcActionType.VmLogon, new VmOperationParameterBase(getEntity().getId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            final SpiceConsoleModel spiceConsoleModel = (SpiceConsoleModel) result.getState();
                            final VdcReturnValueBase logonCommandReturnValue = result.getReturnValue();
                            boolean isLogonSucceeded = logonCommandReturnValue != null && logonCommandReturnValue.getSucceeded();
                            if (isLogonSucceeded) {
                                spiceConsoleModel.executeQuery(getEntity());
                            }
                            else {
                                if (logonCommandReturnValue != null && logonCommandReturnValue.getFault().getError() == VdcBllErrors.nonresp) {
                                    UICommand okCommand =
                                            new UICommand("SpiceWithoutAgentOK", new BaseCommandTarget() { //$NON-NLS-1$
                                                        @Override
                                                        public void executeCommand(UICommand uiCommand) {
                                                            logSsoOnDesktopFailedAgentNonResp(spiceConsoleModel.getLogger(),
                                                                    logonCommandReturnValue != null ?
                                                                            logonCommandReturnValue.getDescription()
                                                                            : ""); //$NON-NLS-1$
                                                            spiceConsoleModel.executeQuery(getEntity());
                                                            parentModel.setWindow(null);
                                                        }
                                                    });

                                    UICommand cancelCommand = new UICommand("SpiceWithoutAgentCancel", new BaseCommandTarget() { //$NON-NLS-1$
                                        @Override
                                        public void executeCommand(UICommand uiCommand) {
                                            parentModel.setWindow(null);
                                        }
                                    });

                                    createConnectWithoutAgentConfirmationPopup(okCommand, cancelCommand);
                                }
                                else {
                                    logSsoOnDesktopFailed(spiceConsoleModel.getLogger(),
                                            logonCommandReturnValue != null ? logonCommandReturnValue.getDescription()
                                                    : ""); //$NON-NLS-1$
                                }
                            }
                        }
                    },
                    this);
        } else {
            executeQuery(getEntity());
        }
    }

    private void createConnectWithoutAgentConfirmationPopup(UICommand okCommand, UICommand cancelCommand){
        SpiceToGuestWithNonRespAgentModel spiceWithoutAgentModel = new SpiceToGuestWithNonRespAgentModel();
        spiceWithoutAgentModel.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .guestAgentNotResponsiveTitle());
        spiceWithoutAgentModel.setHashName("sso_did_not_succeeded"); //$NON-NLS-1$

        spiceWithoutAgentModel.setMessage(ConstantsManager.getInstance()
                .getMessages()
                .connectingToGuestWithNotResponsiveAgentMsg());

        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        spiceWithoutAgentModel.getCommands().add(okCommand);

        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        spiceWithoutAgentModel.getCommands().add(cancelCommand);

        parentModel.setWindow(spiceWithoutAgentModel);
    }

    private void logSsoOnDesktopFailedAgentNonResp(ILogger logger, String vmName) {
        logger.info("SpiceConsoleManager::Connect: Failed to perform SSO on Destkop " //$NON-NLS-1$
                + vmName + " because agent is non-responsive, continuing without SSO."); //$NON-NLS-1$
    }

    private void logSsoOnDesktopFailed(ILogger logger, String vmName) {
        logger.info("SpiceConsoleManager::Connect: Failed to perform SSO on Destkop " //$NON-NLS-1$
                + vmName + ", cancel open spice console request."); //$NON-NLS-1$
    }

    public void spiceConnect()
    {
        try {
            getspice().connect();
        } catch (RuntimeException ex) {
            getLogger().error("Exception on Spice connect", ex); //$NON-NLS-1$
        }
    }

    private static final String CommandStop = "Stop"; //$NON-NLS-1$
    private static final String CommandPlay = "Play"; //$NON-NLS-1$
    private static final String CommandSuspend = "Suspend"; //$NON-NLS-1$
    private static final String CommandChangeCD = "ChangeCD"; //$NON-NLS-1$

    private static final int ID_SYS_MENU_DISCONNECT = 0x1200;
    private static final int ID_SYS_MENU_SEND_CTRL_ALT_DEL = 0x1300;
    private static final int ID_SYS_MENU_TOGGLE_FULL_SCREEN = 0x1400;
    private static final int ID_SYS_MENU_SEND_SHIFT_F11 = 0x1500;
    private static final int ID_SYS_MENU_SEND_SHIFT_F12 = 0x1600;
    private static final int ID_SYS_MENU_SEND_CTRL_ALT_END = 0x1700;
    private static final int ID_SYS_MENU_USB_DEVICES = 0x1800;
}
