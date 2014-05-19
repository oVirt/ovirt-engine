package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickStatusPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RemoveBrickStatusPopupView extends VolumeRebalanceStatusPopupView implements RemoveBrickStatusPopupPresenterWidget.ViewDef {

    @Inject
    public RemoveBrickStatusPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages) {
        super(eventBus, resources, constants, messages);
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
