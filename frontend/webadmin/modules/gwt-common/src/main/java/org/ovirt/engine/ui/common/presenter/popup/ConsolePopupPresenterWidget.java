package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;

public class ConsolePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ConsolePopupModel, ConsolePopupPresenterWidget.ViewDef> {

    @GenEvent
    public class ConsoleModelChanged {

        HasConsoleModel newModel;

    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ConsolePopupModel> {

        void setSpiceAvailable(boolean visible);

        void setRdpAvailable(boolean visible);

        void setVncAvailable(boolean visible);

        HasValueChangeHandlers<Boolean> getSpiceRadioButton();

        HasValueChangeHandlers<Boolean> getRdpRadioButton();

        HasValueChangeHandlers<Boolean> getVncRadioButton();

        HasValueChangeHandlers<Boolean> getSpiceAutoImplRadioButton();
        HasValueChangeHandlers<Boolean> getSpiceNativeImplRadioButton();
        HasValueChangeHandlers<Boolean> getSpicePluginImplRadioButton();
        HasValueChangeHandlers<Boolean> getSpiceHtml5ImplRadioButton();

        HasValueChangeHandlers<Boolean> getNoVncImplRadioButton();
        HasValueChangeHandlers<Boolean> getVncNativeImplRadioButton();

        HasValueChangeHandlers<Boolean> getRdpAutoImplRadioButton();
        HasValueChangeHandlers<Boolean> getRdpNativeImplRadioButton();
        HasValueChangeHandlers<Boolean> getRdpPluginImplRadioButton();

        void showRdpPanel(boolean visible);

        void showSpicePanel(boolean visible);

        void showVncPanel(boolean visible);

        void selectSpice(boolean selected);

        void selectRdp(boolean selected);

        void selectVnc(boolean selected);

        void setNoVncEnabled(boolean enabled, String reason);

        void setAdditionalConsoleAvailable(boolean hasAdditionalConsole);

        void setSpiceConsoleAvailable(boolean available);

        void selectSpiceImplementation(SpiceConsoleModel.ClientConsoleMode consoleMode);

        void setSpicePluginImplEnabled(boolean enabled, String reason);

        void setSpiceHtml5ImplEnabled(boolean enabled, String reason);

        void setRdpPluginImplEnabled(boolean enabled, String reason);

        void selectWanOptionsEnabled(boolean selected);

        void setWanOptionsVisible(boolean visible);

        void setDisableSmartcardVisible(boolean visible);

        void setCtrlAltDelEnabled(boolean enabled, String reason);

        void setSpiceProxyEnabled(boolean enabled, String reason);

        void selectVncImplementation(VncConsoleModel.ClientConsoleMode clientConsoleMode);

        void selectRdpImplementation(RdpConsoleModel.ClientConsoleMode consoleMode);

        void setVmName(String name);

        void flushToPrivateModel();

    }

    private final ConsoleUtils consoleUtils;
    private IEventListener viewUpdatingListener;
    private boolean wanOptionsAvailable = false;
    private ConsolePopupModel model;
    private final CommonApplicationConstants constants;
    private final ConsoleOptionsFrontendPersister consoleOptionsPersister;

