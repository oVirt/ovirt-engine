package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;

public class AbstractNetworkPopupPresenterWidget<T extends NetworkModel, V extends AbstractNetworkPopupPresenterWidget.ViewDef<T>>
        extends AbstractModelBoundPopupPresenterWidget<T, V> {

    public interface ViewDef<T extends NetworkModel> extends AbstractModelBoundPopupPresenterWidget.ViewDef<T> {

        void setMessageLabel(String label);

        void updateVisibility();

        void toggleProfilesVisibility(boolean visible);

        void updateDc(Version dcCompatibilityVersion, Guid dcId);
    }

    public AbstractNetworkPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void init(final T model) {
        // Let the parent do its work
        super.init(model);

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

        getView().toggleProfilesVisibility((Boolean) model.getIsVmNetwork().getEntity());
        model.getIsVmNetwork().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().toggleProfilesVisibility((Boolean) model.getIsVmNetwork().getEntity());
            }
        });

        model.getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                StoragePool dc = model.getSelectedDc();
                getView().updateDc(dc.getcompatibility_version(), dc.getId());
            }
        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().updateVisibility();
    }
}
