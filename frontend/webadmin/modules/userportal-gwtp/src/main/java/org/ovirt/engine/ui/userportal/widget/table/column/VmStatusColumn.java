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

    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageWithDecorator getValue(UserPortalItemModel item) {
        VM vm = item.getVM();
        ImageResource changesImage = null;
        if (vm != null && vm.isNextRunConfigurationExists()) {
            changesImage = resources.vmDelta();
        }
        return new ImageWithDecorator(getMainImage(item), changesImage, DECORATOR_POSITION_LEFT, DECORATOR_POSITION_TOP);
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

        // TODO: (Einav?) is it ok to show all these tooltips in userportal?
        // TODO tt used to be  statusTranslator.translate(value.getStatus().name());

        switch (status) {
            case Up:
                return constants.up();
            case Down:
                return constants.down();
            case SavingState:
                return constants.SavingState();
            case RestoringState:
                return constants.RestoringState();
            case PoweringUp:
                return constants.poweringUp();
            case PoweringDown:
                return constants.poweringDown();
            case RebootInProgress:
                return constants.RebootInProgress();
            case WaitForLaunch:
                return constants.WaitForLaunch();
            case ImageLocked:
                return constants.imageLocked();
            case MigratingFrom:
                return constants.MigratingFrom();
            case MigratingTo:
                return constants.MigratingTo();
            case Suspended:
                return constants.suspended();
            case Paused:
                return constants.paused();
            case Unknown:
                return constants.unknown();
            case Unassigned:
                return constants.unassigned();
            case NotResponding:
                return constants.notResponding();
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
