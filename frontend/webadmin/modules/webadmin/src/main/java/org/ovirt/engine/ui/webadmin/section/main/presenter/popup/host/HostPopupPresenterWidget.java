package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostPopupPresenterWidget extends AbstractTabbedModelBoundPopupPresenterWidget<HostModel, HostPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<HostModel> {

        /**
         * Switch to the power management tab.
         */
        void showPowerManagement();
        HasClickHandlers getKernelCmdlineResetButton();
        HasClickHandlers getAddAffinityGroupButton();
        HasClickHandlers getAddAffinityLabelButton();
    }

    @Inject
    public HostPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostModel model) {
        super.init(model);
        addPowerManagementListener(model);
        addKernelCmdlineResetListener(model);
        addAddAffinityButtonListeners(model);
    }

    private void addKernelCmdlineResetListener(final HostModel model) {
        registerHandler(getView().getKernelCmdlineResetButton().addClickHandler(event -> model.resetKernelCmdline()));
    }

    private void addAddAffinityButtonListeners(final HostModel model) {
        registerHandler(getView().getAddAffinityGroupButton().addClickHandler(event -> model.addAffinityGroup()));
        registerHandler(getView().getAddAffinityLabelButton().addClickHandler(event -> model.addAffinityLabel()));
    }

    private void addPowerManagementListener(final HostModel model) {
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (!"IsPowerManagementTabSelected".equals(propName)) { //$NON-NLS-1$
                return;
            }
            if (model.getIsPowerManagementTabSelected()) {
                getView().showPowerManagement();
            }
        });
    }

}
