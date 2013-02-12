package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterModel, ClusterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterModel> {

        void allowClusterWithVirtGlusterEnabled(boolean value);

    }

    @Inject
    public ClusterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ClusterModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("AllowClusterWithVirtGlusterEnabled".equals(propName)) { //$NON-NLS-1$
                    getView().allowClusterWithVirtGlusterEnabled(model.getAllowClusterWithVirtGlusterEnabled());
                }
            }
        });
    }

}
