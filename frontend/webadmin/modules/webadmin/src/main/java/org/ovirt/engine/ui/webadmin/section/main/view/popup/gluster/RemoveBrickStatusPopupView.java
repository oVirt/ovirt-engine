package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickStatusPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RemoveBrickStatusPopupView extends VolumeRebalanceStatusPopupView implements RemoveBrickStatusPopupPresenterWidget.ViewDef {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RemoveBrickStatusPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public String getColumnHeaderForFilesMoved() {
        return constants.filesMigrated();
    }

    @Override
    public boolean isSkippedFileCountNeeded(){
        return false;
    }
}
