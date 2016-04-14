package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostPopupView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostPopupPresenterWidget extends AbstractTabbedModelBoundPopupPresenterWidget<HostModel, HostPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<HostModel> {

        HasClickHandlers getUpdateHostsButton();

        /**
         * Switch to the power management tab.
         */
        void showPowerManagement();
        void setHostProviderVisibility(boolean visible);
        HasClickHandlers getKernelCmdlineResetButton();
    }

    @Inject
    public HostPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostModel model) {
        super.init(model);
        addUpdateHostsListener(model);
        addPowerManagementListener(model);
        addHostProviderListener(model);
        addRadioButtonsListeners(model);
        addKernelCmdlineResetListener(model);
    }

    private void addKernelCmdlineResetListener(final HostModel model) {
        registerHandler(getView().getKernelCmdlineResetButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.resetKernelCmdline();
            }
        }));
    }

    private void addRadioButtonsListeners(final HostModel model) {
        registerHandler(
                ((HostPopupView)getView()).rbDiscoveredHost.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (((HostPopupView)getView()).rbDiscoveredHost.getValue()) {
                            model.getIsDiscoveredHosts().setEntity(true);
                        }
                    }
                }));
        registerHandler(
                ((HostPopupView)getView()).rbProvisionedHost.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (((HostPopupView)getView()).rbProvisionedHost.getValue()) {
                            model.getIsDiscoveredHosts().setEntity(false);
                        }
                    }
                }));
    }

    private void addPowerManagementListener(final HostModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if (!"IsPowerManagementTabSelected".equals(propName)) { //$NON-NLS-1$
                    return;
                }
                if (model.getIsPowerManagementTabSelected()) {
                    getView().showPowerManagement();
                }
            }
        });
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
        model.getProviderSearchFilter().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                    getView().setHostProviderVisibility(model.getProviderSearchFilter().getIsAvailable());
                }
            }
        });
    }
}
