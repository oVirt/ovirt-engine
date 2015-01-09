package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.widget.ImageWithDecorator;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageWithDecoratorColumn;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.resources.client.ImageResource;

import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

public class VmStatusColumn extends AbstractImageWithDecoratorColumn<UserPortalItemModel> {
    private static final int DECORATOR_POSITION_LEFT = 16;
    private static final int DECORATOR_POSITION_TOP = -9;

    protected ApplicationResources getApplicationResources() {
        return ClientGinjectorProvider.getApplicationResources();
    }

    @Override
    public ImageWithDecorator getValue(UserPortalItemModel item) {
        VM vm = item.getVM();
        ImageResource changesImage = null;
        if (vm != null && vm.isNextRunConfigurationExists()) {
            changesImage = getApplicationResources().vmDelta();
        }
        return new ImageWithDecorator(getMainImage(item), changesImage, DECORATOR_POSITION_LEFT, DECORATOR_POSITION_TOP);
    }

    private ImageResource getMainImage(UserPortalItemModel item) {
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
