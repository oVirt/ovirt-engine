package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
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
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class SpiceConsoleModel extends ConsoleModel {

    public enum ClientConsoleMode { Native, Auto, Html5 }

    public static EventDefinition spiceDisconnectedEventDefinition;
    public static EventDefinition spiceConnectedEventDefinition;

    private static final DynamicMessages dynamicMessages = (DynamicMessages) TypeResolver.getInstance().resolve(DynamicMessages.class);

    private ConsoleClient spiceImpl;
    private ClientConsoleMode consoleMode;

    private void setSpiceImpl(ConsoleClient value) {
        spiceImpl = value;
    }

    public ClientConsoleMode getClientConsoleMode() {
        return consoleMode;
    }

    static {
        spiceDisconnectedEventDefinition = new EventDefinition("SpiceDisconnected", SpiceConsoleModel.class); //$NON-NLS-1$
        spiceConnectedEventDefinition = new EventDefinition("SpiceConnected", SpiceConsoleModel.class); //$NON-NLS-1$
    }

    public SpiceConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());
        setConsoleClientMode(getDefaultConsoleMode());
    }

    protected ClientConsoleMode getDefaultConsoleMode() {
        return ClientConsoleMode.valueOf((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ClientModeSpiceDefault));
    }

    public ConsoleClient getSpiceImpl() {
        return spiceImpl;
    }

    public boolean isWanOptionsAvailableForMyVm() {
        boolean isWindowsVm = AsyncDataProvider.getInstance().isWindowsOsType(getEntity().getOs());
        boolean spiceGuestAgentInstalled = getEntity().getSpiceDriverVersion() != null;

        return isWindowsVm && spiceGuestAgentInstalled;
    }

    /**
     * Sets implementation of ConsoleClient which will be used
     * and performs sets initial configuration as well (different for WA/UP).
     *
     * Default mode is "Auto"
     */
    public void setConsoleClientMode(ClientConsoleMode consoleMode) {
        ConsoleUtils consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
        this.consoleMode = consoleMode;

        switch (consoleMode) {
            case Native:
                setSpiceImpl((ConsoleClient) TypeResolver.getInstance().resolve(ISpiceNative.class));
                break;
            case Html5:
                if (consoleUtils.webBasedClientsSupported()) {
                    setSpiceImpl((ConsoleClient) TypeResolver.getInstance().resolve(ISpiceHtml5.class));
                } else {
                    getLogger().debug("Cannot select SPICE-HTML5."); //$NON-NLS-1$
                    setDefaultConsoleMode();
                }
                break;
            default:
                setDefaultConsoleMode();
            break;
        }

        getConfigurator().configure(getSpiceImpl());

        if (getEntity() != null) {
            boolean isSpiceProxyDefined = consoleUtils.isSpiceProxyDefined(getEntity());
            getSpiceImpl().getOptions().setSpiceProxyEnabled(isSpiceProxyDefined);
        }
    }

    private void setDefaultConsoleMode() {
        setSpiceImpl((ConsoleClient) TypeResolver.getInstance().resolve(ISpiceNative.class));
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
                getSpiceImpl().getOptions().setWanOptionsEnabled(false);
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

    public void invokeClient() {
        final GraphicsInfo spiceInfo = getEntity().getGraphicsInfos().get(GraphicsType.SPICE);
        if (spiceInfo == null) {
            throw new IllegalStateException("Trying to invoke SPICE console but VM GraphicsInfo is null.");//$NON-NLS-1$
        }

        final ConsoleOptions options = getSpiceImpl().getOptions();
        options.setVmId(getEntity().getId());
        // configure options
        ConfigureConsoleOptionsParams parameters = new ConfigureConsoleOptionsParams(options, true);
        parameters.setEngineBaseUrl(FrontendUrlUtils.getRootURL());
        parameters.setConsoleClientResourcesUrl(dynamicMessages.consoleClientResourcesUrl());
        Frontend.getInstance().runQuery(
                VdcQueryType.ConfigureConsoleOptions,
                parameters,
                new ShowErrorAsyncQuery(new AsyncCallback<VdcQueryReturnValue>() {
                    @Override
                    public void onSuccess(VdcQueryReturnValue returnValue) {
                        final ConsoleOptions configuredOptions = returnValue.getReturnValue();
                        // overriding global server settings by frontend settings
                        configuredOptions.setRemapCtrlAltDelete(options.isRemapCtrlAltDelete());
                        configuredOptions.setTitle(getClientTitle());
                        configuredOptions.setVmName(getEntity().getName());
                        configuredOptions.setFullScreen(options.isFullScreen());
                        configuredOptions.setSmartcardEnabledOverridden(options.isSmartcardEnabledOverridden());
                        if (!configuredOptions.isSpiceProxyEnabled()) {
                            configuredOptions.setSpiceProxy(null); // override spice proxy from backend
                        }

                        try {
                            getSpiceImpl().setOptions(configuredOptions);
                            getSpiceImpl().invokeClient();
                        } catch (RuntimeException ex) {
                            getLogger().error("Exception on Spice connect", ex); //$NON-NLS-1$
                        }
                    }
                }));
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
                            final VdcReturnValueBase logonCommandReturnValue = result.getReturnValue();
                            boolean isLogonSucceeded = logonCommandReturnValue != null && logonCommandReturnValue.getSucceeded();
                            if (isLogonSucceeded) {
                                invokeClient();
                            }
                            else {
                                if (logonCommandReturnValue != null && logonCommandReturnValue.getFault().getError() == EngineError.nonresp) {
                                    UICommand okCommand =
                                            new UICommand("SpiceWithoutAgentOK", new BaseCommandTarget() { //$NON-NLS-1$
                                                        @Override
                                                        public void executeCommand(UICommand uiCommand) {
                                                            logSsoOnDesktopFailedAgentNonResp(getLogger(),
                                                                    logonCommandReturnValue != null ?
                                                                            logonCommandReturnValue.getDescription()
                                                                            : ""); //$NON-NLS-1$
                                                            invokeClient();
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
                                    logSsoOnDesktopFailed(getLogger(),
                                            logonCommandReturnValue != null ? logonCommandReturnValue.getDescription()
                                                    : ""); //$NON-NLS-1$
                                }
                            }
                        }
                    },
                    this);
        } else {
            invokeClient();
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

}
