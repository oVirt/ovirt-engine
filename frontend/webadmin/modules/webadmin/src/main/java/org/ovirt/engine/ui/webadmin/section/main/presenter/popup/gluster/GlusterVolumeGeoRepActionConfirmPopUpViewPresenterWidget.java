package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoRepActionConfirmationModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterVolumeGeoRepActionConfirmationModel, GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget.ViewDef> {

    @Inject
    public GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final GlusterVolumeGeoRepActionConfirmationModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if(args.propertyName.equalsIgnoreCase("forceLabel")) {//$NON-NLS-1$
                    if(model.getForceLabel() != null) {
                        getView().setForceLabelMessage(model);
                    }
                } else if(args.propertyName.equalsIgnoreCase("forceHelp")) {//$NON-NLS-1$
                    getView().setForceHelp(model);
                }
            }
        });
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterVolumeGeoRepActionConfirmationModel> {
        public void setForceLabelMessage(GlusterVolumeGeoRepActionConfirmationModel object);
        public void setForceHelp(GlusterVolumeGeoRepActionConfirmationModel object);
    }

}
