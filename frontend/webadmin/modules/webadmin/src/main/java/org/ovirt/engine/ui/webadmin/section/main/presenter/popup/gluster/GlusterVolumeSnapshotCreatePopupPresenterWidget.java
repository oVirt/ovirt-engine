package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class GlusterVolumeSnapshotCreatePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterVolumeSnapshotModel, GlusterVolumeSnapshotCreatePopupPresenterWidget.ViewDef> {
    @Inject
    public GlusterVolumeSnapshotCreatePopupPresenterWidget(EventBus eventBus,
            ViewDef view,
            Provider<GlusterVolumeSnapshotCreatePopupPresenterWidget> snapshotPopupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupPrivder) {
        super(eventBus, view);
    }

    @Override
    public void init(final GlusterVolumeSnapshotModel model) {
        super.init(model);

        model.getInterval().getSelectedItemChangedEvent().addListener((ev, sender, args) -> getView().setCriticalIntervalLabelVisibility(model,
                Integer.parseInt(((ListModel<String>) sender).getSelectedItem())));

        model.getRecurrence().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            getView().updateVisibilities(model);
            getView().setCriticalIntervalLabelVisibility(model,
                    Integer.parseInt(model.getInterval().getSelectedItem()));
            getView().setMessage(null);
        });

        model.getEndByOptions().getSelectedItemChangedEvent().addListener((ev, sender, args) -> getView().setEndDateVisibility(model));

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if(args.propertyName.equalsIgnoreCase("validateAndSwitchAppropriateTab")) {//$NON-NLS-1$
                getView().handleValidationErrors(model);
                getView().switchTabBasedOnEditorInvalidity();
            } else if(args.propertyName.equalsIgnoreCase("modelPropertiesChanged")) {//$NON-NLS-1$
                getView().handleValidationErrors(model);
            }
        });
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterVolumeSnapshotModel> {
        public void updateVisibilities(GlusterVolumeSnapshotModel object);

        public void setEndDateVisibility(GlusterVolumeSnapshotModel object);

        public void setCriticalIntervalLabelVisibility(GlusterVolumeSnapshotModel object, int value);

        public void handleValidationErrors(GlusterVolumeSnapshotModel object);

        public void switchTabBasedOnEditorInvalidity();
    }
}
