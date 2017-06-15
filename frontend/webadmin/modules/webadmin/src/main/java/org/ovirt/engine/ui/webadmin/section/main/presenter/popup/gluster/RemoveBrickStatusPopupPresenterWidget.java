package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeRebalanceStatusModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RemoveBrickStatusPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeRebalanceStatusModel, RemoveBrickStatusPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeRebalanceStatusModel> {
    }

    @Inject
    public RemoveBrickStatusPopupPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<AddBrickPopupPresenterWidget> popupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, view);
    }
}
