package org.ovirt.engine.ui.common.widget.popup;

import java.util.List;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.numa.NumaSupportPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.ValidationTabSwitchHelper;
import org.ovirt.engine.ui.common.view.TabbedView;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AbstractVmBasedPopupPresenterWidget<V extends AbstractVmBasedPopupPresenterWidget.ViewDef> extends
    AbstractModelBoundPopupPresenterWidget<UnitVmModel, V>  {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UnitVmModel>, TabbedView {
        void switchMode(boolean isAdvanced);

        void switchManaged(boolean managed);

        void initToCreateInstanceMode();

        void setSpiceProxyOverrideExplanation(String explanation);

        void switchAttachToInstanceType(boolean isAttached);

        List<HasValidation> getInvalidWidgets();

        HasUiCommandClickHandlers getNumaSupportButton();

        HasClickHandlers getAddAffinityGroupButton();

        HasClickHandlers getAddAffinityLabelButton();
    }

    private final ClientStorage clientStorage;
    private Provider<NumaSupportPopupPresenterWidget> numaSupportProvider;

    @Inject
    public AbstractVmBasedPopupPresenterWidget(EventBus eventBus, V view, ClientStorage clientStorage) {
        super(eventBus, view);

        this.clientStorage = clientStorage;
    }

    public AbstractVmBasedPopupPresenterWidget(EventBus eventBus,
            V view,
            ClientStorage clientStorage,
            Provider<NumaSupportPopupPresenterWidget> numaSupportProvider) {
        this(eventBus, view, clientStorage);

        this.numaSupportProvider = numaSupportProvider;
    }

    @Override
    public void init(UnitVmModel model) {
        super.init(model);

        initAdvancedModeFromLocalStorage(model);

        switchAccordingToMode(model);

        initToCreateInstanceMode(model);

        initListeners(model);
    }

    private void initToCreateInstanceMode(UnitVmModel model) {
        if (model.isCreateInstanceOnly()) {
            // hide the admin-only widgets only for non-admin users
            getView().initToCreateInstanceMode();
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UnitVmModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (numaSupportProvider != null && windowModel instanceof NumaSupportModel) {
            return numaSupportProvider.get();
        }
        return super.getModelPopup(source, lastExecutedCommand, windowModel);
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(ValidationTabSwitchHelper.registerValidationHandler((EventBus) getEventBus(), this,
                getView()));
    }

    private void initListeners(final UnitVmModel model) {
        model.getAdvancedMode().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            storeAdvancedModeToLocalStorage(model);
            switchAccordingToMode(model);
        });

        model.getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            switchAccordingToCluster(model);
        });

        model.getAttachedToInstanceType().getPropertyChangedEvent().addListener((ev, sender, args) -> switchAttachToInstanceType(model));

        model.getValid().getPropertyChangedEvent().addListener((ev, sender, args) -> switchToAdvancedIfNeeded(model));

        registerHandler(getView().getNumaSupportButton().addClickHandler(event -> getView().getNumaSupportButton().getCommand().execute()));

        registerHandler(getView().getAddAffinityGroupButton().addClickHandler(event -> model.addAffinityGroup()));
        registerHandler(getView().getAddAffinityLabelButton().addClickHandler(event -> model.addAffinityLabel()));
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

    private void switchAttachToInstanceType(final UnitVmModel model) {
        getView().switchAttachToInstanceType(model.getAttachedToInstanceType().getEntity());
    }

    private void switchAccordingToMode(final UnitVmModel model) {
        getView().switchMode(model.getAdvancedMode().getEntity());
    }

    private void switchAccordingToCluster(final UnitVmModel model) {
        if (model.getDataCenterWithClustersList().getSelectedItem() != null) {
            getView().switchManaged(model.getDataCenterWithClustersList().getSelectedItem().getCluster().isManaged());
        }
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
