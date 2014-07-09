package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;

public class AbstractNetworkPopupPresenterWidget<T extends NetworkModel, V extends AbstractNetworkPopupPresenterWidget.ViewDef<T>>
        extends AbstractModelBoundPopupPresenterWidget<T, V> {

    public interface ViewDef<T extends NetworkModel> extends AbstractModelBoundPopupPresenterWidget.ViewDef<T> {

        void setMessageLabel(String label);

        void updateVisibility();

        void toggleSubnetVisibility(boolean visible);

        void toggleProfilesVisibility(boolean visible);

        UiCommandButton getQosButton();

        void addMtuEditor();
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
                String propertyName = ((PropertyChangedEventArgs) args).propertyName;

                if ("Message".equals(propertyName)) { //$NON-NLS-1$
                    getView().setMessageLabel(model.getMessage());
                }
            }
        });

        getView().toggleSubnetVisibility((Boolean) model.getExport().getEntity());
        model.getExport().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().toggleSubnetVisibility((Boolean) model.getExport().getEntity());
            }
        });

        getView().toggleProfilesVisibility(model.getProfiles().getIsAvailable());
        model.getProfiles().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsAvailable".equals(((PropertyChangedEventArgs) args).propertyName)) { //$NON-NLS-1$
                    getView().toggleProfilesVisibility(model.getProfiles().getIsAvailable());
                }
            }
        });

        getView().getQosButton().setCommand(model.getAddQosCommand());
        getView().getQosButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().getQosButton().getCommand().execute();
            }
        });

        getView().addMtuEditor();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().updateVisibility();
    }
}
