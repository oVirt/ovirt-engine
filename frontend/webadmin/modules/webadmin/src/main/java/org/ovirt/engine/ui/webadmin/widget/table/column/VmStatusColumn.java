package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Arrays;
import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.widget.table.cell.StatusCompositeCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class VmStatusColumn extends AbstractColumn<VM, VM> {

    public VmStatusColumn() {
        this(() -> null);
    }

    public VmStatusColumn(Supplier<Guid> currentHostIdSupplier) {
        super(new StatusCompositeCell<>(Arrays.asList(
                new StatusColumn(),
                new ReasonColumn<VM>() {
                    @Override
                    protected String getReason(VM value) {
                        return value.getStopReason();
                    }
                },
                new MigrationProgressColumn(currentHostIdSupplier),
                new ImportProgressColumn()
        )));
    }

    @Override
    public VM getValue(VM object) {
        return object;
    }

    @Override
    public SafeHtml getTooltip(VM value) {
        String stopReason = value.getStopReason();
        if (stopReason != null && !stopReason.trim().isEmpty()) {
            return SafeHtmlUtils.fromString(stopReason);
        }
        return null;
    }

    private static class StatusColumn extends AbstractEnumColumn<VM, VMStatus> {
        @Override
        public VMStatus getRawValue(VM object) {
            return object.getStatus();
        }

        @Override
        public String getValue(VM vm) {
            if (vm.getStatus() == VMStatus.MigratingFrom) {
                // will be rendered by progress column
                return null;
            }

            if (vm.getBackgroundOperationDescription() != null) {
                // will be rendered by progress column
                return null;
            }
            return super.getValue(vm);
        }
    }
}
