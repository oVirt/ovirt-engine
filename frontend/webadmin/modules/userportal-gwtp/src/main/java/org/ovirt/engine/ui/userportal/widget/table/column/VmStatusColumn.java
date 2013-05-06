package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.resources.client.ImageResource;

public class VmStatusColumn extends UserPortalImageResourceColumn<UserPortalItemModel> {

    @Override
    public ImageResource getValue(UserPortalItemModel item) {
        switch (item.getStatus()) {
        case Up:
            return getApplicationResources().vmStatusRunning();
        case WaitForLaunch:
        case ImageLocked:
        case MigratingFrom:
        case MigratingTo:
        case PreparingForHibernate:
        case SavingState:
            return getApplicationResources().vmStatusWaiting();
        case PoweringUp:
        case RebootInProgress:
        case RestoringState:
            return getApplicationResources().vmStatusStarting();
        case Paused:
        case Suspended:
            return getApplicationResources().vmStatusPaused();
        case Unknown:
            return getApplicationResources().vmStatusUnknown();
        case Unassigned:
        case ImageIllegal:
        case Down:
        case NotResponding:
        case PoweredDown:
            return getApplicationResources().vmStatusStopped();
        case PoweringDown:
            return getApplicationResources().vmStatusStopping();
        default:
            return getApplicationResources().vmStatusUnknown();
        }
    }

}
