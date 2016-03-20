package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.widget.ImageWithDecorator;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDecoratedImageColumn;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Column for showing VM status in UserPortal. Supports tooltips.
 */
public class VmStatusColumn extends AbstractDecoratedImageColumn<UserPortalItemModel> {
    private static final int DECORATOR_POSITION_LEFT = 16;
    private static final int DECORATOR_POSITION_TOP = -9;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageWithDecorator getValue(UserPortalItemModel item) {
        VM vm = item.getVM();
        ImageResource changesImage = null;
        final String vmStatus;
        if (vm != null) {
            if (vm.isNextRunConfigurationExists()) {
                changesImage = resources.vmDelta();
            }
            vmStatus = vm.getStatus().toString();
        } else {
            vmStatus = "";
        }
        return new ImageWithDecorator(getMainImage(item), changesImage, DECORATOR_POSITION_LEFT,
                DECORATOR_POSITION_TOP, vmStatus);
    }

    @Override
    public SafeHtml getTooltip(UserPortalItemModel item) {
        VM vm = item.getVM();
        if (vm != null) {
            String tooltip = getTooltipText(vm.getStatus());
            if (tooltip != null) {
                return SafeHtmlUtils.fromSafeConstant(tooltip);
            }
        }

        return null;
    }

    private String getTooltipText(VMStatus status) {

        switch (status) {
            case Up:
                return constants.Up();
            case Down:
                return constants.Down();
            case SavingState:
                return constants.SavingState();
            case RestoringState:
                return constants.RestoringState();
            case PoweringUp:
                return constants.PoweringUp();
            case PoweringDown:
                return constants.PoweringDown();
            case RebootInProgress:
                return constants.RebootInProgress();
            case WaitForLaunch:
                return constants.WaitForLaunch();
            case ImageLocked:
                return constants.ImageLocked();
            case MigratingFrom:
                return constants.MigratingFrom();
            case MigratingTo:
                return constants.MigratingTo();
            case Suspended:
                return constants.Suspended();
            case Paused:
                return constants.Paused();
            case Unknown:
                return constants.Unknown();
            case Unassigned:
                return constants.Unassigned();
            case NotResponding:
                return constants.NotResponding();
            default:
                break;
        }
        return null;
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
