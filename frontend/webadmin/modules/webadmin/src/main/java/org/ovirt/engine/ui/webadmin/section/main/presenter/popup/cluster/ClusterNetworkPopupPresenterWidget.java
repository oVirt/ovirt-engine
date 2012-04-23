package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterNetworkPopupPresenterWidget extends AbstractNetworkPopupPresenterWidget<ClusterNetworkModel, ClusterNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractNetworkPopupPresenterWidget.ViewDef<ClusterNetworkModel> {

        void setDataCenterName(String name);

    }

    @Inject
    public ClusterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ClusterNetworkModel model) {
        // Let the parent do its work
        super.init(model);

        // Listen for changes in the properties of the model in order
        // to update the view accordingly
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (args instanceof PropertyChangedEventArgs) {
                    PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                    if ("DataCenterName".equals(changedArgs.PropertyName)) { //$NON-NLS-1$
                        getView().setDataCenterName(model.getDataCenterName());
                    }
                }
            }
        });
    }

}
