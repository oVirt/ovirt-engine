package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterNewNetworkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterNetworkModel, ClusterNewNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterNetworkModel> {
        /**
         * Set the name of the data center.
         *
         * @param name the name of the data center that should
         *   be displayed in the notes area
         */
        void setDataCenterName(String name);

        /**
         * Tell the view to enable/disable the text box for the VLAN tag.
         *
         * @param flag <code>true</code> to enable, <code>false</code> to
         *   disable
         */
        void setVLanTagEnabled(boolean flag);
    }

    @Inject
    public ClusterNewNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ClusterNetworkModel model) {
        // Let the parent do its work:
        super.init(model);

        // Set the enabled/disabled status of the VLAN tag field according
        // to the initial value in the model:
        Boolean hasVLanTagValue = (Boolean) model.getHasVLanTag().getEntity();
        getView().setVLanTagEnabled(hasVLanTagValue);

        // Listen for changes in the VLAN enable/disable status in order
        // to enable/disable the VLAN tag field accordingly:
        model.getHasVLanTag().getEntityChangedEvent().addListener(
                new IEventListener() {
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        EntityModel hasVLanTagEntity = (EntityModel) sender;
                        Boolean hasVLanTagValue = (Boolean) hasVLanTagEntity.getEntity();
                        getView().setVLanTagEnabled(hasVLanTagValue);
                    }
                }
                );

        // Listen for changes in the properties of the model in order
        // to update the view accordingly:
        model.getPropertyChangedEvent().addListener(
                new IEventListener() {
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        if (args instanceof PropertyChangedEventArgs) {
                            PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                            if ("DataCenterName".equals(changedArgs.PropertyName)) {
                                getView().setDataCenterName(model.getDataCenterName());
                            }
                        }
                    }
                }
                );
    }
}
