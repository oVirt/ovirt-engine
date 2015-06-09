package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;

import com.google.gwt.safehtml.shared.SafeHtml;

public class ImportProgressColumn extends AbstractOneColorPercentColumn<VM> {

    public ImportProgressColumn() {
        super(ProgressBarColors.GREEN);
    }

    @Override
    protected Integer getProgressValue(VM vm) {
        return vm.getBackgroundOperationDescription() != null ? vm.getBackgroundOperationProgress() : null;
    }

    @Override
    public SafeHtml getValue(VM object) {
        return object.getBackgroundOperationDescription() != null ? super.getValue(object) : null;
    }

    @Override
    protected String getStyle() {
        return "engine-progress-box-migration";//$NON-NLS-1$
    }

    @Override
    protected String getProgressText(VM vm) {
        String description = vm.getBackgroundOperationDescription();
        return description != null ? description : super.getProgressText(vm);
    }
}
