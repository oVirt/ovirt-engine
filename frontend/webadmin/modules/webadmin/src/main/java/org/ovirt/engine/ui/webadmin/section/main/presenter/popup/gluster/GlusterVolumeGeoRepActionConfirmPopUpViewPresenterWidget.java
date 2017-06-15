package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoRepActionConfirmationModel;

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

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if(args.propertyName.equalsIgnoreCase("forceLabel")) {//$NON-NLS-1$
                getView().setForceLabelMessage(model.getForceLabel());
            } else if(args.propertyName.equalsIgnoreCase("forceHelp")) {//$NON-NLS-1$
                getView().setForceHelp(model.getForceHelp());
            } else if (args.propertyName.equalsIgnoreCase("Message")) {//$NON-NLS-1$
                getView().setErrorMessage(model.getMessage());
            } else if(args.propertyName.equalsIgnoreCase("ActionConfirmationMessage")) {//$NON-NLS-1$
                getView().setActionConfirmationMessage(model.getMessage());
            }
        });
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterVolumeGeoRepActionConfirmationModel> {
        public void setForceLabelMessage(String forceLabelMessage);
        public void setForceHelp(String forceHelpText);
        public void setErrorMessage(String errorMessage);
        public void setActionConfirmationMessage(String message);
    }

}
