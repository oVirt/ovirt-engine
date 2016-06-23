package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ShowErrorAsyncQuery;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.restapi.HasForeignMenuData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class SpiceConsoleModel extends ConsoleModel {

    public enum ClientConsoleMode { Native, Plugin, Auto, Html5 }// The 'Plugin' is unsupported since 4.0 and will be removed in 4.1

    public static EventDefinition spiceDisconnectedEventDefinition;
    public static EventDefinition spiceConnectedEventDefinition;
    public static EventDefinition spiceMenuItemSelectedEventDefinition;

    private static final DynamicMessages dynamicMessages = (DynamicMessages) TypeResolver.getInstance().resolve(DynamicMessages.class);

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
    }

    public SpiceConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());
        setConsoleClientMode(getDefaultConsoleMode());
    }

    public ClientConsoleMode getDefaultConsoleMode() {
        return ClientConsoleMode.valueOf((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ClientModeSpiceDefault));
    }

    public boolean isEnableDeprecatedClientModeSpicePlugin() {
        return AsyncDataProvider.getInstance().isEnableDeprecatedClientModeSpicePlugin();
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
     * Sets implementation of ISpice which will be used
     * and performs sets initial configuration as well (different for WA/UP).
     *
     * Default mode is "Auto"
     */
    public void setConsoleClientMode(ClientConsoleMode consoleMode) {
        ConsoleUtils consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
        this.consoleMode = consoleMode;

        switch (consoleMode) {
            case Native:
                setspice((ISpice) TypeResolver.getInstance().resolve(ISpiceNative.class));
                break;
            case Plugin:// Unsupported since 4.0
                setspice((ISpice) TypeResolver.getInstance().resolve(ISpicePlugin.class));
                break;
            case Html5:
                if (consoleUtils.webBasedClientsSupported()) {
                    setspice((ISpice) TypeResolver.getInstance().resolve(ISpiceHtml5.class));
                } else {
                    getLogger().debug("Cannot select SPICE-HTML5."); //$NON-NLS-1$
                    setDefaultConsoleMode();
                }
                break;
            default:
                setDefaultConsoleMode();
            break;
        }

        getConfigurator().configure(getspice());

        if (!getspice().getConnectedEvent().getListeners().contains(this)) {
            getspice().getConnectedEvent().addListener(this);
        }

        if (getEntity() != null) {
            boolean isSpiceProxyDefined = consoleUtils.isSpiceProxyDefined(getEntity());
            getspice().getOptions().setSpiceProxyEnabled(isSpiceProxyDefined);
        }
    }

    private void setDefaultConsoleMode() {
        setspice((ISpice) TypeResolver.getInstance().resolve(ISpiceNative.class));
    }

    @Override
    public boolean canConnect() {
        return super.canConnect() && !getIsConnected();
    }

    @Override
    protected void connect() {
        if (getEntity() != null) {
            getLogger().debug("Connecting to Spice console..."); //$NON-NLS-1$

            // Don't connect if there VM is not running on any host.
            if (getEntity().getRunOnVds() == null) {
                return;
            }

            // If it is not windows or SPICE guest agent is not installed, make sure the WAN options are disabled.
            if (!AsyncDataProvider.getInstance().isWindowsOsType(getEntity().getVmOsId()) || !getEntity().getHasSpiceDriver()) {
                getspice().getOptions().setWanOptionsEnabled(false);
            }

            UICommand invokeConsoleCommand = new UICommand("invokeConsoleCommand", new BaseCommandTarget() { //$NON-NLS-1$
                @Override
                public void executeCommand(UICommand uiCommand) {
                    invokeConsole();
                }
            });
            executeCommandWithConsoleSafenessWarning(invokeConsoleCommand);
        }
    }

    @Override
    public boolean canBeSelected() {
        boolean hasVmSpiceSupport = Boolean.TRUE.equals(
                AsyncDataProvider.getInstance().hasSpiceSupport(
                        getEntity().getOs(),
                        getEntity().getCompatibilityVersion()));
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
                            new ArrayList<>(Arrays.asList(new VdcActionParametersBase[]{tempVar})));

                } else if (CommandSuspend.equals(item.getCommandName())) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.HibernateVm,
                            new ArrayList<>(Arrays.asList(new VdcActionParametersBase[]{new VmOperationParameterBase(getEntity().getId())})));

                } else if (CommandStop.equals(item.getCommandName())) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.ShutdownVm,
                            new ArrayList<>(Arrays.asList(new VdcActionParametersBase[]{new ShutdownVmParameters(getEntity().getId(),
                                    true)})));

                } else if (CommandChangeCD.equals(item.getCommandName())) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.ChangeDisk,
                            new ArrayList<>(Arrays.asList(new VdcActionParametersBase[]{new ChangeDiskCommandParameters(getEntity().getId(),
                                    getEjectLabel().equals(item.getText()) ? "" : item.getText())}))); //$NON-NLS-1$
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
        final AsyncQuery imagesListQuery = new AsyncQuery();
        imagesListQuery.setModel(this);
        imagesListQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<RepoImage> repoImages = ((VdcQueryReturnValue) returnValue).getReturnValue();
                ((SpiceConsoleModel) model).invokeClient(repoImages);
            }
        };

        AsyncQuery isoDomainQuery = new AsyncQuery();
        isoDomainQuery.setModel(this);
        isoDomainQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                StorageDomain isoDomain = (StorageDomain) result;
                if (isoDomain != null) {
                    GetImagesListByStoragePoolIdParameters getIsoParams =
                            new GetImagesListByStoragePoolIdParameters(vm.getStoragePoolId(), ImageFileType.ISO);

                    Frontend.getInstance().runQuery(
                            VdcQueryType.GetImagesListByStoragePoolId,
                            getIsoParams,
                            imagesListQuery);
                } else {
                    ((SpiceConsoleModel) model).invokeClient(null);
                }
            }
        };

        AsyncDataProvider.getInstance().getIsoDomainByDataCenterId(isoDomainQuery, vm.getStoragePoolId());
    }

    public void invokeClient(final List<RepoImage> repoImages) {
        final GraphicsInfo spiceInfo = getEntity().getGraphicsInfos().get(GraphicsType.SPICE);
        if (spiceInfo == null) {
            throw new IllegalStateException("Trying to invoke SPICE console but VM GraphicsInfo is null.");//$NON-NLS-1$
        }

        final ConsoleOptions options = getspice().getOptions();
        options.setVmId(getEntity().getId());
        // configure options
        ConfigureConsoleOptionsParams parameters = new ConfigureConsoleOptionsParams(options, true);
        parameters.setEngineBaseUrl(FrontendUrlUtils.getRootURL());
        parameters.setConsoleClientResourcesUrl(dynamicMessages.consoleClientResourcesUrl());
        Frontend.getInstance().runQuery(
                VdcQueryType.ConfigureConsoleOptions,
                parameters,
                new ShowErrorAsyncQuery(new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        final ConsoleOptions configuredOptions = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        // overriding global server settings by frontend settings
                        configuredOptions.setRemapCtrlAltDelete(options.isRemapCtrlAltDelete());
                        configuredOptions.setTitle(getClientTitle());
                        configuredOptions.setVmName(getEntity().getName());
                        configuredOptions.setFullScreen(options.isFullScreen());
                        configuredOptions.setSmartcardEnabledOverridden(options.isSmartcardEnabledOverridden());
                        configuredOptions.setAdminConsole(getConfigurator().getSpiceAdminConsole()
                                                          ? true
                                                          : !getEntity().getHasSpiceDriver());
                        if (!configuredOptions.isSpiceProxyEnabled()) {
                            configuredOptions.setSpiceProxy(null); // override spice proxy from backend
                        }
                        createAndSetMenu(configuredOptions, repoImages);

                        // Subscribe to events.
                        getspice().getDisconnectedEvent().addListener(SpiceConsoleModel.this);
                        getspice().getMenuItemSelectedEvent().addListener(SpiceConsoleModel.this);

                        try {
                            getspice().setOptions(configuredOptions);
                            if (getspice() instanceof HasForeignMenuData) {
                                setForeignMenuData((HasForeignMenuData) getspice());
                            }
                            getspice().invokeClient();
                        } catch (RuntimeException ex) {
                            getLogger().error("Exception on Spice connect", ex); //$NON-NLS-1$
                        }
                    }
                }));
    }

    // todo move to spicepluginimpl
    private void createAndSetMenu(ConsoleOptions options, List<RepoImage> repoImages) {
        int id = 1;
        menu = new SpiceMenu();

        SpiceMenuContainerItem changeCDItem =
                new SpiceMenuContainerItem(id, ConstantsManager.getInstance().getConstants().changeCd());
        id++;

        ArrayList<String> isos = new ArrayList<>();

        if (repoImages != null) {
            for (RepoImage repoImage : repoImages) {
                isos.add(repoImage.getRepoImageId());
            }
        }

        isos = isos.size() > 0
                ? isos
                : new ArrayList<>(Arrays.asList(new String[]{ConstantsManager.getInstance().getConstants().noCds()}));
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

        options.setMenu(menu.toString());
    }

    public void invokeConsole() { // todo refactor this later
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
                                if (logonCommandReturnValue != null && logonCommandReturnValue.getFault().getError() == EngineError.nonresp) {
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

    private static final String CommandStop = "Stop"; //$NON-NLS-1$
    private static final String CommandPlay = "Play"; //$NON-NLS-1$
    private static final String CommandSuspend = "Suspend"; //$NON-NLS-1$
    private static final String CommandChangeCD = "ChangeCD"; //$NON-NLS-1$

}
