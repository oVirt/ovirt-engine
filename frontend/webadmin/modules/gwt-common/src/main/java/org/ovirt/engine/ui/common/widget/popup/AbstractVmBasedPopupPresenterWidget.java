package org.ovirt.engine.ui.common.widget.popup;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import java.util.List;

public class AbstractVmBasedPopupPresenterWidget<V extends AbstractVmBasedPopupPresenterWidget.ViewDef> extends AbstractModelBoundPopupPresenterWidget<UnitVmModel, V>  {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UnitVmModel> {
        void switchMode(boolean isAdvanced);

        void initToCreateInstanceMode();

        void setSpiceProxyOverrideExplanation(String explanation);

        void switchAttachToInstanceType(boolean isAttached);

        List<HasValidation> getInvalidWidgets();
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

        initToCreateInstanceMode(model);

        initListeners(model);
    }

    private void initToCreateInstanceMode(UnitVmModel model) {
        if (model.isCreateInstanceOnly()) {
            // hide the admin-only widgets only for non-admin users
            getView().initToCreateInstanceMode();
        }
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

        model.getValid().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                switchToAdvancedIfNeeded(model);
            }

        });
    }

    private void switchToAdvancedIfNeeded(final UnitVmModel model) {
        if (model.getAdvancedMode().getEntity() || model.getValid().getEntity()) {
            return;
        }

        List<HasValidation> invalidWidgets = getView().getInvalidWidgets();

        if (invalidWidgets.size() == 0) {
            return;
        }

        for (HasValidation invalidWidget : invalidWidgets) {
            boolean isVisible = invalidWidget instanceof HasVisibility && ((HasVisibility) invalidWidget).isVisible();
            boolean isAttached = invalidWidget instanceof HasAttachHandlers && ((HasAttachHandlers) invalidWidget).isAttached();
            if (!isVisible || !isAttached) {
                model.getAdvancedMode().setEntity(true);
                break;
            }
        }
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
