package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class IsProblematicImportVmColumn extends AbstractWebAdminImageResourceColumn<Object> {

    private final List<VM> problematicItems;

    public IsProblematicImportVmColumn(List<VM> problematicItems) {
        this.problematicItems = problematicItems;
    }

    @Override
    public ImageResource getValue(Object vm) {
        if (problematicItems.contains(vm)) {
            return getApplicationResources().alertImage();
        }
        return null;
    }

}
