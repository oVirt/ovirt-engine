package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterNetworkPopupPresenterWidget extends AbstractNetworkPopupPresenterWidget<DataCenterNetworkModel, DataCenterNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractNetworkPopupPresenterWidget.ViewDef<DataCenterNetworkModel> {
    }

    @Inject
    public DataCenterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final DataCenterNetworkModel model) {
        super.init(model);

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
    }

}
