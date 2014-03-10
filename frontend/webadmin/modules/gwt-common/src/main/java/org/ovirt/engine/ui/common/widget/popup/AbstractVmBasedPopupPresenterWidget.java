package org.ovirt.engine.ui.common.widget.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AbstractVmBasedPopupPresenterWidget<V extends AbstractVmBasedPopupPresenterWidget.ViewDef> extends AbstractModelBoundPopupPresenterWidget<UnitVmModel, V>  {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UnitVmModel> {
        void switchMode(boolean isAdvanced);

        void setSpiceProxyOverrideExplanation(String explanation);

        void switchAttachToInstanceType(boolean isAttached);
    }

    private ClientStorage clientStorage;

    @Inject
    public AbstractVmBasedPopupPresenterWidget(EventBus eventBus, V view, ClientStorage clientStorage) {
        super(eventBus, view);

        this.clientStorage = clientStorage;
    }

    @Override
    public void init(UnitVmModel model) {
        super.init(model);

        initAdvancedModeFromLocalStorage(model);

        swithAccordingToMode(model);

        initListeners(model);
    }

    private void initListeners(final UnitVmModel model) {
        model.getAdvancedMode().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(org.ovirt.engine.ui.uicompat.Event ev, Object sender, EventArgs args) {
                storeAdvancedModeToLocalStorage(model);
                swithAccordingToMode(model);
            }

        });

        model.getAttachedToInstanceType().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                swithAttachToInstanceType(model);
            }
        });
    }

    private void swithAttachToInstanceType(final UnitVmModel model) {
        getView().switchAttachToInstanceType(model.getAttachedToInstanceType().getEntity());
    }

    private void swithAccordingToMode(final UnitVmModel model) {
        getView().switchMode(model.getAdvancedMode().getEntity());
    }

    private void storeAdvancedModeToLocalStorage(UnitVmModel model) {
        if (model.getIsAdvancedModeLocalStorageKey() == null) {
            return;
        }

        clientStorage.setLocalItem(model.getIsAdvancedModeLocalStorageKey(), Boolean.toString(model.getAdvancedMode().getEntity()));
    }

    private void initAdvancedModeFromLocalStorage(UnitVmModel model) {
        if (model.getIsAdvancedModeLocalStorageKey() == null) {
            return;
        }

        boolean isAdvancedMode = Boolean.parseBoolean(clientStorage.getLocalItem(model.getIsAdvancedModeLocalStorageKey()));
        model.getAdvancedMode().setEntity(isAdvancedMode);
    }

}
