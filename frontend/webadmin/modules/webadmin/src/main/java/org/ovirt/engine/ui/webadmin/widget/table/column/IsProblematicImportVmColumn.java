package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code PermissionTypeDataTemplate}.
 */
public class IsProblematicImportVmColumn extends AbstractImageResourceColumn<Object> {

    private final static ApplicationResources resources = AssetProvider.getResources();

    private final List<VM> problematicItems;

    public IsProblematicImportVmColumn(List<VM> problematicItems) {
        this.problematicItems = problematicItems;
    }

    @Override
    public ImageResource getValue(Object vm) {
        if (problematicItems.contains(vm)) {
            return resources.alertImage();
        }
        return null;
    }

}
