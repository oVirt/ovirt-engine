package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;

public class AbstractNetworkPopupPresenterWidget<T extends NetworkModel, V extends AbstractNetworkPopupPresenterWidget.ViewDef<T>>
        extends AbstractModelBoundPopupPresenterWidget<T, V> {

    public interface ViewDef<T extends NetworkModel> extends AbstractModelBoundPopupPresenterWidget.ViewDef<T> {

        void setVLanTagEnabled(boolean flag);

        void setMtuEnabled(boolean flag);

        void setNetworkClusterList(ListModel networkClusterList);

        void setMessageLabel(String label);

        void postModelEnabled(boolean enabled);

        HasClickHandlers getApply();

        void setApplyEnabled(boolean enabled);

        void updateVisibility();

    }

    public AbstractNetworkPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void init(final T model) {
        // Let the parent do its work
        super.init(model);

        // Set the enabled/disabled status of the VLAN tag field
        // according to the initial value in the model
        Boolean hasVLanTagValue = (Boolean) model.getHasVLanTag().getEntity();
        getView().setVLanTagEnabled(hasVLanTagValue);

        // Listen for changes in the VLAN enable/disable status in order
        // to enable/disable the VLAN tag field accordingly
        model.getHasVLanTag().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel hasVLanTagEntity = (EntityModel) sender;
                Boolean hasVLanTagValue = (Boolean) hasVLanTagEntity.getEntity();
                getView().setVLanTagEnabled(hasVLanTagValue);
            }
        });

        // Set the enabled/disabled status of the MTU field
        // according to the initial value in the model
        Boolean hasMtuValue = (Boolean) model.getHasMtu().getEntity();
        getView().setMtuEnabled(hasMtuValue);

        // Listen for changes in the MTU enable/disable status in order
        // to enable/disable the MTU field accordingly
        model.getHasMtu().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel hasMtuEntity = (EntityModel) sender;
                Boolean hasMtuValue = (Boolean) hasMtuEntity.getEntity();
                getView().setMtuEnabled(hasMtuValue);

                // Clear MTU field in case MTU is disabled
                if (!hasMtuValue) {
                    model.getMtu().setEntity(null);
                }
            }
        });

        // Listen to Properties
        getView().setNetworkClusterList(model.getNetworkClusterList());

        // Listen to "IsEnabled" property
        model.getIsEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                boolean inputFieldsEnabled = (Boolean) entity.getEntity();
                getView().postModelEnabled(inputFieldsEnabled);
            }
        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().updateVisibility();
    }
}
