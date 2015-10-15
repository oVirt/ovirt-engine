package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractProgressBarColumn;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;

public class MigrationProgressColumn extends AbstractOneColorPercentColumn<VM> {

    private EnumRenderer<VMStatus> renderer = new EnumRenderer<>();

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    public MigrationProgressColumn() {
        super(AbstractProgressBarColumn.ProgressBarColors.GREEN);
    }

    @Override
    public Integer getProgressValue(VM object) {
        return object.getMigrationProgressPercent();
    }

    public final SafeHtml getValue(VM vm) {
        if (!migrating(vm)) {
            return null;
        }

        return super.getValue(vm);
    }

    public boolean migrating(VM vm) {
        return vm.getStatus() == VMStatus.MigratingFrom;
    }

    @Override
    protected String getStyle() {
        return "engine-progress-box-migration";//$NON-NLS-1$

    }

    @Override
    protected String getProgressText(VM vm) {
        String percentText = super.getProgressText(vm);

        if (migrating(vm)) {
            return messages.migratingProgress(renderer.render(vm.getStatus()), percentText);
        }

        return percentText;
    }
}