    @Inject
    public ConsolePopupPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleUtils consoleUtils,
            CommonApplicationConstants constants,
            ConsoleOptionsFrontendPersister consoleOptionsPersister) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.constants = constants;
        this.consoleOptionsPersister = consoleOptionsPersister;
    }

    @Override
    public void init(final ConsolePopupModel model) {
        if (model.getModel().isPool()) {
            throw new IllegalArgumentException("The console popup can not be used with pool, only with VM"); //$NON-NLS-1$
        }

        this.model = model;
        initModel(model);
        initView(model);
        initListeners(model);

        String vmName = model.getModel().getVM().getName();
        getView().setVmName(vmName);

        super.init(model);
    }

    private void initModel(ConsolePopupModel model) {
        consoleOptionsPersister.loadFromLocalStorage(model.getModel());
    }

    private void initListeners(final ConsolePopupModel model) {
        ISpice spice = extractSpice(model);
        if (spice == null) {
            return;
        }

        viewUpdatingListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().edit(model);
            }
        };

        spice.getUsbAutoShareChangedEvent().addListener(viewUpdatingListener);
        spice.getWANColorDepthChangedEvent().addListener(viewUpdatingListener);
        spice.getWANDisableEffectsChangeEvent().addListener(viewUpdatingListener);

    }

    private void removeListeners(ConsolePopupModel model) {
        if (viewUpdatingListener == null) {
            return;
        }

        ISpice spice = extractSpice(model);
        if (spice == null) {
            return;
        }

        spice.getUsbAutoShareChangedEvent().removeListener(viewUpdatingListener);
        spice.getWANColorDepthChangedEvent().removeListener(viewUpdatingListener);
        spice.getWANDisableEffectsChangeEvent().removeListener(viewUpdatingListener);
    }

    private void initView(ConsolePopupModel model) {

        listenOnRadioButtons(model);
        HasConsoleModel currentItem = model.getModel();

        boolean spiceAvailable =
                currentItem.getDefaultConsoleModel() instanceof SpiceConsoleModel;

        boolean vncAvailable =
                currentItem.getDefaultConsoleModel() instanceof VncConsoleModel;

        boolean rdpAvailable = isAdditionalConsoleAvailable(currentItem) && consoleUtils.isRDPAvailable();

        getView().setSpiceAvailable(spiceAvailable);
        getView().setRdpAvailable(rdpAvailable);
        getView().setVncAvailable(vncAvailable);

        ConsoleProtocol selectedProtocol = currentItem.getUserSelectedProtocol();

        boolean rdpPreselected = ConsoleProtocol.RDP.equals(selectedProtocol);
        boolean spicePreselected = ConsoleProtocol.SPICE.equals(selectedProtocol);
        boolean vncPreselected = ConsoleProtocol.VNC.equals(selectedProtocol);

        getView().selectSpice(spicePreselected);
        getView().selectRdp(rdpPreselected);
        getView().selectVnc(vncPreselected);

        getView().showSpicePanel(spicePreselected);
        getView().showRdpPanel(rdpPreselected);
        getView().showVncPanel(vncPreselected);

        getView().setDisableSmartcardVisible(consoleUtils.isSmartcardGloballyEnabled(currentItem));

        ISpice spice = extractSpice(model);
        if (spice != null) {
            if (!spice.isWanOptionsEnabled()) {
                getView().selectWanOptionsEnabled(false);
            }
        }

        if (!consoleUtils.isBrowserPluginSupported(ConsoleProtocol.SPICE)) {
            getView().setSpicePluginImplEnabled(false, constants.spicePluginNotSupportedByBrowser());
        }

        getView().setSpiceHtml5ImplEnabled(consoleUtils.isWebSocketProxyDefined(), constants.spiceHtml5OnlyWhenWebsocketProxySet());

        getView().setNoVncEnabled(consoleUtils.isWebSocketProxyDefined(), constants.webSocketProxyNotSet());

        if (!consoleUtils.isBrowserPluginSupported(ConsoleProtocol.RDP)) {
            getView().setRdpPluginImplEnabled(false, constants.rdpPluginNotSupportedByBrowser());
        }

        SpiceConsoleModel spiceModel = extractSpiceModel(model);
        getView().selectSpiceImplementation(spiceModel == null ? SpiceConsoleModel.ClientConsoleMode.Auto : spiceModel.getClientConsoleMode());

        VncConsoleModel vncModel = extractVncModel(model);
        getView().selectVncImplementation(vncModel == null ? VncConsoleModel.ClientConsoleMode.Native : vncModel.getClientConsoleMode());

        RdpConsoleModel rdpModel = extractRdpModel(model);
        getView().selectRdpImplementation(rdpModel == null ? RdpConsoleModel.ClientConsoleMode.Auto : rdpModel.getClientConsoleMode());

        wanOptionsAvailable = consoleUtils.isWanOptionsAvailable(model.getModel());
        if (wanOptionsAvailable) {
            getView().setWanOptionsVisible(true);
        } else {
            getView().setWanOptionsVisible(false);
        }

        getView().setAdditionalConsoleAvailable(rdpAvailable);
        getView().setSpiceConsoleAvailable(currentItem.getDefaultConsoleModel() instanceof SpiceConsoleModel);

        boolean ctrlAltDelEnabled = consoleUtils.isCtrlAltDelEnabled();
        getView().setCtrlAltDelEnabled(ctrlAltDelEnabled, constants.ctrlAltDeletIsNotSupportedOnWindows());

        boolean spiceProxyEnabled = consoleUtils.isSpiceProxyDefined();
        getView().setSpiceProxyEnabled(spiceProxyEnabled, constants.spiceProxyCanBeEnabledOnlyWhenDefined());
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
            consoleOptionsPersister.storeToLocalStorage(model.getModel());

            ConsoleModelChangedEvent.fire(this, model.getModel());
        }
    }

    protected boolean isAdditionalConsoleAvailable(HasConsoleModel currentItem) {
        return currentItem.getAdditionalConsoleModel() != null;
    }

    protected void listenOnRadioButtons(final ConsolePopupModel model) {
        registerHandler(getView().getRdpRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().showRdpPanel(event.getValue());
            }
        }));

        registerHandler(getView().getVncRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().showVncPanel(event.getValue());
            }
        }));

        registerHandler(getView().getSpiceRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().showSpicePanel(event.getValue());
            }
        }));

        registerHandler(getView().getSpiceAutoImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectSpiceImplementation(SpiceConsoleModel.ClientConsoleMode.Auto);
                    }
                }));

        registerHandler(getView().getSpiceNativeImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectSpiceImplementation(SpiceConsoleModel.ClientConsoleMode.Native);
                    }
                }));
        registerHandler(getView().getSpicePluginImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectSpiceImplementation(SpiceConsoleModel.ClientConsoleMode.Plugin);
                    }
                }));

         registerHandler(getView().getNoVncImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectVncImplementation(VncConsoleModel.ClientConsoleMode.NoVnc);
                    }
                }));

         registerHandler(getView().getVncNativeImplRadioButton()
                 .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                     @Override
                     public void onValueChange(ValueChangeEvent<Boolean> event) {
                         getView().selectVncImplementation(VncConsoleModel.ClientConsoleMode.Native);
                     }
                 }));

        registerHandler(getView().getRdpAutoImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectRdpImplementation(RdpConsoleModel.ClientConsoleMode.Auto);
                    }
                }));

        registerHandler(getView().getRdpNativeImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectRdpImplementation(RdpConsoleModel.ClientConsoleMode.Native);
                    }
                }));

        registerHandler(getView().getRdpPluginImplRadioButton()
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        getView().selectRdpImplementation(RdpConsoleModel.ClientConsoleMode.Plugin);
                    }
                }));
    }

    protected ISpice extractSpice(ConsolePopupModel model) {
        SpiceConsoleModel spiceModel = extractSpiceModel(model);

        if (spiceModel != null) {
            return spiceModel.getspice();
        }

        return null;
    }

    protected SpiceConsoleModel extractSpiceModel(ConsolePopupModel model) {
        ConsoleModel consoleModel = model.getModel().getDefaultConsoleModel();

        if (consoleModel instanceof SpiceConsoleModel) {
            return (SpiceConsoleModel) consoleModel;
        }

        return null;
    }

    private VncConsoleModel extractVncModel(ConsolePopupModel model) {
    ConsoleModel consoleModel = model.getModel().getDefaultConsoleModel();

        if (consoleModel instanceof VncConsoleModel) {
            return (VncConsoleModel) consoleModel;
        }

        return null;
    }

    protected RdpConsoleModel extractRdpModel(ConsolePopupModel model) {
        ConsoleModel consoleModel = model.getModel().getAdditionalConsoleModel();

        if (consoleModel instanceof RdpConsoleModel) {
            return (RdpConsoleModel) consoleModel;
        }

        return null;
    }

}
