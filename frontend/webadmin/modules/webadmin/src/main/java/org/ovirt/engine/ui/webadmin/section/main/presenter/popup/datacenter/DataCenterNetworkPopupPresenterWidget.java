package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterNetworkPopupPresenterWidget extends AbstractNetworkPopupPresenterWidget<DataCenterNetworkModel, DataCenterNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractNetworkPopupPresenterWidget.ViewDef<DataCenterNetworkModel> {

        void setNetworkClusterList(ListModel networkClusterList);

        void setMessageLabel(String label);

        void setInputFieldsEnabled(boolean enabled);

        HasClickHandlers getApply();

        void setApplyEnabled(boolean enabled);

    }

    @Inject
    public DataCenterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final DataCenterNetworkModel model) {
        // Let the parent do its work
        super.init(model);

        // Listen to Properties
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                DataCenterNetworkModel model = (DataCenterNetworkModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("NetworkClusterList".equals(propertyName)) { //$NON-NLS-1$
                    // update the view
                    getView().setNetworkClusterList(model.getNetworkClusterList());
                }else if ("Message".equals(propertyName)) { //$NON-NLS-1$
                    getView().setMessageLabel(model.getMessage());
                }
            }
        });

        model.getApplyCommand().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("IsExecutionAllowed".equals(propertyName)) { //$NON-NLS-1$
                    // update the view
                   getView().setApplyEnabled(model.getApplyCommand().getIsExecutionAllowed());
                }
            }
        });

        // Listen to "IsEnabled" property
        model.getIsEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                boolean inputFieldsEnabled = (Boolean) entity.getEntity();
                getView().setInputFieldsEnabled(inputFieldsEnabled);
            }
        });

        registerHandler(getView().getApply().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getApplyCommand().Execute();
            }
        }));
    }

}
