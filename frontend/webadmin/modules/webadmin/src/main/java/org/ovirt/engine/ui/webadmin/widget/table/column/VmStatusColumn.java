package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IdentifiableComparator;
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

    private final static ApplicationConstants constants = AssetProvider.getConstants();

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

            IdentifiableComparator<VMStatus> valueComparator = new Linq.IdentifiableComparator<VMStatus>();

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

        VM vm = getValue(object);
        if (vm == null) {
            return null;
        }

        boolean updateNeeded = vm.getStatus() == VMStatus.Up && vm.getGuestAgentStatus() == GuestAgentStatus.UpdateNeeded;


        if (!updateNeeded && (vm.getVmPauseStatus() != VmPauseStatus.NONE || vm.getVmPauseStatus() != VmPauseStatus.NOERR)) {
            tooltip = getTooltipText(vm.getStatus());
        }
        else {
            tooltip = updateNeeded ? constants.newtools() : EnumTranslator.getInstance().translate(vm.getVmPauseStatus());
        }

        if (tooltip != null) {
            return SafeHtmlUtils.fromSafeConstant(tooltip);
        }

        return null;
    }

    private String getTooltipText(VMStatus status) {

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
