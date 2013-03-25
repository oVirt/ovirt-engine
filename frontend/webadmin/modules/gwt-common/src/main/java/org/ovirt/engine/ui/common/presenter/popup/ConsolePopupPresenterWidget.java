package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.utils.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
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

        void rdpSelected(boolean selected);

        void spiceSelected(boolean selected);

        void selectSpice(boolean selected);

        void selectRdp(boolean selected);

        void selectVnc(boolean selected);

        void setAdditionalConsoleAvailable(boolean hasAdditionalConsole);

        void setSpiceConsoleAvailable(boolean available);

        void selectWanOptionsEnabled(boolean selected);

        void setWanOptionsVisible(boolean visible);

        void setDisableSmartcardVisible(boolean visible);

        void setCtrlAltDelEnabled(boolean enabled, String reason);

        void setSpiceProxyEnabled(boolean enabled, String reason);

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
        consoleOptionsPersister.loadFromLocalStorage(model.getModel(), model.getConsoleContext());
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
                currentItem.getDefaultConsoleModel() instanceof SpiceConsoleModel && consoleUtils.isSpiceAvailable();

        boolean vncAvailable =
                currentItem.getDefaultConsoleModel() instanceof VncConsoleModel;

        boolean rdpAvailable = isAdditionalConsoleAvailable(currentItem) && consoleUtils.isRDPAvailable();

        getView().setSpiceAvailable(spiceAvailable);
        getView().setRdpAvailable(rdpAvailable);
        getView().setVncAvailable(vncAvailable);

        ConsoleProtocol selectedProtocol = currentItem.getSelectedProtocol();

        boolean rdpPreselected = ConsoleProtocol.RDP.equals(selectedProtocol);
        boolean spicePreselected = ConsoleProtocol.SPICE.equals(selectedProtocol);
        boolean vncPreselected = ConsoleProtocol.VNC.equals(selectedProtocol);

        getView().selectSpice(spicePreselected);
        getView().selectRdp(rdpPreselected);
        getView().selectVnc(vncPreselected);

        getView().spiceSelected(spicePreselected);
        getView().rdpSelected(rdpPreselected);

        getView().setDisableSmartcardVisible(consoleUtils.isSmartcardGloballyEnabled(currentItem));

        ISpice spice = extractSpice(model);
        if (spice != null) {
            if (!spice.isWanOptionsEnabled()) {
                getView().selectWanOptionsEnabled(false);
            }
        }

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
            consoleOptionsPersister.storeToLocalStorage(model.getModel(), model.getConsoleContext());

            ConsoleModelChangedEvent.fire(getEventBus(), model.getModel());
        }
    }

    protected boolean isAdditionalConsoleAvailable(HasConsoleModel currentItem) {
        return currentItem.getAdditionalConsoleModel() != null;
    }

    protected void listenOnRadioButtons(final ConsolePopupModel model) {
        registerHandler(getView().getRdpRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().rdpSelected(event.getValue());
                getView().spiceSelected(!event.getValue());
                getView().setWanOptionsVisible(wanOptionsAvailable && !event.getValue());
            }
        }));

        registerHandler(getView().getSpiceRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().spiceSelected(event.getValue());
                getView().setWanOptionsVisible(wanOptionsAvailable && event.getValue());
                getView().rdpSelected(!event.getValue());
            }
        }));

        registerHandler(getView().getVncRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    // hide all detail panels if this is selected.
                    // Ignore if deselected
                    getView().spiceSelected(false);
                    getView().setWanOptionsVisible(false);
                    getView().rdpSelected(false);
                }
            }
        }));
    }

    protected ISpice extractSpice(ConsolePopupModel model) {
        ConsoleModel consoleModel = model.getModel().getDefaultConsoleModel();
        if (!(consoleModel instanceof SpiceConsoleModel)) {
            return null;
        }

        ISpice spice = ((SpiceConsoleModel) consoleModel).getspice();
        return spice;
    }

}
