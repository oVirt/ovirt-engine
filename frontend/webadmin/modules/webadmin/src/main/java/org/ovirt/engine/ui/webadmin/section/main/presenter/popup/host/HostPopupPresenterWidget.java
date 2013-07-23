package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostModel, HostPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostModel> {

        HasUiCommandClickHandlers getTestButton();
        HasClickHandlers getUpdateHostsButton();

        /**
         * Switch to the power management tab.
         */
        void showPowerManagement();
        void setHostProviderVisibility(boolean visible);
    }

    @Inject
    public HostPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostModel model) {
        super.init(model);

        addTestButtonListener();
        addUpdateHostsListener(model);
        addPowerManagementListener(model);
        addHostProviderListener(model);
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
                getView().getTestButton().getCommand().execute();
            }
        }));
    }

    private void addUpdateHostsListener(final HostModel model) {
        registerHandler(getView().getUpdateHostsButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getUpdateHostsCommand().execute();
            }
        }));
    }
    private void addHostProviderListener(final HostModel model) {
        model.getProviderSearchFilter().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    getView().setHostProviderVisibility(model.getProviderSearchFilter().getIsAvailable());
                }
            }
        });
    }
}
