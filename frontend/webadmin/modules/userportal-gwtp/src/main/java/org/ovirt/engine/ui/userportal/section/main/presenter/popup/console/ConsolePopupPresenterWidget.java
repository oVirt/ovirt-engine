package org.ovirt.engine.ui.userportal.section.main.presenter.popup.console;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.widget.basic.ConsoleUtils;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;

public class ConsolePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<UserPortalConsolePopupModel, ConsolePopupPresenterWidget.ViewDef> {

    @GenEvent
    public class ConsoleModelChanged {
        UserPortalItemModel itemModel;
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UserPortalConsolePopupModel> {

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

        void setVmName(String name);

        void flushToPrivateModel();

    }

    private final ConsoleUtils consoleUtils;
    private IEventListener viewUpdatingListener;
    private boolean wanOptionsAvailable = false;
    private UserPortalConsolePopupModel model;
    private final ApplicationConstants constants;

    @Inject
    public ConsolePopupPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleUtils consoleUtils,
            ApplicationConstants constants) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.constants = constants;
    }

    @Override
    public void init(final UserPortalConsolePopupModel model) {

        this.model = model;
        initView(model);
        initListeners(model);

        String vmName = ((UserPortalItemModel) model.getModel().getSelectedItem()).getName();
        getView().setVmName(vmName);

        super.init(model);

    }

    private void initListeners(final UserPortalConsolePopupModel model) {
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

    private void removeListeners(UserPortalConsolePopupModel model) {
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

    private void initView(UserPortalConsolePopupModel model) {

        listenOnRadioButtons(model);
        UserPortalItemModel currentItem = (UserPortalItemModel) model.getModel().getSelectedItem();

        boolean spiceAvailable =
                currentItem.getDefaultConsole() instanceof SpiceConsoleModel && consoleUtils.isSpiceAvailable();

        boolean vncAvailable =
                currentItem.getDefaultConsole() instanceof VncConsoleModel;

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
            if (!spice.getIsWanOptionsEnabled()) {
                getView().selectWanOptionsEnabled(false);
            }
        }

        boolean isWindowsVm = asUserPortalItem(model).getOsType().isWindows();
        boolean spiceGuestAgentInstalled = asUserPortalItem(model).getSpiceDriverVersion() != null;

        wanOptionsAvailable = isWindowsVm && spiceAvailable && spiceGuestAgentInstalled;
        if (wanOptionsAvailable) {
            getView().setWanOptionsVisible(true);
        } else {
            getView().setWanOptionsVisible(false);
        }

        getView().setAdditionalConsoleAvailable(rdpAvailable);
        getView().setSpiceConsoleAvailable(currentItem.getDefaultConsole() instanceof SpiceConsoleModel);

        boolean ctrlAltDelEnabled = consoleUtils.isCtrlAltDelEnabled();
        getView().setCtrlAltDelEnabled(ctrlAltDelEnabled, constants.ctrlAltDeletIsNotSupportedOnWindows());
        if (!ctrlAltDelEnabled && spice != null) {
            spice.setSendCtrlAltDelete(false);
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
            ConsoleModelChangedEvent.fire(getEventBus(), (UserPortalItemModel) model.getModel().getSelectedItem());
        }
    }

    protected UserPortalItemModel asUserPortalItem(UserPortalConsolePopupModel model) {
        return (UserPortalItemModel) model.getModel().getSelectedItem();
    }

    protected boolean isAdditionalConsoleAvailable(UserPortalItemModel currentItem) {
        return currentItem.getHasAdditionalConsole();
    }

    protected void listenOnRadioButtons(final UserPortalConsolePopupModel model) {
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

    protected ISpice extractSpice(UserPortalConsolePopupModel model) {
        ConsoleModel consoleModel = asUserPortalItem(model).getDefaultConsole();
        if (!(consoleModel instanceof SpiceConsoleModel)) {
            return null;
        }

        ISpice spice = ((SpiceConsoleModel) consoleModel).getspice();
        return spice;
    }

}
