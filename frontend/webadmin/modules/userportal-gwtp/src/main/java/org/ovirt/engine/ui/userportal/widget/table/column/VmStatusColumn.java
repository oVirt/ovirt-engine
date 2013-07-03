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
        case ImageLocked:
        case PreparingForHibernate:
        case SavingState:
            return getApplicationResources().vmStatusWaiting();
        case PoweringUp:
        case RebootInProgress:
        case RestoringState:
            return getApplicationResources().vmStatusStarting();
        case Paused:
            return getApplicationResources().frozenImage();
        case Suspended:
            return getApplicationResources().vmStatusPaused();
        case NotResponding:
        case Unknown:
            return getApplicationResources().vmStatusUnknown();
        case Unassigned:
        case ImageIllegal:
        case Down:
        case PoweredDown:
            return getApplicationResources().vmStatusStopped();
        case PoweringDown:
            return getApplicationResources().vmStatusStopping();
        default:
            return getApplicationResources().vmStatusUnknown();
        }
    }

}
