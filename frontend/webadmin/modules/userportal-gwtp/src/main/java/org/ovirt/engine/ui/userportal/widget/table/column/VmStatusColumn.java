package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.widget.ImageWithDecorator;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDecoratedImageColumn;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class VmStatusColumn extends AbstractDecoratedImageColumn<UserPortalItemModel> {
    private static final int DECORATOR_POSITION_LEFT = 16;
    private static final int DECORATOR_POSITION_TOP = -9;

    private final static ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageWithDecorator getValue(UserPortalItemModel item) {
        VM vm = item.getVM();
        ImageResource changesImage = null;
        if (vm != null && vm.isNextRunConfigurationExists()) {
            changesImage = resources.vmDelta();
        }
        return new ImageWithDecorator(getMainImage(item), changesImage, DECORATOR_POSITION_LEFT, DECORATOR_POSITION_TOP);
    }

    private ImageResource getMainImage(UserPortalItemModel item) {
        switch (item.getStatus()) {
        case Up:
            VM vm = item.getVM();
            if (vm == null) {
                return resources.vmStatusRunning();
            }

            if (vm.isRunOnce()) {
                return resources.runOnceUpImage();
            } else {
                return resources.vmStatusRunning();
            }

        case MigratingFrom:
        case MigratingTo:
            return resources.migrationImage();
        case WaitForLaunch:
            return resources.waitforlaunch();
        case ImageLocked:
        case SavingState:
            return resources.vmStatusWaiting();
        case PoweringUp:
            return resources.vmStatusStarting();
        case RebootInProgress:
            return resources.rebooting();
        case RestoringState:
            return resources.vmStatusWaiting();
        case Paused:
            return resources.vmStatusPaused();
        case Suspended:
            return resources.vmStatusSuspended();
        case NotResponding:
        case Unassigned:
        case Unknown:
            return resources.vmStatusUnknown();
        case ImageIllegal:
        case Down:
            return resources.vmStatusStopped();
        case PoweringDown:
            return resources.vmStatusStopping();
        default:
            return resources.vmStatusUnknown();
        }
    }

}
