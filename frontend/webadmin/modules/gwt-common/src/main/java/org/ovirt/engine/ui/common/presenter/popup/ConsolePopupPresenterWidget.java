package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.VmConsolesImpl;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleClient;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;

public class ConsolePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ConsolePopupModel, ConsolePopupPresenterWidget.ViewDef> {

    @GenEvent
    public class ConsoleModelChanged { }

    // long term todo - rewrite set***Visible to set***Enabled with descriptive tooltip if disabled
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ConsolePopupModel> {

        void setSpiceAvailable(boolean visible);

        void setRdpAvailable(boolean visible);

        void setVncAvailable(boolean visible);

        HasValueChangeHandlers<Boolean> getSpiceRadioButton();

        HasValueChangeHandlers<Boolean> getRdpRadioButton();

        HasValueChangeHandlers<Boolean> getVncRadioButton();

        HasValueChangeHandlers<Boolean> getNoVncImplRadioButton();
        HasValueChangeHandlers<Boolean> getVncNativeImplRadioButton();

        HasValueChangeHandlers<Boolean> getRdpAutoImplRadioButton();
        HasValueChangeHandlers<Boolean> getRdpNativeImplRadioButton();
        HasValueChangeHandlers<Boolean> getRdpPluginImplRadioButton();

        HasValueChangeHandlers<Boolean> getSpiceProxyEnabledCheckBox();
        HasEnabledWithHints getEnableUsbAutoshare();

        HasClickHandlers getConsoleClientResourcesAnchor();

        void showRdpPanel(boolean visible);

        void showSpicePanel(boolean visible);

        void showVncPanel(boolean visible);

        void selectSpice(boolean selected);

        void selectRdp(boolean selected);

        void selectVnc(boolean selected);

        void setNoVncEnabled(boolean enabled, String reason);

        void setAdditionalConsoleAvailable(boolean hasAdditionalConsole);

        void setSpiceConsoleAvailable(boolean available);

        void setRdpPluginImplEnabled(boolean enabled, String reason);

        void selectWanOptionsEnabled(boolean selected);

        void setWanOptionsVisible(boolean visible);

        void setDisableSmartcardVisible(boolean visible);

        void setSpiceProxyEnabled(boolean enabled, String reason);

        void setSpiceProxy(boolean enabled);

        Boolean getSpiceProxy();

        void selectVncImplementation(VncConsoleModel.ClientConsoleMode clientConsoleMode);

        void selectRdpImplementation(RdpConsoleModel.ClientConsoleMode consoleMode);

        void setVmName(String name);

        void flushToPrivateModel();

        void setCtrlAltDeleteRemapHotkey(String hotkey);
    }

