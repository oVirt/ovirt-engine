package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;


public class GlusterHookSyncStatusColumn extends AbstractImageResourceColumn<GlusterHookEntity> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(GlusterHookEntity hookEntity) {
        if (hookEntity.hasConflicts()) {
            return resources.alertImage();
        }
        return null;
    }
}
