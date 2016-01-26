package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IdentifiableComparator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.HostStatusCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class HostStatusColumn<S> extends AbstractColumn<S, VDS> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public HostStatusColumn() {
        super(new HostStatusCell());
    }

    @Override
    public VDS getValue(S object) {
        if (object instanceof VDS){
            return (VDS) object;
        }
        if (object instanceof PairQueryable){
            if (((PairQueryable) object).getSecond() instanceof VDS){
                return (VDS) ((PairQueryable) object).getSecond();
            }
        }
        return null;
    }

    public void makeSortable() {
        makeSortable(new Comparator<S>() {

            IdentifiableComparator<VDSStatus> valueComparator = new Linq.IdentifiableComparator<>();

            @Override
            public int compare(S o1, S o2) {
                VDSStatus status1 = (getValue(o1) == null) ? null : getValue(o1).getStatus();
                VDSStatus status2 = (getValue(o2) == null) ? null : getValue(o2).getStatus();
                return valueComparator.compare(status1, status2);
            }
        });
    }

    @Override
    public SafeHtml getTooltip(S object) {
        VDS vds = getValue(object);
        if (vds != null) {
            VDSStatus status = vds.getStatus();
            String tooltip = getTooltipText(status);
            if (tooltip != null) {
                return SafeHtmlUtils.fromSafeConstant(tooltip);
            }
        }
        return null;
    }

    private String getTooltipText(VDSStatus status) {

        switch (status) {
            case Up:
                return constants.up();
            case Down:
                return constants.down();
            case Unassigned:
                return constants.unassigned();
            case Maintenance:
                return constants.maintenance();
            case NonResponsive:
                return constants.nonResponsive();
            case Error:
                return constants.error();
            case Installing:
                return constants.installing();
            case InstallFailed:
                return constants.installFailed();
            case Reboot:
                return constants.reboot();
            case PreparingForMaintenance:
                return constants.preparingForMaintenance();
            case NonOperational:
                return constants.nonOperational();
            case PendingApproval:
                return constants.pendingApproval();
            case Initializing:
                return constants.initializing();
            case Connecting:
                return constants.connecting();
            case InstallingOS:
                return constants.installingOS();
            case Kdumping:
                return constants.kdumping();
            default:
                break;
        }
        return null;
    }

}
