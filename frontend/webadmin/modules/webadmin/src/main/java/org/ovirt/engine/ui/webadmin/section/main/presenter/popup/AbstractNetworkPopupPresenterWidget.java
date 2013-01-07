package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;

import com.google.gwt.event.shared.EventBus;

public class AbstractNetworkPopupPresenterWidget<T extends NetworkModel, V extends AbstractNetworkPopupPresenterWidget.ViewDef<T>>
        extends AbstractModelBoundPopupPresenterWidget<T, V> {

    public interface ViewDef<T extends NetworkModel> extends AbstractModelBoundPopupPresenterWidget.ViewDef<T> {

        void setVLanTagEnabled(boolean flag);

        void setMtuEnabled(boolean flag);

        void setMessageLabel(String label);

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
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                NetworkModel model = (NetworkModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("Message".equals(propertyName)) { //$NON-NLS-1$
                    getView().setMessageLabel(model.getMessage());
                }
            }
        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().updateVisibility();
    }
}
