package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
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
    public void init(final HostModel model) {
        super.init(model);

        registerHandler(getView().getTestButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().flush();
                getView().getTestButton().getCommand().Execute();
            }
        }));

        model.getHost().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (!"Entity".equals(propName)) {
                    return;
                }
                // If this is not a new host and power management is not enabled then
                // make sure that the power management tab is displayed:
                if (!model.getIsNew() && !model.getIsPowerManagementSelected()) {
                    getView().showPowerManagement();
                }
            }
        });

    }
}
