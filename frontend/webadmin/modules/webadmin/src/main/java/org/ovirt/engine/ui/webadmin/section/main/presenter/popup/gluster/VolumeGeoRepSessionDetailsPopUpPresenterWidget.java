package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepSessionDetailsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VolumeGeoRepSessionDetailsPopUpPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeGeoRepSessionDetailsModel, VolumeGeoRepSessionDetailsPopUpPresenterWidget.ViewDef>{
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeGeoRepSessionDetailsModel> {
        public void setCheckPointCompletedAtVisibility(boolean visible);
        public void updateSessionDetailProperties(GlusterGeoRepSessionDetails selectedSessionDetail);
    }

    @Inject
    public VolumeGeoRepSessionDetailsPopUpPresenterWidget(EventBus eventBus, ViewDef view, Provider<VolumeGeoRepSessionDetailsPopUpPresenterWidget> geoRepSessionDetailPopupProvider, Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, view);
    }

    @Override
    public void init(final VolumeGeoRepSessionDetailsModel model) {
        super.init(model);
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if(args.propertyName.equalsIgnoreCase("selectedSessionSummaryRow")) {//$NON-NLS-1$
                GlusterGeoRepSessionDetails selectedSessionDetail = model.getGeoRepSessionSummary().getSelectedItem().getEntity();
                getView().setCheckPointCompletedAtVisibility(selectedSessionDetail.isCheckpointCompleted());
                getView().updateSessionDetailProperties(selectedSessionDetail);
            }
        });
    }
}
