package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;

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

        addTestButtonListener();
        addPowerManagementListener(model);

    }

    private void addPowerManagementListener(final HostModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (!"IsPowerManagementTabSelected".equals(propName)) { //$NON-NLS-1$
                    return;
                }
                if (model.getIsPowerManagementTabSelected()) {
                    getView().showPowerManagement();
                }
            }
        });
    }

    private void addTestButtonListener() {
        registerHandler(getView().getTestButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().flush();
                getView().getTestButton().getCommand().Execute();
            }
        }));
    }
}
