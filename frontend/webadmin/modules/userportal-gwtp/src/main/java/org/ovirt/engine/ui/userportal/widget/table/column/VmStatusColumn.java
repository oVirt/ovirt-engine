package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.resources.client.ImageResource;

public class VmStatusColumn extends UserPortalImageResourceColumn<UserPortalItemModel> {

    @Override
    public ImageResource getValue(UserPortalItemModel item) {
        switch (item.getStatus()) {
        case Up:
            VM vm = item.getVM();
            if (vm == null) {
                return getApplicationResources().vmStatusRunning();
            }

            if (vm.isRunOnce()) {
                return getApplicationResources().runOnceUpImage();
            } else {
                return getApplicationResources().vmStatusRunning();
            }

        case MigratingFrom:
        case MigratingTo:
            return getApplicationResources().migrationImage();
        case WaitForLaunch:
            return getApplicationResources().waitforlaunch();
        case ImageLocked:
        case PreparingForHibernate:
        case SavingState:
            return getApplicationResources().vmStatusWaiting();
        case PoweringUp:
            return getApplicationResources().vmStatusStarting();
        case RebootInProgress:
            return getApplicationResources().rebooting();
        case RestoringState:
            return getApplicationResources().vmStatusWaiting();
        case Paused:
            return getApplicationResources().vmStatusPaused();
        case Suspended:
            return getApplicationResources().vmStatusSuspended();
        case NotResponding:
        case Unassigned:
        case Unknown:
            return getApplicationResources().vmStatusUnknown();
        case ImageIllegal:
        case Down:
            return getApplicationResources().vmStatusStopped();
        case PoweringDown:
            return getApplicationResources().vmStatusStopping();
        default:
            return getApplicationResources().vmStatusUnknown();
        }
    }

}
