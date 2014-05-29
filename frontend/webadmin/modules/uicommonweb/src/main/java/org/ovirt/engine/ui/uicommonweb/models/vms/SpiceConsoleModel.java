package org.ovirt.engine.ui.uicommonweb.models.vms;

import com.google.gwt.user.client.Window;
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
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
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
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
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

    public static EventDefinition spiceDisconnectedEventDefinition;
    public static EventDefinition spiceConnectedEventDefinition;
    public static EventDefinition spiceMenuItemSelectedEventDefinition;
    public static EventDefinition usbAutoShareChangedEventDefinition;
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
        spiceDisconnectedEventDefinition = new EventDefinition("SpiceDisconnected", SpiceConsoleModel.class); //$NON-NLS-1$
        spiceConnectedEventDefinition = new EventDefinition("SpiceConnected", SpiceConsoleModel.class); //$NON-NLS-1$
        spiceMenuItemSelectedEventDefinition = new EventDefinition("SpiceMenuItemSelected", SpiceConsoleModel.class); //$NON-NLS-1$
        usbAutoShareChangedEventDefinition = new EventDefinition("UsbAutoShareChanged", SpiceConsoleModel.class); //$NON-NLS-1$
        wanColorDepthChangedEventDefinition = new EventDefinition("ColorDepthChanged", SpiceConsoleModel.class); //$NON-NLS-1$
        wanDisableEffectsChangeEventDefinition = new EventDefinition("DisableEffectsChange", SpiceConsoleModel.class); //$NON-NLS-1$
    }

    public SpiceConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());
        setConsoleClientMode(getDefaultConsoleMode());
    }

    protected ClientConsoleMode getDefaultConsoleMode() {
        return ClientConsoleMode.valueOf((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ClientModeSpiceDefault));
    }

    public ISpice getspice() {
        return privatespice;
    }

    public boolean isWanOptionsAvailableForMyVm() {
        boolean isWindowsVm = AsyncDataProvider.getInstance().isWindowsOsType(getEntity().getOs());
        boolean spiceGuestAgentInstalled = getEntity().getSpiceDriverVersion() != null;

        return isWindowsVm && spiceGuestAgentInstalled;
    }

    /**
     * Sets implementation of ISpice which will be used (either implementation
     * that uses spice browser plugin or the one using configuration servlet)
     * and performs sets initial configuration as well (different for WA/UP).
     *
     * Default mode is "Auto" (spice browser plugin is used only if it is
     * installed).
     */
    public void setConsoleClientMode(ClientConsoleMode consoleMode) {
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

        if (getEntity() != null) {
            ConsoleUtils consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
            boolean isSpiceProxyDefined = consoleUtils.isSpiceProxyDefined(getEntity());
            getspice().setSpiceProxyEnabled(isSpiceProxyDefined);
        }
    }

    @Override
    public boolean canConnect() {
        return super.canConnect() && !getIsConnected();
    }

    @Override
    protected void connect() {
        if (getEntity() != null) {
            getLogger().debug("Connecting to Spice console..."); //$NON-NLS-1$
            // Check a spice version.
            if (getConfigurator().getIsAdmin()
                    && getspice().getCurrentVersion().compareTo(getspice().getDesiredVersion()) < 0)
            {
                Window.alert("Spice client version is not as desired (" + getspice().getDesiredVersion() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }

            // Don't connect if there VM is not running on any host.
            if (getEntity().getRunOnVds() == null) {
                return;
            }

            // If it is not windows or SPICE guest agent is not installed, make sure the WAN options are disabled.
            if (!AsyncDataProvider.getInstance().isWindowsOsType(getEntity().getVmOsId()) || !getEntity().getHasSpiceDriver()) {
                getspice().setWanOptionsEnabled(false);
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
    public boolean canBeSelected() {
        boolean hasVmSpiceSupport = Boolean.TRUE.equals(AsyncDataProvider.getInstance().hasSpiceSupport(getEntity().getOs(), getEntity().getVdsGroupCompatibilityVersion()));
        return getEntity().getGraphicsInfos().containsKey(GraphicsType.SPICE) && hasVmSpiceSupport;
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
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
            SpiceMenuCommandItem item = null;
            for (SpiceMenuItem a : menu.descendants()) {
                if (a.getClass() == SpiceMenuCommandItem.class && a.getId() == e.getMenuItemId()) {
                    item = (SpiceMenuCommandItem) a;
                    break;
                }
            }
            if (item != null) {
                if (CommandPlay.equals(item.getCommandName())) {
                    // use sysprep iff the vm is not initialized and vm has Win OS
                    RunVmParams tempVar = new RunVmParams(getEntity().getId());
                    tempVar.setRunAsStateless(getEntity().isStateless());
                    Frontend.getInstance().runMultipleAction(VdcActionType.RunVm,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { tempVar })));

                } else if (CommandSuspend.equals(item.getCommandName())) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.HibernateVm,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new VmOperationParameterBase(getEntity().getId()) })));

                } else if (CommandStop.equals(item.getCommandName())) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.ShutdownVm,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new ShutdownVmParameters(getEntity().getId(),
                                    true) })));

                } else if (CommandChangeCD.equals(item.getCommandName())) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.ChangeDisk,
                            new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new ChangeDiskCommandParameters(getEntity().getId(),
                                    getEjectLabel().equals(item.getText()) ? "" : item.getText()) }))); //$NON-NLS-1$
                }
            }
        }
    }

    private void spice_Disconnected(Object sender, ErrorCodeEventArgs e) {
        getspice().getDisconnectedEvent().removeListener(this);
        getspice().getMenuItemSelectedEvent().removeListener(this);

        setIsConnected(false);

        if (e.getErrorCode() > 100) {
            getErrorEvent().raise(this, e);
        }
    }

    private void spice_Connected(Object sender) {
        setIsConnected(true);
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
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

                ArrayList<VdcQueryParametersBase> parametersList =
                        new ArrayList<VdcQueryParametersBase>();
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SSLEnabled, AsyncDataProvider.getInstance().getDefaultConfigurationVersion()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.CipherSuite, AsyncDataProvider.getInstance().getDefaultConfigurationVersion()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceSecureChannels,
                        thisVm.getVdsGroupCompatibilityVersion().toString()));
                parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.EnableSpiceRootCertificateValidation, AsyncDataProvider.getInstance().getDefaultConfigurationVersion()));
                parametersList.add(new IdQueryParameters(thisVm.getId()));
                parametersList.add(new VdcQueryParametersBase());

                if (isoDomain != null) {
                    queryTypeList.add(VdcQueryType.GetImagesListByStoragePoolId);

                    GetImagesListByStoragePoolIdParameters getIsoParams =
                            new GetImagesListByStoragePoolIdParameters(vm.getStoragePoolId(), ImageFileType.ISO);
                    parametersList.add(getIsoParams);
                }

                Frontend.getInstance().runMultipleQueries(queryTypeList, parametersList, thisSpiceConsoleModel);
            }
        };

        AsyncDataProvider.getInstance().getIsoDomainByDataCenterId(_asyncQuery0, vm.getStoragePoolId());
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

        GraphicsInfo spiceInfo = getEntity().getGraphicsInfos().get(GraphicsType.SPICE);
        if (spiceInfo == null) {
            throw new IllegalStateException("Trying to invoke SPICE console but VM GraphicsInfo is null.");//$NON-NLS-1$
        }
        getspice().setSmartcardEnabled(getEntity().isSmartcardEnabled());
        Integer port = spiceInfo.getPort();
        getspice().setPort(port == null ? 0 : port);
        getspice().setPassword(ticket);
        getspice().setTicketValiditySeconds(TICKET_VALIDITY_SECONDS);
        getspice().setNumberOfMonitors(getEntity().getNumOfMonitors());
        getspice().setGuestHostName(getEntity().getVmHost().split("[ ]", -1)[0]); //$NON-NLS-1$
        if (spiceInfo.getTlsPort() != null) {
            getspice().setSecurePort(spiceInfo.getTlsPort());
        }
        if (!StringHelper.isNullOrEmpty(spiceSecureChannels)) {
            getspice().setSslChanels(spiceSecureChannels);
        }
        if (!StringHelper.isNullOrEmpty(cipherSuite)) {
            getspice().setCipherSuite(cipherSuite);
        }

        getspice().setHostSubject(certificateSubject);
        getspice().setTrustStore(caCertificate);

        getspice().setTitle(getClientTitle());

        getspice().setSpiceProxy(determineSpiceProxy());

        // If 'AdminConsole' is true, send true; otherwise, false should be sent only for VMs with SPICE driver
        // installed.
        getspice().setAdminConsole(getConfigurator().getSpiceAdminConsole() ? true : !getEntity().getHasSpiceDriver());

        // Update 'UsbListenPort' value
        getspice().setUsbListenPort(getConfigurator().getIsUsbEnabled()
                && getEntity().getUsbPolicy() == UsbPolicy.ENABLED_LEGACY ? getConfigurator().getSpiceDefaultUsbPort()
                : getConfigurator().getSpiceDisableUsbListenPort());

        getspice().setToggleFullscreenHotKey(getToggleFullScreenKeys());
        getspice().setReleaseCursorHotKey(getReleaseCursorKeys());

        getspice().setLocalizedStrings(new String[]{
                ConstantsManager.getInstance().getConstants().usb(),
                ConstantsManager.getInstance()
                        .getConstants()
                        .usbDevicesNoUsbdevicesClientSpiceUsbRedirectorNotInstalled()});

        // Create menu.
        int id = 1;
        menu = new SpiceMenu();

        SpiceMenuContainerItem changeCDItem =
                new SpiceMenuContainerItem(id, ConstantsManager.getInstance().getConstants().changeCd());
        id++;

        ArrayList<String> isos = new ArrayList<String>();

        if (returnValues.size() > 6) {
            ArrayList<RepoImage> repoList = returnValues.get(6).getReturnValue();
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
        changeCDItem.getItems().add(new SpiceMenuCommandItem(id, getEjectLabel(), CommandChangeCD));
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

        String displayIp = spiceInfo.getIp();
        if (StringHelper.isNullOrEmpty(displayIp) || "0".equals(displayIp)) { //$NON-NLS-1$
            determineIpAndConnect(getEntity().getId());
        }
        else {
            // Try to connect.
            getspice().setHost(displayIp);
            spiceConnect();
        }
    }

    private String determineSpiceProxy() {
        if (!getspice().isSpiceProxyEnabled()) {
            return null;
        }

        if (!StringHelper.isNullOrEmpty(getEntity().getVmPoolSpiceProxy())) {
            return getEntity().getVmPoolSpiceProxy();
        }

        if (!StringHelper.isNullOrEmpty(getEntity().getVdsGroupSpiceProxy())) {
            return getEntity().getVdsGroupSpiceProxy();
        }

        String globalSpiceProxy = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.SpiceProxyDefault);
        if (!StringHelper.isNullOrEmpty(globalSpiceProxy)) {
            return globalSpiceProxy;
        }

        return null;
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

        Frontend.getInstance().runQuery(VdcQueryType.GetManagementInterfaceAddressByVmId,
                new IdQueryParameters(vmId),
                _asyncQuery);
    }

    private void setVmTicket() {
        // Create ticket for single sign on.
        Frontend.getInstance().runAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(), null, TICKET_VALIDITY_SECONDS),
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
        if (!getConfigurator().getIsAdmin() && getEntity().getStatus() == VMStatus.Up
                && SsoMethod.GUEST_AGENT.equals(getEntity().getSsoMethod())) {
            getLogger().info("SpiceConsoleManager::Connect: Attempting to perform SSO on Desktop " //$NON-NLS-1$
                    + getEntity().getName());

            Frontend.getInstance().runAction(VdcActionType.VmLogon, new VmOperationParameterBase(getEntity().getId()),
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
                                                            getParentModel().setWindow(null);
                                                        }
                                                    });

                                    UICommand cancelCommand = new UICommand("SpiceWithoutAgentCancel", new BaseCommandTarget() { //$NON-NLS-1$
                                        @Override
                                        public void executeCommand(UICommand uiCommand) {
                                            getParentModel().setWindow(null);
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
        spiceWithoutAgentModel.setHelpTag(HelpTag.sso_did_not_succeeded);
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

        getParentModel().setWindow(spiceWithoutAgentModel);
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

}
