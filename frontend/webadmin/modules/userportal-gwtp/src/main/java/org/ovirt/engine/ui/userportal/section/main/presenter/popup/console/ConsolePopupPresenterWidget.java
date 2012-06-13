package org.ovirt.engine.ui.userportal.section.main.presenter.popup.console;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
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

        HasValueChangeHandlers<Boolean> getSpiceRadioButton();

        HasValueChangeHandlers<Boolean> getRdpRadioButton();

        void rdpSelected(boolean selected);

        void spiceSelected(boolean selected);

        void selectSpice(boolean selected);

        void selectRdp(boolean selected);

        void setAdditionalConsoleAvailable(boolean hasAdditionalConsole);

        void setSpiceConsoleAvailable(boolean available);

        void selectWanOptionsEnabled(boolean selected);

        void setWanOptionsVisible(boolean visible);

        void setVmName(String name);

        void flushToPrivateModel();

    }

    private final ConsoleUtils consoleUtils;
    private IEventListener viewUpdatingListener;
    private boolean wanOptionsAvailable = false;
    private UserPortalConsolePopupModel model;

    @Inject
    public ConsolePopupPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleUtils consoleUtils) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
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
        boolean rdpAvailable = isAdditionalConsoleAvailable(currentItem) && consoleUtils.isRDPAvailable();

        getView().setSpiceAvailable(spiceAvailable);
        getView().setRdpAvailable(rdpAvailable);

        if (spiceAvailable && rdpAvailable) {
            getView().selectSpice(true);
            getView().selectRdp(false);
            getView().spiceSelected(true);
        } else {
            getView().selectSpice(spiceAvailable);
            getView().selectRdp(rdpAvailable);
            getView().rdpSelected(rdpAvailable);
            getView().spiceSelected(spiceAvailable);
        }

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
