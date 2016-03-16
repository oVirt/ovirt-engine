package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IdentifiableComparator;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VmStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Image column that corresponds to XAML {@code VmStatusTemplate}.
 */
public class VmStatusColumn<T> extends AbstractColumn<T, VM> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();


    public VmStatusColumn() {
        super(new VmStatusCell());
    }

    @Override
    public VM getValue(T object) {
        if (object instanceof VM){
            return (VM)object;
        }if (object instanceof PairQueryable && ((PairQueryable) object).getSecond() instanceof VM){
            return ((PairQueryable<VmNetworkInterface, VM>) object).getSecond();
        }
        return null;
    }

    public void makeSortable() {
        makeSortable(new Comparator<T>() {

            IdentifiableComparator<VMStatus> valueComparator = new Linq.IdentifiableComparator<>();

            @Override
            public int compare(T o1, T o2) {
                VMStatus status1 = (getValue(o1) == null) ? null : getValue(o1).getStatus();
                VMStatus status2 = (getValue(o2) == null) ? null : getValue(o2).getStatus();
                return valueComparator.compare(status1, status2);
            }
        });
    }

    @Override
    public SafeHtml getTooltip(T object) {

        String tooltip = null;
        EnumTranslator translator = EnumTranslator.getInstance();

        VM vm = getValue(object);
        if (vm == null) {
            return null;
        }

        // the tooltip always has at least the vm status
        tooltip = getStatusTooltipText(vm.getStatus());

        // optionally, the vm status icon gets an alert exclamation. Indicate why.
        if (needsAlert(vm)) {

            if (isUpdateNeeded(vm)) {
                tooltip += "<br/><br/>" + constants.newtools(); //$NON-NLS-1$
            }

            if (hasPauseError(vm)) {
                tooltip += "<br/><br/>" + translator.translate(vm.getVmPauseStatus()); //$NON-NLS-1$
            }

            if (hasDifferentTimezone(vm)) {
                tooltip += "<br/><br/>" + constants.guestTimezoneDiffers(); //$NON-NLS-1$
            }

            if (hasDifferentOSType(vm)) {
                tooltip += "<br/><br/>" + constants.guestOSDiffers(); //$NON-NLS-1$
            }
        }

        if (tooltip != null) {
            return SafeHtmlUtils.fromSafeConstant(tooltip);
        }

        return null;
    }

    public static boolean needsAlert(VM vm) {
        return hasDifferentTimezone(vm) || hasDifferentOSType(vm) || isUpdateNeeded(vm) ||
            hasPauseError(vm);
    }

    private static boolean hasDifferentOSType(VM vm) {
        return AsyncDataProvider.getInstance().isWindowsOsType(vm.getVmOsId()) != (vm.getGuestOsType() == OsType.Windows);
    }

    private static boolean hasDifferentTimezone(VM vm) {
        if (AsyncDataProvider.getInstance().isWindowsOsType(vm.getVmOsId())) {
            String timeZone = vm.getTimeZone();
            if (timeZone != null && !timeZone.isEmpty()) {
                int offset = 0;
                String javaZoneId = null;
                // convert to java & calculate offset
                javaZoneId = WindowsJavaTimezoneMapping.get(timeZone);

                if (javaZoneId != null) {
                    offset = TimeZoneType.GENERAL_TIMEZONE.getStandardOffset(javaZoneId);
                }

                if (vm.getGuestOsTimezoneOffset() != offset) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isUpdateNeeded(VM vm) {
        return vm.getStatus() == VMStatus.Up && vm.getGuestAgentStatus() == GuestAgentStatus.UpdateNeeded;
    }

    private static boolean hasPauseError(VM vm) {
        return vm.getVmPauseStatus() != VmPauseStatus.NONE && vm.getVmPauseStatus() != VmPauseStatus.NOERR;
    }

    private String getStatusTooltipText(VMStatus status) {

        switch (status) {
            case Up:
                return constants.up();
            case Down:
                return constants.down();
            case SavingState:
                return constants.vmStatusSaving();
            case RestoringState:
                return constants.restoring();
            case PoweringUp:
                return constants.poweringUp();
            case PoweringDown:
                return constants.poweringDown();
            case RebootInProgress:
                return constants.rebooting();
            case WaitForLaunch:
                return constants.waitForLaunchStatus();
            case ImageLocked:
                return constants.imageLocked();
            case MigratingFrom:
                return constants.migrating();
            case MigratingTo:
                return constants.migrating();
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

}