    private final ConsoleUtils consoleUtils;
    private IEventListener<EventArgs> viewUpdatingListener;
    private boolean wanOptionsAvailable = false;
    private ConsolePopupModel model;
    private final DynamicMessages dynamicMessages;
    private final ConsoleOptionsFrontendPersister consoleOptionsPersister;
    private boolean spiceProxyUserPreference;
    private boolean spiceProxyDefinedOnCluster;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ConsolePopupPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleUtils consoleUtils,
            final DynamicMessages dynamicMessages,
            ConsoleOptionsFrontendPersister consoleOptionsPersister) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.consoleOptionsPersister = consoleOptionsPersister;
        this.dynamicMessages = dynamicMessages;
    }

    @Override
    public void init(final ConsolePopupModel model) {
        this.model = model;
        initView(model);
        initListeners(model);

        String vmName = (model.getVmConsoles() instanceof VmConsolesImpl)
                ? model.getVmConsoles().getVm().getName()
                : model.getVmConsoles().getVm().getVmPoolName(); // for pool dialogs display pool name

        getView().setVmName(vmName);
        getView().setCtrlAltDeleteRemapHotkey(ConsoleOptions.SECURE_ATTENTION_MAPPING);

        super.init(model);
    }

    private void initListeners(final ConsolePopupModel model) {
        ConsoleClient spice = model.getVmConsoles().getConsoleModel(SpiceConsoleModel.class).getSpiceImpl();
        if (spice == null) {
            return;
        }

        viewUpdatingListener = (ev, sender, args) -> getView().edit(model);
    }

    private void removeListeners(ConsolePopupModel model) {
        if (viewUpdatingListener == null) {
            return;
        }

        ConsoleClient spice = model.getVmConsoles().getConsoleModel(SpiceConsoleModel.class).getSpiceImpl();
        if (spice == null) {
            return;
        }
    }

    private void initView(ConsolePopupModel model) {

        listenOnRadioButtons();
        VmConsoles vmConsoles = model.getVmConsoles();

        getView().setSpiceAvailable(vmConsoles.canSelectProtocol(ConsoleProtocol.SPICE));
        getView().setVncAvailable(vmConsoles.canSelectProtocol(ConsoleProtocol.VNC));
        getView().setRdpAvailable(vmConsoles.canSelectProtocol(ConsoleProtocol.RDP));

        ConsoleProtocol selectedProtocol = vmConsoles.getSelectedProcotol();

        boolean rdpPreselected = ConsoleProtocol.RDP.equals(selectedProtocol);
        boolean spicePreselected = ConsoleProtocol.SPICE.equals(selectedProtocol);
        boolean vncPreselected = ConsoleProtocol.VNC.equals(selectedProtocol);

        getView().selectSpice(spicePreselected);
        getView().selectRdp(rdpPreselected);
        getView().selectVnc(vncPreselected);

        getView().showSpicePanel(spicePreselected);
        getView().showRdpPanel(rdpPreselected);
        getView().showVncPanel(vncPreselected);

        getView().setDisableSmartcardVisible(model.getVmConsoles().getVm().isSmartcardEnabled());

        ConsoleClient spice = model.getVmConsoles().getConsoleModel(SpiceConsoleModel.class).getSpiceImpl();
        if (spice != null) {
            if (!spice.getOptions().isWanOptionsEnabled()) {
                getView().selectWanOptionsEnabled(false);
            }
            spiceProxyUserPreference = vmConsoles.getConsoleModel(SpiceConsoleModel.class).getSpiceImpl().getOptions().isSpiceProxyEnabled();
        }

        getView().setNoVncEnabled(consoleUtils.webBasedClientsSupported(), constants.webBasedClientsUnsupported());

        if (!consoleUtils.isBrowserPluginSupported(ConsoleProtocol.RDP)) {
            getView().setRdpPluginImplEnabled(false, constants.rdpPluginNotSupportedByBrowser());
        }

        spiceProxyDefinedOnCluster = consoleUtils.isSpiceProxyDefined(vmConsoles.getVm());

        handleSpiceProxyAvailability();
        getView().selectVncImplementation(vmConsoles.getConsoleModel(VncConsoleModel.class).getClientConsoleMode());
        getView().selectRdpImplementation(vmConsoles.getConsoleModel(RdpConsoleModel.class).getClientConsoleMode());

        wanOptionsAvailable = vmConsoles.getConsoleModel(SpiceConsoleModel.class).isWanOptionsAvailableForMyVm();
        if (wanOptionsAvailable) {
            getView().setWanOptionsVisible(true);
        } else {
            getView().setWanOptionsVisible(false);
        }

        getView().setAdditionalConsoleAvailable(vmConsoles.canSelectProtocol(ConsoleProtocol.RDP));
        getView().setSpiceConsoleAvailable(vmConsoles.canSelectProtocol(ConsoleProtocol.SPICE));

        registerHandler(getView().getConsoleClientResourcesAnchor().addClickHandler(event -> {
            Window.open(dynamicMessages.consoleClientResourcesUrl(), "_blank", "resizable=yes,scrollbars=yes"); //$NON-NLS-1$ $NON-NLS-2$
        }));

        registerHandler(getView().getSpiceProxyEnabledCheckBox().addValueChangeHandler(
                booleanValueChangeEvent -> spiceProxyUserPreference = booleanValueChangeEvent.getValue()
        ));

        final boolean enableUsbAutoshareEnabled =
                model.getVmConsoles().getVm().getUsbPolicy() == UsbPolicy.ENABLED_NATIVE;
        if (enableUsbAutoshareEnabled) {
            getView().getEnableUsbAutoshare().setEnabled(true);
        } else {
            getView().getEnableUsbAutoshare().disable(constants.enableUsbSupportNotAvailable());
        }
    }


    @Override
    protected void beforeCommandExecuted(UICommand command) {
        super.beforeCommandExecuted(command);

        if (command == model.getDefaultCommand()) {
            // remove listeners which listens to changes in model before flushing
            // data into it
            removeListeners(model);

            // now flush the model
            getView().flushToPrivateModel();

            // store to local storage
            consoleOptionsPersister.storeToLocalStorage(model.getVmConsoles());

            ConsoleModelChangedEvent.fire(this);
        }
    }

    protected void listenOnRadioButtons() {
        registerHandler(getView().getRdpRadioButton().addValueChangeHandler(event -> getView().showRdpPanel(event.getValue())));

        registerHandler(getView().getVncRadioButton().addValueChangeHandler(event -> getView().showVncPanel(event.getValue())));

        registerHandler(getView().getSpiceRadioButton().addValueChangeHandler(event -> getView().showSpicePanel(event.getValue())));

        registerHandler(getView().getNoVncImplRadioButton()
                .addValueChangeHandler(event -> getView().selectVncImplementation(VncConsoleModel.ClientConsoleMode.NoVnc)));

        registerHandler(getView().getVncNativeImplRadioButton()
                 .addValueChangeHandler(event -> getView().selectVncImplementation(VncConsoleModel.ClientConsoleMode.Native)));

        registerHandler(getView().getRdpAutoImplRadioButton()
                .addValueChangeHandler(event -> getView().selectRdpImplementation(RdpConsoleModel.ClientConsoleMode.Auto)));

        registerHandler(getView().getRdpNativeImplRadioButton()
                .addValueChangeHandler(event -> getView().selectRdpImplementation(RdpConsoleModel.ClientConsoleMode.Native)));

        registerHandler(getView().getRdpPluginImplRadioButton()
                .addValueChangeHandler(event -> getView().selectRdpImplementation(RdpConsoleModel.ClientConsoleMode.Plugin)));
    }

    private void handleSpiceProxyAvailability() {
        setSpiceProxyAvailability();
        getView().setSpiceProxy(spiceProxyUserPreference);
    }

    private void setSpiceProxyAvailability() {
        getView().setSpiceProxyEnabled(
                spiceProxyDefinedOnCluster,
                spiceProxyDefinedOnCluster ? "" : constants.spiceProxyCanBeEnabledOnlyWhenDefined()); //$NON-NLS-1$
    }
}
