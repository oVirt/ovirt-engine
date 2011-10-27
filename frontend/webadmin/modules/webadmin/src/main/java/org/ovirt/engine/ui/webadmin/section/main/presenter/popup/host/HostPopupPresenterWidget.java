package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostModel, HostPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostModel> {

        HasUiCommandClickHandlers getTestButton();

        /**
         * Switch to the power management tab.
         */
        void showPowerManagement();

    }

    @Inject
    public HostPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(HostModel model) {
        super.init(model);

        registerHandler(getView().getTestButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().flush();
                getView().getTestButton().getCommand().Execute();
            }
        }));

        // If this is not a new host and power management is not enabled then
        // make sure that the power management tab is displayed:
        if (!model.getIsNew() && !model.getIsPowerManagementSelected()) {
            getView().showPowerManagement();
        }
    }

}
