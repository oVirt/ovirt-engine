package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoRepCreateModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class GlusterVolumeGeoRepCreateSessionPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterVolumeGeoRepCreateModel, GlusterVolumeGeoRepCreateSessionPopupPresenterWidget.ViewDef>{

    @Inject
    public GlusterVolumeGeoRepCreateSessionPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterVolumeGeoRepCreateModel> {
        public void setSuggestedConfigViolations(String recommendationViolations);

        public void setFailureMessage(String failureMessage);
    }

    @Override
    public void init(final GlusterVolumeGeoRepCreateModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if(args.propertyName.equalsIgnoreCase("RecommendationViolations")) {//$NON-NLS-1$
                getView().setSuggestedConfigViolations(model.getRecommendationViolations());
            } else if (args.propertyName.equalsIgnoreCase("QueryFailed")) {//$NON-NLS-1$
                getView().setFailureMessage(model.getQueryFailureMessage());
            }
        });
    }
}
